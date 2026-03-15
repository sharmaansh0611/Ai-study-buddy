import fs from "fs/promises";

import multer from "multer";
import pdfParse from "pdf-parse";

import Note from "../models/Note.js";

export const uploadNote = async (req, res) => {
  const uploadedFile = req.file;
  const noteTitle = req.body?.title?.trim();

  if (!uploadedFile) {
    return res.status(400).json({ message: "PDF file is required." });
  }

  if (!noteTitle) {
    await fs.unlink(uploadedFile.path).catch(() => null);
    return res.status(400).json({ message: "Note title is required." });
  }

  try {
    // Extract the full PDF text so future AI and quiz features can work off stored content.
    const fileBuffer = await fs.readFile(uploadedFile.path);
    const parsedPdf = await pdfParse(fileBuffer);

    const note = await Note.create({
      user_id: req.user.uid,
      title: noteTitle,
      file_url: `/uploads/${uploadedFile.filename}`,
      text_content: parsedPdf.text?.trim() || "",
    });

    return res.status(201).json({
      message: "Note uploaded successfully",
      note_id: note.note_id,
      title: note.title,
      file_url: note.file_url,
    });
  } catch (error) {
    await fs.unlink(uploadedFile.path).catch(() => null);
    return res.status(500).json({
      message: "Failed to upload and process PDF.",
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
