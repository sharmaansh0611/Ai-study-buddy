import fs from "fs";
import path from "path";

import multer from "multer";

const uploadDirectory = path.resolve("backend/src/uploads");

if (!fs.existsSync(uploadDirectory)) {
  fs.mkdirSync(uploadDirectory, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, uploadDirectory);
  },
  filename: (req, file, cb) => {
    const sanitizedName = file.originalname.replace(/\s+/g, "_");
    cb(null, `${Date.now()}_${sanitizedName}`);
  },
});

const upload = multer({
  storage,
  limits: {
    fileSize: 10 * 1024 * 1024,
  },
  fileFilter: (_req, file, cb) => {
    const isPdfMimeType = file.mimetype === "application/pdf";
    const hasPdfExtension = file.originalname.toLowerCase().endsWith(".pdf");

    if (isPdfMimeType && hasPdfExtension) {
      cb(null, true);
      return;
    }

    cb(new Error("Only PDF files are allowed."));
  },
});

export default upload;
