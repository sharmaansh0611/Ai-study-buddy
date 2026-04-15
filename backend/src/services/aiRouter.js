import { generateOllamaResponse } from "./ollamaService.js";
import { generateAIResponse } from "./geminiService.js";
import { generateFastAIResponse } from "./fastAIService.js";

const FLASH_LITE_TASKS = new Set([
  "quiz",
  "flashcards",
  "summarize",
  "pdf_processing",
  "structured_json",
]);

const DEEP_REASONING_TASKS = new Set(["reasoning", "chat", "deep_question", "study_room"]);

const generateFallbackResponse = async (taskType, prompt) => {
  if (FLASH_LITE_TASKS.has(taskType)) {
    return await generateFastAIResponse(prompt);
  }

  if (DEEP_REASONING_TASKS.has(taskType)) {
    return await generateAIResponse(prompt);
  }

  return await generateAIResponse(prompt);
};

const generateWithFallback = async (taskType, prompt, primaryGenerator) => {
  try {
    return await primaryGenerator();
  } catch (primaryError) {
    try {
      return await generateFallbackResponse(taskType, prompt);
    } catch (fallbackError) {
      throw new Error(
        `Primary Ollama request failed: ${primaryError.message}. Fallback Gemini request failed: ${fallbackError.message}`
      );
    }
  }
};

export const routeAI = async (taskType, prompt) => {
  if (!prompt?.trim()) {
    throw new Error("prompt is required for AI generation.");
  }

  try {
    if (FLASH_LITE_TASKS.has(taskType)) {
      return await generateWithFallback(taskType, prompt, () => generateOllamaResponse(prompt, { fast: true }));
    }

    if (DEEP_REASONING_TASKS.has(taskType)) {
      return await generateWithFallback(taskType, prompt, () => generateOllamaResponse(prompt));
    }

    return await generateWithFallback(taskType, prompt, () => generateOllamaResponse(prompt));
  } catch (error) {
    throw new Error(`AI routing failed for task "${taskType}": ${error.message}`);
  }
};
