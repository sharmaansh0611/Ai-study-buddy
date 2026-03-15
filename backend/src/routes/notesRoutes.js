import { Router } from "express";

import {
  getMyNotes,
  handleUploadError,
  uploadNote,
} from "../controllers/notesController.js";
import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";
import upload from "../utils/upload.js";

const router = Router();

router.get("/my-notes", verifyFirebaseToken, getMyNotes);
router.post(
  "/upload-note",
  verifyFirebaseToken,
  upload.single("file"),
  handleUploadError,
  uploadNote
);

export default router;
