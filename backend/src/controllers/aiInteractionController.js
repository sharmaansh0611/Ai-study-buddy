import Note from "../models/Note.js";
import { answerNoteQuestion, answerSelectedTextQuestion } from "../services/ragService.js";

export const interactWithNote = async (req, res) => {
  const noteId = req.body?.note_id?.trim() || "";
  const question = req.body?.question?.trim();
  const selectedText = req.body?.selected_text?.trim() || "";
  const pageNumber = Number(req.body?.page_number || 0);

  if (!question) {
    return res.status(400).json({
      message: "Question is required.",
    });
  }

  try {
    if (noteId) {
      const note = await Note.findOne({
        note_id: noteId,
        user_id: req.user.uid,
      }).lean();

      if (!note) {
        return res.status(404).json({
          message: "Note not found.",
        });
      }
    }

    const result = selectedText
      ? await answerSelectedTextQuestion({
          question,
          selectedText,
          pageNumber: Number.isFinite(pageNumber) && pageNumber > 0 ? pageNumber : undefined,
        })
      : await answerNoteQuestion({
          userId: req.user.uid,
          noteId: noteId || undefined,
          question,
        });

    return res.json({
      answer: result.answer,
      retrieved_chunks: result.retrieved_chunks,
    });
  } catch (error) {
    if (error?.status && error.status >= 400 && error.status < 600) {
      return res.status(502).json({
        message: "The AI service failed to generate a response.",
        error: error.message,
      });
    }

    return res.status(500).json({
      message: "Failed to process note interaction.",
      error: error.message,
    });
  }
};
