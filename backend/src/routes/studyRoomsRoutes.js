const express = require("express");

const verifyFirebaseToken = require("../middleware/verifyFirebaseToken");
const studyRoomsController = require("../controllers/studyRoomsController");

const router = express.Router();

router.use(verifyFirebaseToken);

router.get("/", studyRoomsController.getStudyRooms);

module.exports = router;
