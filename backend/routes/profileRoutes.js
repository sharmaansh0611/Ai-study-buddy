import { Router } from "express";

import { getProfile } from "../controllers/profileController.js";
import { authenticateUser } from "../middleware/authMiddleware.js";

const router = Router();

router.get("/", authenticateUser, getProfile);

export default router;
