import mongoose from "mongoose";

const connectDB = async () => {
  try {
    const mongoUri = process.env.MONGO_URI;

    await mongoose.connect(mongoUri);
    console.log(`MongoDB connected (${mongoUri})`);
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export default connectDB;
