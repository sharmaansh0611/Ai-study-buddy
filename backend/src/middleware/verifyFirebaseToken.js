import admin from "../../config/firebase.js";

const verifyFirebaseToken = async (req, res, next) => {
  const authorizationHeader = req.headers.authorization;

  if (!authorizationHeader || !authorizationHeader.startsWith("Bearer ")) {
    return res.status(401).json({ message: "Unauthorized: missing bearer token." });
  }

  const token = authorizationHeader.substring("Bearer ".length).trim();

  if (!token) {
    return res.status(401).json({ message: "Unauthorized: Firebase ID token is required." });
  }

  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    return res.status(401).json({ message: "Unauthorized: invalid Firebase ID token." });
  }
};

export default verifyFirebaseToken;
