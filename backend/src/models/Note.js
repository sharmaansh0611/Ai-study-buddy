import mongoose from "mongoose";

const noteSchema = new mongoose.Schema(
  {
    note_id: {
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
    title: {
      type: String,
      required: true,
      trim: true,
    },
    file_url: {
      type: String,
      required: true,
    },
    text_content: {
      type: String,
      default: "",
    },
    indexed: {
      type: Boolean,
      default: false,
      index: true,
    },
    indexing: {
      type: Boolean,
      default: false,
      index: true,
    },
    index_status: {
      type: String,
      enum: ["pending", "ready", "failed"],
      default: "pending",
      index: true,
    },
    indexed_at: {
      type: Date,
      default: null,
    },
    last_index_error: {
      type: String,
      default: null,
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

const Note = mongoose.models.Note || mongoose.model("Note", noteSchema);

export default Note;
