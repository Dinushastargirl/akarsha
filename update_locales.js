const fs = require('fs');
const path = require('path');

const localesDir = path.join(__dirname, 'frontend', 'src', 'locales');

const translations = {
  'en.json': {
    chat: {
      title: "Chat",
      send: "Send",
      book_appointment: "Book appointment",
      reschedule: "Reschedule",
      cancel: "Cancel",
      talk_to_staff: "Talk to staff",
      language: "Language",
      available_times: "Available times",
      appointment_confirmed: "Appointment confirmed",
      appointment_cancelled: "Appointment cancelled",
      appointment_rescheduled: "Appointment rescheduled",
      human_support: "Human support"
    }
  },
  'si_lk.json': {
    chat: {
      title: "කතාබස්",
      send: "යවන්න",
      book_appointment: "වේලාවක් වෙන්කරවා ගන්න",
      reschedule: "වෙනස් කරන්න",
      cancel: "අවලංගු කරන්න",
      talk_to_staff: "සේවකයෙකුට කතා කරන්න",
      language: "භාෂාව",
      available_times: "ඇති වේලාවන්",
      appointment_confirmed: "වෙන්කරවා ගැනීම තහවුරු විය",
      appointment_cancelled: "වෙන්කරවා ගැනීම අවලංගු විය",
      appointment_rescheduled: "වෙන්කරවා ගැනීම වෙනස් විය",
      human_support: "සේවක සහාය"
    }
  },
  'ta_lk.json': {
    chat: {
      title: "அரட்டை",
      send: "அனுப்பு",
      book_appointment: "நேரம் ஒதுக்கு",
      reschedule: "மாற்று",
      cancel: "ரத்து செய்",
      talk_to_staff: "பணியாளரிடம் பேசு",
      language: "மொழி",
      available_times: "கிடைக்கும் நேரங்கள்",
      appointment_confirmed: "நேரம் உறுதி செய்யப்பட்டது",
      appointment_cancelled: "நேரம் ரத்து செய்யப்பட்டது",
      appointment_rescheduled: "நேரம் மாற்றப்பட்டது",
      human_support: "பணியாளர் உதவி"
    }
  }
};

Object.keys(translations).forEach(file => {
  const filePath = path.join(localesDir, file);
  if (fs.existsSync(filePath)) {
    const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    data.chat = translations[file].chat;
    fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
    console.log(`Updated ${file}`);
  }
});
