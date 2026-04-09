import { answerGlobalQuestion } from "../services/ragService.js";
import { routeAI } from "../services/aiRouter.js";
import { generateStudyMaterial } from "../services/backgroundWorker.js";

const getStatusCode = (error) =>
  error?.message === "GEMINI_API_KEY is required to use Gemini." ? 500 : 502;

export const askAiChat = async (req, res) => {
  try {
    const message = req.body?.message?.trim();
    const text = req.body?.text?.trim();
    const prompt = req.body?.prompt?.trim();

    if (!message && !text) {
      return res.status(400).json({
        error: "message or text is required",
      });
    }

    if (message && !text) {
      return res.status(503).json({
        error: "General AI assistant is coming soon.",
      });
    }

    if (text) {
      const instruction = prompt || "Explain this text clearly for studying.";
      const normalized = instruction.toLowerCase();
      const taskType = normalized.includes("flashcard")
        ? "flashcards"
        : normalized.includes("summary") ||
            normalized.includes("summarize") ||
            normalized.includes("key point")
          ? "summarize"
          : "reasoning";

      const reply = await routeAI(
        taskType,
        `You are a helpful study assistant.

Text:
${text}

Task:
${instruction}`
      );

      return res.json({ reply });
    }

    return res.status(503).json({
      error: "General AI assistant is coming soon.",
    });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const askGlobalAi = async (req, res) => {
  try {
    const question = req.body?.message?.trim() || req.body?.question?.trim();

    if (!question) {
      return res.status(400).json({
        error: "question is required",
      });
    }

    const result = await answerGlobalQuestion({
      userId: req.user.uid,
      question,
    });

    return res.json({
      reply: result.answer,
      retrieved_chunks: result.retrieved_chunks,
    });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const generateLiveNotes = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const reply = await routeAI(
      "summarize",
      `Generate live study notes from the following text.

Text:
${text}`
    );

    return res.json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const summarizeText = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const reply = await routeAI(
      "summarize",
      `Generate a concise summary from the following text.

Text:
${text}`
    );

    return res.json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const generateKeyPoints = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const reply = await routeAI(
      "summarize",
      `Extract bullet-point key insights from the following text.

Text:
${text}`
    );

    return res.json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const generateStructuredStudyMaterial = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const result = await generateStudyMaterial(text);
    return res.json(result);
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const generateQuiz = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const result = await generateStudyMaterial(text);
    return res.json({
      quiz: result.quiz || [],
    });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const generateFlashcards = async (req, res) => {
  try {
    const text = req.body?.text?.trim();

    if (!text) {
      return res.status(400).json({
        error: "text is required",
      });
    }

    const result = await generateStudyMaterial(text);
    return res.json({
      flashcards: result.flashcards || [],
    });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};

export const analyzePerformance = async (req, res) => {
  try {
    const quizResults = req.body?.quiz_results;

    if (!Array.isArray(quizResults) || !quizResults.length) {
      return res.status(400).json({
        error: "quiz_results is required",
      });
    }

    const formattedResults = quizResults
      .map((result, index) => {
        const topic = result?.topic || `Topic ${index + 1}`;
        const score = result?.score ?? "unknown";
        const total = result?.total ?? "unknown";
        return `- Topic: ${topic}, Score: ${score}/${total}`;
      })
      .join("\n");

    const reply = await routeAI(
      "reasoning",
      `You are an academic coach. Analyze these quiz results, identify weak topics, and recommend what to revise next.

Quiz results:
${formattedResults}

Return concise JSON in this format:
{
  "weak_topics": ["", ""],
  "recommendations": ["", ""]
}`
    );

    return res.json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(getStatusCode(error)).json({
      error: "Internal Server Error",
      details: error.message,
    });
  }
};
