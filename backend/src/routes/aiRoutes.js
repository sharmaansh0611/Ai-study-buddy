const express = require("express");

const verifyFirebaseToken = require("../middleware/verifyFirebaseToken");
const aiController = require("../controllers/aiController");

const router = express.Router();

router.use(verifyFirebaseToken);

router.post("/ask", aiController.askAi);

module.exports = router;
