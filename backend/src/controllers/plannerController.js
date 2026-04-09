import StudyEvent from "../models/StudyEvent.js";

export const createEvent = async (req, res) => {
  try {
    const title = req.body?.title?.trim();
    const description = req.body?.description?.trim() || "";
    const date = req.body?.date?.trim();
    const time = req.body?.time?.trim();

    if (!title || !date || !time) {
      return res.status(400).json({
        message: "title, date, and time are required.",
      });
    }

    const event = await StudyEvent.create({
      user_id: req.user.uid,
      title,
      description,
      date,
      time,
    });

    return res.status(201).json({
      message: "Study event created successfully.",
      event: {
        event_id: event.event_id,
        title: event.title,
        description: event.description,
        date: event.date,
        time: event.time,
        created_at: event.created_at,
      },
    });
  } catch (error) {
    console.error("Failed to create study event:", error);
    return res.status(500).json({
      message: "Failed to create study event.",
      error: error.message,
    });
  }
};

export const getEvents = async (req, res) => {
  try {
    const events = await StudyEvent.find({ user_id: req.user.uid })
      .sort({ date: 1, time: 1, created_at: -1 })
      .lean();

    return res.json({
      events: events.map((event) => ({
        event_id: event.event_id,
        title: event.title,
        description: event.description,
        date: event.date,
        time: event.time,
        created_at: event.created_at,
      })),
    });
  } catch (error) {
    console.error("Failed to fetch study events:", error);
    return res.status(500).json({
      message: "Failed to fetch study events.",
      error: error.message,
    });
  }
};
