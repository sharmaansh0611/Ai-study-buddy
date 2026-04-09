import { createOllamaEmbedding } from "./ollamaService.js";

const EMBEDDING_MAX_CONCURRENCY = Number(process.env.OLLAMA_EMBEDDING_MAX_CONCURRENCY || 2);
const EMBEDDING_MAX_RETRIES = Number(process.env.OLLAMA_EMBEDDING_MAX_RETRIES || 2);
const EMBEDDING_RETRY_BASE_DELAY_MS = Number(
  process.env.OLLAMA_EMBEDDING_RETRY_BASE_DELAY_MS || 400
);
const EMBEDDING_PROGRESS_LOG_EVERY = Number(process.env.OLLAMA_EMBEDDING_PROGRESS_LOG_EVERY || 10);

const sleep = (milliseconds) =>
  new Promise((resolve) => {
    setTimeout(resolve, milliseconds);
  });

const toPositiveInteger = (value, fallback) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
};

const isRetryableEmbeddingError = (error) => {
  const message = `${error?.message || ""} ${error?.cause?.message || ""}`.toLowerCase();
  return (
    message.includes("fetch failed") ||
    message.includes("timeout") ||
    message.includes("timed out") ||
    message.includes("econnreset") ||
    message.includes("enotfound") ||
    message.includes("socket")
  );
};

const embedTextWithRetry = async ({ text }) => {
  const maxRetries = toPositiveInteger(EMBEDDING_MAX_RETRIES, 2);
  let lastError;

  for (let attempt = 0; attempt <= maxRetries; attempt += 1) {
    try {
      return await createOllamaEmbedding(text);
    } catch (error) {
      lastError = error;

      if (attempt === maxRetries || !isRetryableEmbeddingError(error)) {
        break;
      }

      const jitter = Math.floor(Math.random() * 120);
      const backoffDelay =
        toPositiveInteger(EMBEDDING_RETRY_BASE_DELAY_MS, 400) * 2 ** attempt + jitter;
      await sleep(backoffDelay);
    }
  }

  throw lastError;
};
const mapWithConcurrency = async (items, mapper, concurrencyOverride) => {
  const concurrency =
    toPositiveInteger(concurrencyOverride, 0) || toPositiveInteger(EMBEDDING_MAX_CONCURRENCY, 4);
  const results = new Array(items.length);
  let nextIndex = 0;

  const worker = async () => {
    while (nextIndex < items.length) {
      const currentIndex = nextIndex;
      nextIndex += 1;
      results[currentIndex] = await mapper(items[currentIndex], currentIndex);
    }
  };

  const workers = Array.from(
    { length: Math.min(concurrency, items.length) },
    () => worker()
  );

  await Promise.all(workers);
  return results;
};

export const createEmbeddings = async (texts) => {
  const sanitizedTexts = texts.map((text) => text.trim()).filter(Boolean);

  if (!sanitizedTexts.length) {
    return [];
  }

  try {
    let completed = 0;
    return await mapWithConcurrency(sanitizedTexts, async (text) =>
      embedTextWithRetry({ text }).then((vector) => {
        completed += 1;
        const logEvery = toPositiveInteger(EMBEDDING_PROGRESS_LOG_EVERY, 10);
        if (completed % logEvery === 0 || completed === sanitizedTexts.length) {
          console.log(`Embedding progress: ${completed}/${sanitizedTexts.length}`);
        }
        return vector;
      })
    );
  } catch (error) {
    const causeMessage = error?.cause?.message ? ` | cause: ${error.cause.message}` : "";
    throw new Error(`Ollama embedding generation failed: ${error.message}${causeMessage}`);
  }
};

export const createEmbedding = async (text) => {
  const [embedding] = await createEmbeddings([text]);
  return embedding;
};
