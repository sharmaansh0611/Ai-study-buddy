import { Router } from "express";

import verifyFirebaseToken from "../middleware/verifyFirebaseToken.js";
import { createEvent, getEvents } from "../controllers/plannerController.js";

const router = Router();

router.use(verifyFirebaseToken);
router.post("/create-event", createEvent);
router.get("/events", getEvents);

export default router;
