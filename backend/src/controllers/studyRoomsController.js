export async function getStudyRooms(req, res) {
  return res.json({
    rooms: [
      {
        id: "room-1",
        title: "System Design Study Room",
        topic: "Scalable backend architecture",
        memberCount: 8,
        nextSession: "Today • 8:00 PM",
        ownerUid: req.user.uid,
      },
    ],
  });
}
