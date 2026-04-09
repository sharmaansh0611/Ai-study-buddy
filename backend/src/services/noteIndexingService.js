import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

import pdfParse from "pdf-parse";

import Note from "../models/Note.js";
import { extractTextWithOcr } from "./ocrService.js";
import { indexNoteChunks } from "./vectorStoreService.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const uploadsDirectory = path.resolve(__dirname, "../uploads");

const getNoteFileBuffer = async (fileUrl) => {
  const fileName = path.basename(fileUrl);
  const filePath = path.join(uploadsDirectory, fileName);
  return fs.readFile(filePath);
};

export const indexNoteById = async (noteId, { force = false } = {}) => {
  const note = await Note.findOne({ note_id: noteId });

  if (!note) {
    throw new Error(`Note ${noteId} not found for indexing.`);
  }

  if (!force && (note.indexed || note.indexing)) {
    return note;
  }

  if (force) {
    note.indexed = false;
    note.indexed_at = null;
  }

  note.indexing = true;
  note.index_status = "pending";
  note.last_index_error = null;
  await note.save();

  try {
    console.log("Indexing started");

    const fileBuffer = await getNoteFileBuffer(note.file_url);
    const parsedPdf = await pdfParse(fileBuffer);
    let extractedText = parsedPdf.text?.trim() || "";

    if (!extractedText) {
      extractedText = await extractTextWithOcr(fileBuffer);
    }

    console.log("Text extracted");

    note.text_content = extractedText;
    await note.save();

    await indexNoteChunks(note);

    note.indexed = true;
    note.indexing = false;
    note.index_status = "ready";
    note.indexed_at = new Date();
    note.last_index_error = null;
    await note.save();

    console.log("Indexing completed");
    return note;
  } catch (error) {
    note.indexed = false;
    note.indexing = false;
    note.index_status = "failed";
    note.last_index_error = error.message;
    await note.save();
    throw error;
  }
};

export const resumeIncompleteIndexing = async () => {
  const notesToRecover = await Note.find({
    $or: [{ indexing: true }, { index_status: "pending" }, { index_status: "failed" }],
  })
    .sort({ created_at: 1 })
    .lean();

  if (!notesToRecover.length) {
    console.log("No incomplete note indexing jobs found.");
    return;
  }

  console.log(`Resuming indexing for ${notesToRecover.length} notes...`);

  for (const note of notesToRecover) {
    try {
      await indexNoteById(note.note_id, { force: true });
    } catch (error) {
      console.error(`Resume indexing failed for note ${note.note_id}:`, error.message);
    }
  }
};
