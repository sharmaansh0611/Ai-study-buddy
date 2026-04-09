import { createCanvas } from "@napi-rs/canvas";
import * as pdfjsLib from "pdfjs-dist/legacy/build/pdf.mjs";
import Tesseract from "tesseract.js";

const OCR_RENDER_SCALE = Number(process.env.OCR_RENDER_SCALE || 2);
const OCR_MAX_PAGES = Number(process.env.OCR_MAX_PAGES || 25);

const renderPageToImage = async (page) => {
  const viewport = page.getViewport({ scale: OCR_RENDER_SCALE });
  const canvas = createCanvas(viewport.width, viewport.height);
  const context = canvas.getContext("2d");

  await page.render({
    canvasContext: context,
    viewport,
  }).promise;

  return canvas.toBuffer("image/png");
};

export const extractTextWithOcr = async (fileBuffer) => {
  const loadingTask = pdfjsLib.getDocument({
    data: new Uint8Array(fileBuffer),
    useSystemFonts: true,
    isEvalSupported: false,
  });
  const pdfDocument = await loadingTask.promise;
  const pageCount = Math.min(pdfDocument.numPages, OCR_MAX_PAGES);
  const pageTexts = [];

  for (let pageIndex = 1; pageIndex <= pageCount; pageIndex += 1) {
    const page = await pdfDocument.getPage(pageIndex);
    const imageBuffer = await renderPageToImage(page);
    const result = await Tesseract.recognize(imageBuffer, "eng");
    const pageText = result.data.text?.replace(/\s+/g, " ").trim();

    if (pageText) {
      pageTexts.push(pageText);
    }
  }

  return pageTexts.join("\f").trim();
};
