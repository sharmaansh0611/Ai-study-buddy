import { routeAI } from "./aiRouter.js";

const extractJson = (rawText) => {
  const trimmed = rawText.trim();

  if (trimmed.startsWith("```")) {
    return trimmed.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, "");
  }

  return trimmed;
};

const parseStructuredResponse = (rawText, label) => {
  try {
    return JSON.parse(extractJson(rawText));
  } catch (error) {
    throw new Error(`Failed to parse ${label} response as JSON.`);
  }
};

const buildFlashcardsPrompt = (text) => `Generate flashcards from the following study material.
Return only valid JSON in this exact format:
[
  { "question": "", "answer": "" }
]

Study material:
${text}`;

const buildQuizPrompt = (text) => `Generate 5 multiple-choice questions from the following study material.
Return only valid JSON in this exact format:
[
  {
    "question": "",
    "options": ["", "", "", ""],
    "answer": ""
  }
]

Study material:
${text}`;

export const generateStudyMaterial = async (text) => {
  if (!text?.trim()) {
    throw new Error("text is required to generate study material.");
  }

  try {
    const [flashcardsResponse, quizResponse] = await Promise.all([
      routeAI("flashcards", buildFlashcardsPrompt(text)),
      routeAI("quiz", buildQuizPrompt(text)),
    ]);

    return {
      flashcards: parseStructuredResponse(flashcardsResponse, "flashcards"),
      quiz: parseStructuredResponse(quizResponse, "quiz"),
    };
  } catch (error) {
    throw new Error(`Study material generation failed: ${error.message}`);
  }
};
