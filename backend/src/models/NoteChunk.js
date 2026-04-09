import mongoose from "mongoose";

const noteChunkSchema = new mongoose.Schema(
  {
    chunk_id: {
      type: String,
      required: true,
      unique: true,
      default: () => new mongoose.Types.ObjectId().toString(),
    },
    user_id: {
      type: String,
      required: true,
      index: true,
    },
    note_id: {
      type: String,
      required: true,
      index: true,
    },
    page_number: {
      type: Number,
      required: true,
      default: 1,
    },
    chunk_index: {
      type: Number,
      required: true,
    },
    chunk_text: {
      type: String,
      required: true,
    },
    embedding: {
      type: [Number],
      required: true,
    },
    created_at: {
      type: Date,
      default: Date.now,
    },
  },
  {
    versionKey: false,
  }
);

noteChunkSchema.index({ user_id: 1, note_id: 1, chunk_index: 1 });

const NoteChunk =
  mongoose.models.NoteChunk || mongoose.model("NoteChunk", noteChunkSchema);

export default NoteChunk;
