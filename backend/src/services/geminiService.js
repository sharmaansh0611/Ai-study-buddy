import { GoogleGenerativeAI } from "@google/generative-ai";

const DEFAULT_GEMINI_MODEL = "gemini-3.1-pro-preview";
const GEMINI_MODEL = process.env.GEMINI_MODEL || DEFAULT_GEMINI_MODEL;
const GEMINI_TEMPERATURE = Number(process.env.GEMINI_TEMPERATURE ?? process.env.AI_TEMPERATURE);

let cachedModel;

const getGeminiModel = () => {
  if (cachedModel) {
    return cachedModel;
  }

  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey) {
    throw new Error("GEMINI_API_KEY is required to use Gemini.");
  }

  const client = new GoogleGenerativeAI(apiKey);
  cachedModel = client.getGenerativeModel({
    model: GEMINI_MODEL,
    generationConfig: Number.isFinite(GEMINI_TEMPERATURE)
      ? { temperature: GEMINI_TEMPERATURE }
      : undefined,
  });

  return cachedModel;
};

export const generateAIResponse = async (prompt) => {
  try {
    const model = getGeminiModel();
    const result = await model.generateContent(prompt);
    const response = await result.response;
    const reply = response.text()?.trim();

    if (!reply) {
      throw new Error("Gemini returned an empty response.");
    }

    return reply;
  } catch (error) {
    throw new Error(`Gemini reasoning request failed: ${error.message}`);
  }
};
