import "dotenv/config";

import connectDB from "../config/db.js";
import Note from "../models/Note.js";
import { indexNoteById } from "../services/noteIndexingService.js";

const run = async () => {
  await connectDB();

  const notes = await Note.find({}).sort({ created_at: 1 }).lean();

  if (!notes.length) {
    console.log("No notes found to reindex.");
    process.exit(0);
  }

  console.log(`Starting offline reindex for ${notes.length} notes...`);

  let successCount = 0;
  let failureCount = 0;

  for (const note of notes) {
    try {
      console.log(`Reindexing note ${note.note_id} (${note.title})`);
      await indexNoteById(note.note_id, { force: true });
      successCount += 1;
      console.log(`Finished note ${note.note_id}`);
    } catch (error) {
      failureCount += 1;
      console.error(`Failed note ${note.note_id}:`, error.message);
    }
  }

  console.log(
    JSON.stringify(
      {
        total: notes.length,
        successCount,
        failureCount,
      },
      null,
      2
    )
  );

  process.exit(failureCount > 0 ? 1 : 0);
};

run().catch((error) => {
  console.error("Offline reindex failed:", error);
  process.exit(1);
});
