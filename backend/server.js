const express = require("express");
const cors = require("cors");
require("dotenv").config();

const connectDB = require("./src/config/db");   // ADD THIS

const app = express();

connectDB();   // ADD THIS

app.use(cors());
app.use(express.json());

app.get("/health", (req, res) => {
  res.json({ status: "Server running" });
});

// ADD THIS ROUTE
app.use("/api", require("./src/routes/healthRoutes"));

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
