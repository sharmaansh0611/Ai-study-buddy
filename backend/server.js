import "dotenv/config";

import cors from "cors";
import express from "express";
import path from "path";
import { fileURLToPath } from "url";

import profileRoutes from "./routes/profileRoutes.js";
import connectDB from "./src/config/db.js";
import aiRoutes from "./src/routes/aiRoutes.js";
import aiInteractionRoutes from "./src/routes/aiInteractionRoutes.js";
import healthRoutes from "./src/routes/healthRoutes.js";
import noteRoutes from "./src/routes/noteRoutes.js";
import notesRoutes from "./src/routes/notesRoutes.js";
import plannerRoutes from "./src/routes/plannerRoutes.js";
import { resumeIncompleteIndexing } from "./src/services/noteIndexingService.js";
import studyRoomsRoutes from "./src/routes/studyRoomsRoutes.js";

const app = express();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

await connectDB();

app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(path.join(__dirname, "src/uploads")));

app.get("/health", (_req, res) => {
  res.json({ status: "Server running" });
});

app.use("/profile", profileRoutes);
app.use("/api", healthRoutes);
app.use("/api/ai", aiRoutes);
app.use("/api/notes", noteRoutes);
app.use("/api", plannerRoutes);
app.use("/api/study-rooms", studyRoomsRoutes);
app.use("/api", notesRoutes);
app.use("/api", aiInteractionRoutes);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  setImmediate(() => {
    resumeIncompleteIndexing().catch((error) => {
      console.error("Failed to resume incomplete indexing jobs:", error.message);
    });
  });
});
