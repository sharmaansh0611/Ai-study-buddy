const OLLAMA_BASE_URL = process.env.OLLAMA_BASE_URL || "http://127.0.0.1:11434";
const OLLAMA_CHAT_MODEL = process.env.OLLAMA_CHAT_MODEL || "qwen2.5:7b";
const OLLAMA_FAST_MODEL = process.env.OLLAMA_FAST_MODEL || OLLAMA_CHAT_MODEL;
const OLLAMA_REQUEST_TIMEOUT_MS = Number(process.env.OLLAMA_REQUEST_TIMEOUT_MS || 120000);
const OLLAMA_TEMPERATURE = Number(process.env.OLLAMA_TEMPERATURE ?? process.env.AI_TEMPERATURE);

const buildOllamaOptions = () => {
  const temperature = OLLAMA_TEMPERATURE;
  if (!Number.isFinite(temperature)) {
    return undefined;
  }
  return { temperature };
};

const buildAbortSignal = () => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), OLLAMA_REQUEST_TIMEOUT_MS);
  return {
    signal: controller.signal,
    clear: () => clearTimeout(timeoutId),
  };
};

const requestOllama = async ({ endpoint, body }) => {
  const timeout = buildAbortSignal();

  try {
    const response = await fetch(`${OLLAMA_BASE_URL}${endpoint}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
      signal: timeout.signal,
    });

    const payload = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(payload?.error || `Ollama request failed with status ${response.status}`);
    }

    return payload;
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error(`Ollama request timed out after ${OLLAMA_REQUEST_TIMEOUT_MS}ms`);
    }

    throw error;
  } finally {
    timeout.clear();
  }
};

export const generateOllamaResponse = async (prompt, { fast = false } = {}) => {
  if (!prompt?.trim()) {
    throw new Error("prompt is required for Ollama generation.");
  }

  const payload = await requestOllama({
    endpoint: "/api/generate",
    body: {
      model: fast ? OLLAMA_FAST_MODEL : OLLAMA_CHAT_MODEL,
      prompt,
      stream: false,
      options: buildOllamaOptions(),
    },
  });

  const reply = payload?.response?.trim();

  if (!reply) {
    throw new Error("Ollama returned an empty response.");
  }

  return reply;
};

export const createOllamaEmbedding = async (text) => {
  if (!text?.trim()) {
    throw new Error("text is required for Ollama embedding generation.");
  }

  const embedModel = process.env.OLLAMA_EMBED_MODEL || "nomic-embed-text";

  try {
    const payload = await requestOllama({
      endpoint: "/api/embed",
      body: {
        model: embedModel,
        input: text,
      },
    });

    const embedding = payload?.embeddings?.[0];

    if (!Array.isArray(embedding) || !embedding.length) {
      throw new Error("Ollama embedding response was empty.");
    }

    return embedding;
  } catch (error) {
    const fallbackPayload = await requestOllama({
      endpoint: "/api/embeddings",
      body: {
        model: embedModel,
        prompt: text,
      },
    });

    const embedding = fallbackPayload?.embedding;

    if (!Array.isArray(embedding) || !embedding.length) {
      throw new Error(`Ollama embedding generation failed: ${error.message}`);
    }

    return embedding;
  }
};
