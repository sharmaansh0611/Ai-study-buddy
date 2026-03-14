async function getUserNotes(req, res) {
  return res.json({
    notes: [
      {
        id: "note-1",
        title: "Operating Systems Revision",
        subject: "Computer Science",
        updatedAt: "2026-03-15T09:00:00.000Z",
        fileUrl: null,
        userId: req.user.uid,
      },
    ],
  });
}

async function uploadNote(req, res) {
  return res.status(201).json({
    success: true,
    message: `Note uploaded successfully for user ${req.user.uid}`,
  });
}

module.exports = {
  getUserNotes,
  uploadNote,
};
