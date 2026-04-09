import { Router } from "express";

import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";
import { getStudyRooms } from "../controllers/studyRoomsController.js";

const router = Router();

router.use(verifyFirebaseToken);
router.get("/", getStudyRooms);

export default router;
