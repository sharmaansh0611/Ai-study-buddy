import { Router } from "express";

import { interactWithNote } from "../controllers/aiInteractionController.js";
import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";

const router = Router();

router.post("/note-interaction", verifyFirebaseToken, interactWithNote);

export default router;
