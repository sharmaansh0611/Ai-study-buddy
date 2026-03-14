async function askAi(req, res) {
  const prompt = req.body?.prompt || "No prompt provided";

  return res.json({
    answer: `AI response placeholder for ${req.user.uid}: ${prompt}`,
    confidence: "high",
  });
}

module.exports = {
  askAi,
};
