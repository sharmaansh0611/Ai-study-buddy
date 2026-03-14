const admin = require("../config/firebaseAdmin");

async function verifyFirebaseToken(req, res, next) {
  const authorizationHeader = req.headers.authorization;

  if (!authorizationHeader || !authorizationHeader.startsWith("Bearer ")) {
    return res.status(401).json({ message: "Missing or invalid Authorization header." });
  }

  const idToken = authorizationHeader.split("Bearer ")[1]?.trim();
  if (!idToken) {
    return res.status(401).json({ message: "Firebase ID token is required." });
  }

  try {
    // Verify the Firebase ID token and expose the decoded identity to downstream handlers.
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email || null,
      name: decodedToken.name || null,
    };
    next();
  } catch (error) {
    console.error("Firebase token verification failed:", error.message);
    return res.status(401).json({ message: "Unauthorized: invalid Firebase token." });
  }
}

module.exports = verifyFirebaseToken;
