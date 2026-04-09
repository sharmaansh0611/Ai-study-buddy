import NoteChunk from "../models/NoteChunk.js";
import { createEmbedding, createEmbeddings } from "./embeddingService.js";
import { chunkNoteText } from "./textChunker.js";

const VECTOR_SEARCH_INDEX =
  process.env.MONGODB_VECTOR_INDEX_NAME || "note_chunks_vector_index";
const VECTOR_SEARCH_NUM_CANDIDATES = Number(
  process.env.MONGODB_VECTOR_NUM_CANDIDATES || 100
);
const USE_VECTOR_SEARCH = process.env.MONGODB_USE_VECTOR_SEARCH === "true";

const tokenizeQuestion = (question = "") =>
  question
    .toLowerCase()
    .split(/[^a-z0-9]+/i)
    .map((token) => token.trim())
    .filter((token) => token.length > 2);

const scoreByKeywordOverlap = (chunkText = "", tokens = []) => {
  if (!tokens.length) {
    return 0;
  }

  const lowerChunkText = chunkText.toLowerCase();
  return tokens.reduce((score, token) => {
    if (!lowerChunkText.includes(token)) {
      return score;
    }

    const occurrences = lowerChunkText.split(token).length - 1;
    return score + occurrences;
  }, 0);
};

const cosineSimilarity = (vectorA = [], vectorB = []) => {
  if (!vectorA.length || !vectorB.length || vectorA.length !== vectorB.length) {
    return 0;
  }

  let dotProduct = 0;
  let magnitudeA = 0;
  let magnitudeB = 0;

  for (let index = 0; index < vectorA.length; index += 1) {
    dotProduct += vectorA[index] * vectorB[index];
    magnitudeA += vectorA[index] ** 2;
    magnitudeB += vectorB[index] ** 2;
  }

  if (!magnitudeA || !magnitudeB) {
    return 0;
  }

  return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
};

export const indexNoteChunks = async (note) => {
  console.log("Starting note indexing");

  const chunks = chunkNoteText({
    noteId: note.note_id,
    text: note.text_content,
  });

  await NoteChunk.deleteMany({
    note_id: note.note_id,
    user_id: note.user_id,
  });

  if (!chunks.length) {
    return [];
  }

  console.log("Chunks created");
  console.log(`Chunk count: ${chunks.length}`);
  const embeddings = await createEmbeddings(chunks.map((chunk) => chunk.chunk_text));
  console.log("Embeddings generated");

  const chunkDocuments = chunks.map((chunk, index) => ({
    ...chunk,
    user_id: note.user_id,
    embedding: embeddings[index],
  }));

  try {
    await NoteChunk.insertMany(chunkDocuments);
  } catch (error) {
    throw new Error(`Failed to store note chunks: ${error.message}`);
  }

  return chunkDocuments;
};

export const retrieveRelevantChunks = async ({
  userId,
  question,
  noteId,
  limit = Number(process.env.RAG_TOP_K || 5),
}) => {
  console.log("Vector search started");
  const chunkQuery = {
    user_id: userId,
  };

  if (noteId) {
    chunkQuery.note_id = noteId;
  }

  if (USE_VECTOR_SEARCH) {
    try {
      const queryEmbedding = await createEmbedding(question);
      const vectorFilter = noteId
        ? { user_id: userId, note_id: noteId }
        : { user_id: userId };

      const results = await NoteChunk.aggregate([
        {
          $vectorSearch: {
            index: VECTOR_SEARCH_INDEX,
            path: "embedding",
            queryVector: queryEmbedding,
            numCandidates: VECTOR_SEARCH_NUM_CANDIDATES,
            limit,
            filter: vectorFilter,
          },
        },
        {
          $project: {
            _id: 0,
            note_id: 1,
            page_number: 1,
            chunk_text: 1,
            score: { $meta: "vectorSearchScore" },
          },
        },
      ]);

      console.log("Chunks retrieved");
      console.log(`Vector search results: ${results.length}`);

      return results;
    } catch (error) {
      console.error(
        "Atlas vector search failed, falling back to local retrieval:",
        error.message
      );
    }
  } else {
    console.log("MongoDB Atlas vector search disabled, using local retrieval.");
  }

  const candidateChunks = await NoteChunk.find(chunkQuery).lean();

  if (!candidateChunks.length) {
    console.log("Vector search results: 0");
    return [];
  }

  try {
    const queryEmbedding = await createEmbedding(question);
    const localVectorResults = candidateChunks
      .map((chunk) => ({
        ...chunk,
        score: cosineSimilarity(queryEmbedding, chunk.embedding),
      }))
      .sort((left, right) => right.score - left.score)
      .slice(0, limit);

    console.log("Chunks retrieved");
    console.log(`Local vector fallback results: ${localVectorResults.length}`);

    return localVectorResults;
  } catch (embeddingError) {
    console.error(
      "Vector embedding search failed, falling back to keyword retrieval:",
      embeddingError.message
    );

    const questionTokens = tokenizeQuestion(question);
    const fallbackResults = candidateChunks
      .map((chunk) => ({
        ...chunk,
        score: scoreByKeywordOverlap(chunk.chunk_text, questionTokens),
      }))
      .filter((chunk) => chunk.score > 0)
      .sort((left, right) => right.score - left.score)
      .slice(0, limit);

    console.log("Chunks retrieved");
    console.log(`Keyword fallback results: ${fallbackResults.length}`);

    return fallbackResults;
  }
};
