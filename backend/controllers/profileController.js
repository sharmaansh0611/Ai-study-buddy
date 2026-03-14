export const getProfile = (req, res) => {
  return res.json({
    message: "User authenticated",
    uid: req.user.uid,
  });
};
