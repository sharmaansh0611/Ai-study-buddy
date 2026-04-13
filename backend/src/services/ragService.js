import { routeAI } from "./aiRouter.js";
import { retrieveRelevantChunks } from "./vectorStoreService.js";

const buildContextBlock = (chunks) =>
  chunks
    .map(
      (chunk, index) =>
        `Source ${index + 1} | note_id=${chunk.note_id} | page=${chunk.page_number}\n${chunk.chunk_text}`
    )
    .join("\n\n");

export const buildRagPrompt = ({ question, chunks }) => ({
  system:
    "You are an offline AI study assistant. Use only the provided study notes. Prefer chunks that directly match the requested section heading, clause number, or terminology. If the answer is not in the notes, say that clearly and suggest what the student should review next.",
  user: `Use the following study notes to answer the question.

Context:
${buildContextBlock(chunks)}

Question:
${question}

Answer clearly using only the provided context. If the question mentions a numbered section such as 11.01, prioritize that exact section.`,
});

export const buildSelectedTextPrompt = ({ question, selectedText, pageNumber }) => ({
  system:
    "You are a helpful study assistant. Answer strictly from the selected note excerpt. If the excerpt is insufficient, explicitly say so.",
  user: `Selected excerpt${pageNumber ? ` (page ${pageNumber})` : ""}:
${selectedText}

Question:
${question}

Give a concise and accurate answer based only on the selected excerpt.`,
});

export const answerNoteQuestion = async ({ userId, noteId, question }) => {
  try {
    const chunks = await retrieveRelevantChunks({
      userId,
      noteId,
      question,
    });

    console.log("----- RAG RETRIEVAL DEBUG -----");
    console.log("User Question:", question);

    chunks.forEach((chunk, index) => {
      console.log(`Chunk ${index + 1}:`);
      console.log("Page:", chunk.page_number);
      console.log("Text:", chunk.chunk_text.substring(0, 200));
      console.log("----------------------------");
    });

    if (!chunks.length) {
      return {
        answer:
          "I could not find relevant content in your uploaded study notes for that question yet.",
        retrieved_chunks: [],
      };
    }

    const prompt = buildRagPrompt({
      question,
      chunks,
    });
    console.log("Sending to Ollama");
    const answer = await routeAI(
      "reasoning",
      `${prompt.system}\n\n${prompt.user}`
    );

    return {
      answer,
      retrieved_chunks: chunks.map((chunk) => ({
        note_id: chunk.note_id,
        page_number: chunk.page_number,
        chunk_text: chunk.chunk_text,
        score: Number(chunk.score.toFixed(4)),
      })),
    };
  } catch (error) {
    throw new Error(`RAG note interaction failed: ${error.message}`);
  }
};

export const answerGlobalQuestion = async ({ userId, question }) => {
  try {
    const chunks = await retrieveRelevantChunks({
      userId,
      question,
      limit: Number(process.env.RAG_TOP_K || 5),
    });

    if (!chunks.length) {
      return {
        answer:
          "I could not find relevant content in your offline notes for that question yet. Try uploading more material or rephrasing the question.",
        retrieved_chunks: [],
      };
    }

    const prompt = buildRagPrompt({
      question,
      chunks,
    });

    console.log("Sending to Ollama");
    const answer = await routeAI(
      "reasoning",
      `${prompt.system}\n\n${prompt.user}`
    );

    return {
      answer,
      retrieved_chunks: chunks.map((chunk) => ({
        note_id: chunk.note_id,
        page_number: chunk.page_number,
        chunk_text: chunk.chunk_text,
        score: Number(chunk.score.toFixed(4)),
      })),
    };
  } catch (error) {
    throw new Error(`Global Ask AI failed: ${error.message}`);
  }
};

export const answerSelectedTextQuestion = async ({ question, selectedText, pageNumber }) => {
  try {
    const prompt = buildSelectedTextPrompt({
      question,
      selectedText,
      pageNumber,
    });

    const answer = await routeAI("summarize", `${prompt.system}\n\n${prompt.user}`);

    return {
      answer,
      retrieved_chunks: [],
    };
  } catch (error) {
    throw new Error(`Selected text interaction failed: ${error.message}`);
  }
};
