import { Router } from "express";

import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";
import {
  askAiChat,
  askGlobalAi,
  analyzePerformance,
  generateFlashcards,
  generateKeyPoints,
  generateLiveNotes,
  generateQuiz,
  generateStructuredStudyMaterial,
  summarizeText,
} from "../controllers/aiController.js";

const router = Router();

router.post("/chat", verifyFirebaseToken, askAiChat);
router.post("/ask", verifyFirebaseToken, askGlobalAi);
router.post("/ask-ai", verifyFirebaseToken, askAiChat);
router.post("/chat-notes", verifyFirebaseToken, askGlobalAi);
router.post("/live-notes", verifyFirebaseToken, generateLiveNotes);
router.post("/summarize", verifyFirebaseToken, summarizeText);
router.post("/summarize-note", verifyFirebaseToken, summarizeText);
router.post("/keypoints", verifyFirebaseToken, generateKeyPoints);
router.post("/generate-study-material", verifyFirebaseToken, generateStructuredStudyMaterial);
router.post("/generate-quiz", verifyFirebaseToken, generateQuiz);
router.post("/generate-flashcards", verifyFirebaseToken, generateFlashcards);
router.post("/analyze-performance", verifyFirebaseToken, analyzePerformance);

export default router;
