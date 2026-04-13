const DEFAULT_CHUNK_SIZE = 450;
const DEFAULT_CHUNK_OVERLAP = 80;

const normalizePageText = (text = "") => text.replace(/\s+/g, " ").trim();
const sectionPattern = /(?:^|\s)(\d+(?:\.\d+)+)(?=\s|$)/g;

const splitPageIntoBlocks = (pageText = "") => {
  const normalized = pageText.replace(/\r\n/g, "\n");
  const paragraphCandidates = normalized
    .split(/\n{2,}/)
    .map((paragraph) => normalizePageText(paragraph))
    .filter(Boolean);

  if (paragraphCandidates.length > 1) {
    return paragraphCandidates;
  }

  return normalized
    .split(/(?<=\.)\s+(?=[A-Z0-9])/)
    .map((sentenceGroup) => normalizePageText(sentenceGroup))
    .filter(Boolean);
};

const extractSectionRefs = (text = "") => {
  const refs = new Set();
  for (const match of text.matchAll(sectionPattern)) {
    refs.add(match[1]);
  }
  return [...refs];
};

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
    const blocks = splitPageIntoBlocks(page_text);
    let currentChunk = "";

    blocks.forEach((block) => {
      const proposedChunk = currentChunk ? `${currentChunk} ${block}` : block;

      if (proposedChunk.length <= effectiveChunkSize) {
        currentChunk = proposedChunk;
        return;
      }

      if (currentChunk) {
        chunks.push({
          note_id: noteId,
          page_number,
          chunk_index: chunkIndex,
          chunk_text: currentChunk,
          section_refs: extractSectionRefs(currentChunk),
        });
        chunkIndex += 1;
      }

      if (block.length <= effectiveChunkSize) {
        currentChunk = block;
        return;
      }

      for (let start = 0; start < block.length; start += step) {
        const chunkText = block.slice(start, start + effectiveChunkSize).trim();

        if (!chunkText) {
          continue;
        }

        chunks.push({
          note_id: noteId,
          page_number,
          chunk_index: chunkIndex,
          chunk_text: chunkText,
          section_refs: extractSectionRefs(chunkText),
        });
        chunkIndex += 1;

        if (start + effectiveChunkSize >= block.length) {
          currentChunk = "";
          break;
        }
      }
    });

    if (currentChunk) {
      chunks.push({
        note_id: noteId,
        page_number,
        chunk_index: chunkIndex,
        chunk_text: currentChunk,
        section_refs: extractSectionRefs(currentChunk),
      });
      chunkIndex += 1;
    }
  });

  return chunks;
};
