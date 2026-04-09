import { GoogleGenerativeAI } from "@google/generative-ai";

const GEMINI_MODEL = "gemini-3.1-pro-preview";

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
  cachedModel = client.getGenerativeModel({ model: GEMINI_MODEL });

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
