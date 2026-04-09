const DEFAULT_CHUNK_SIZE = 700;
const DEFAULT_CHUNK_OVERLAP = 100;

const normalizePageText = (text = "") => text.replace(/\s+/g, " ").trim();

export const splitTextIntoPages = (text = "") => {
  const normalized = text.replace(/\r\n/g, "\n").trim();

  if (!normalized) {
    return [];
  }

  const rawPages = normalized.includes("\f")
    ? normalized.split("\f")
    : [normalized];

  return rawPages
    .map((pageText) => normalizePageText(pageText))
    .filter(Boolean)
    .map((pageText, index) => ({
      page_number: index + 1,
      page_text: pageText,
    }));
};

export const chunkNoteText = ({
  noteId,
  text,
  chunkSize = Number(process.env.RAG_CHUNK_SIZE || DEFAULT_CHUNK_SIZE),
  overlap = Number(process.env.RAG_CHUNK_OVERLAP || DEFAULT_CHUNK_OVERLAP),
}) => {
  const effectiveChunkSize =
    Number.isFinite(chunkSize) && chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
  const effectiveOverlap =
    Number.isFinite(overlap) && overlap >= 0 && overlap < effectiveChunkSize
      ? overlap
      : DEFAULT_CHUNK_OVERLAP;

  const step = Math.max(1, effectiveChunkSize - effectiveOverlap);
  const pages = splitTextIntoPages(text);
  const chunks = [];
  let chunkIndex = 0;

  pages.forEach(({ page_number, page_text }) => {
    // Character-window chunking keeps the implementation simple and predictable for note uploads.
    for (let start = 0; start < page_text.length; start += step) {
      const chunkText = page_text.slice(start, start + effectiveChunkSize).trim();

      if (!chunkText) {
        continue;
      }

      chunks.push({
        note_id: noteId,
        page_number,
        chunk_index: chunkIndex,
        chunk_text: chunkText,
      });

      chunkIndex += 1;

      if (start + effectiveChunkSize >= page_text.length) {
        break;
      }
    }
  });

  return chunks;
};
