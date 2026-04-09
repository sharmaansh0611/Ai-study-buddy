import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

import multer from "multer";

import Note from "../models/Note.js";
import NoteChunk from "../models/NoteChunk.js";
import { indexNoteById } from "../services/noteIndexingService.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const uploadsDirectory = path.resolve(__dirname, "../uploads");
const INDEX_RETRY_DELAYS_MS = [0, 2000, 5000];

const startBackgroundIndexing = ({ noteId }) => {
  const attemptIndexing = async (attemptIndex) => {
    try {
      await indexNoteById(noteId, { force: attemptIndex > 0 });
    } catch (error) {
      console.error(
        `Background note indexing failed for note ${noteId} on attempt ${attemptIndex + 1}:`,
        error.message
      );

      const nextAttemptIndex = attemptIndex + 1;
      if (nextAttemptIndex >= INDEX_RETRY_DELAYS_MS.length) {
        return;
      }

      const retryDelay = INDEX_RETRY_DELAYS_MS[nextAttemptIndex];
      console.log(`Retrying note indexing for ${noteId} in ${retryDelay}ms`);
      setTimeout(() => {
        attemptIndexing(nextAttemptIndex).catch(() => null);
      }, retryDelay);
    }
  };

  setImmediate(() => {
    attemptIndexing(0).catch(() => null);
  });
};

export const uploadNote = async (req, res) => {
  const uploadedFile = req.file;
  const noteTitle = req.body?.title?.trim();
  let notePersisted = false;

  if (!uploadedFile) {
    return res.status(400).json({ message: "PDF file is required." });
  }

  if (!noteTitle) {
    await fs.unlink(uploadedFile.path).catch(() => null);
    return res.status(400).json({ message: "Note title is required." });
  }

  try {
    const note = await Note.create({
      user_id: req.user.uid,
      title: noteTitle,
      file_url: `/uploads/${uploadedFile.filename}`,
      text_content: "",
      indexed: false,
      indexing: false,
      index_status: "pending",
    });
    notePersisted = true;
    console.log("PDF uploaded");

    startBackgroundIndexing({
      noteId: note.note_id,
    });

    return res.status(201).json({
      message: "PDF uploaded successfully.",
      note_id: note.note_id,
      title: note.title,
      file_url: note.file_url,
      index_status: "pending",
    });
  } catch (error) {
    if (req.file && !notePersisted) {
      await fs.unlink(uploadedFile.path).catch(() => null);
    }
    return res.status(500).json({
      message: "Failed to upload and process PDF.",
      error: error.message,
    });
  }
};

export const deleteNote = async (req, res) => {
  try {
    const note = await Note.findOne({
      note_id: req.params.id,
      user_id: req.user.uid,
    });

    if (!note) {
      return res.status(404).json({
        message: "Note not found.",
      });
    }

    const noteFileName = path.basename(note.file_url);
    const noteFilePath = path.join(uploadsDirectory, noteFileName);

    await NoteChunk.deleteMany({
      note_id: note.note_id,
      user_id: req.user.uid,
    });

    await Note.deleteOne({
      note_id: note.note_id,
      user_id: req.user.uid,
    });

    await fs.unlink(noteFilePath).catch(() => null);

    return res.json({
      message: "Note deleted successfully.",
      note_id: note.note_id,
    });
  } catch (error) {
    return res.status(500).json({
      message: "Failed to delete note.",
      error: error.message,
    });
  }
};

export const getMyNotes = async (req, res) => {
  try {
    const notes = await Note.find({ user_id: req.user.uid })
      .sort({ created_at: -1 })
      .lean();

    return res.json({
      notes: notes.map((note) => ({
        note_id: note.note_id,
        title: note.title,
        file_url: note.file_url,
        indexed: note.indexed,
        indexing: note.indexing,
        index_status: note.index_status,
        created_at: note.created_at,
      })),
    });
  } catch (error) {
    return res.status(500).json({
      message: "Failed to fetch notes.",
      error: error.message,
    });
  }
};

export const handleUploadError = (error, _req, res, next) => {
  if (error instanceof multer.MulterError) {
    if (error.code === "LIMIT_FILE_SIZE") {
      return res.status(400).json({ message: "PDF must be 10MB or smaller." });
    }

    return res.status(400).json({ message: error.message });
  }

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return next();
};
