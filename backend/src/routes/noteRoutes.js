import { Router } from "express";

import {
  deleteNote,
  getMyNotes,
  handleUploadError,
  uploadNote,
} from "../controllers/notesController.js";
import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";
import upload from "../utils/upload.js";

const router = Router();

router.get("/", verifyFirebaseToken, getMyNotes);
router.delete("/:id", verifyFirebaseToken, deleteNote);
router.post("/upload", verifyFirebaseToken, upload.single("pdf"), handleUploadError, uploadNote);

export default router;
