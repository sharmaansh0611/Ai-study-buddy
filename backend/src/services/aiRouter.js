import { generateOllamaResponse } from "./ollamaService.js";

const FLASH_LITE_TASKS = new Set([
  "quiz",
  "flashcards",
  "summarize",
  "pdf_processing",
  "structured_json",
]);

const DEEP_REASONING_TASKS = new Set(["reasoning", "chat", "deep_question", "study_room"]);

export const routeAI = async (taskType, prompt) => {
  if (!prompt?.trim()) {
    throw new Error("prompt is required for AI generation.");
  }

  try {
    if (FLASH_LITE_TASKS.has(taskType)) {
      return await generateOllamaResponse(prompt, { fast: true });
    }

    if (DEEP_REASONING_TASKS.has(taskType)) {
      return await generateOllamaResponse(prompt);
    }

    return await generateOllamaResponse(prompt);
  } catch (error) {
    throw new Error(`AI routing failed for task "${taskType}": ${error.message}`);
  }
};
