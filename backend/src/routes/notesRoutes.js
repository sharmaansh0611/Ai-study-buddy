const express = require("express");

const verifyFirebaseToken = require("../middleware/verifyFirebaseToken");
const notesController = require("../controllers/notesController");

const router = express.Router();

router.use(verifyFirebaseToken);

router.get("/", notesController.getUserNotes);
router.post("/upload", notesController.uploadNote);

module.exports = router;
