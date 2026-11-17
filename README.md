# ভয়েস কিবোর্ড AI (Android)

বাংলা ও English কিবোর্ড, voice typing, Gemini দিয়ে লেখা গুছানো/সংশোধন/অনুবাদ,
অডিও ফাইল থেকে transcription এবং ভাষা শেখার অনুশীলন।

## প্রয়োজনীয়তা

- JDK 17
- Android SDK Platform 35

## লোকালি APK তৈরি

Android Studio-তে এই ফোল্ডার খুলে Gradle sync শেষ করুন, অথবা Android SDK সেটআপ
করে চালান:

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions দিয়ে APK তৈরি

`main` branch-এ push, pull request, অথবা Actions-এর **Build APK** workflow
ম্যানুয়ালি চালালে `VoiceKeyboardAI-apk` নামে downloadable debug APK artifact
তৈরি হবে। Workflow প্রয়োজনীয় Android SDK packages নিজে ইনস্টল করে।

## প্রথমবার সেটআপ

1. অ্যাপ ইনস্টল করে খুলুন।
2. `aistudio.google.com/apikey` থেকে Gemini API key তৈরি করে অ্যাপে সেভ করুন।
   Key-টি Android Keystore দিয়ে এই ডিভাইসে এনক্রিপ্ট করে রাখা হয়।
3. **কিবোর্ড চালু করুন** চাপুন, Settings-এ কিবোর্ড enable করুন, তারপর **কিবোর্ড
   বেছে নিন** চাপুন।
4. Voice typing ব্যবহার করতে অ্যাপ থেকে microphone permission দিন।

## কিবোর্ড ও AI

- 🎤 চাপলে নির্বাচিত বাংলা বা English ভাষায় voice typing হয়।
- Space ধরে রাখলে voice input Gemini দিয়ে গুছিয়ে বসে।
- ✨ লেখা গুছায়, ✓ বানান/ব্যাকরণ ঠিক করে, ⇄ বাংলা ↔ English অনুবাদ করে।
- বাংলা / English বোতামে কিবোর্ড layout বদলায়।
- 📋 ধরে রাখলে নির্বাচিত লেখা কপি হয়; selection না থাকলে পুরো field কপি হয়।
- AI লেখা বসানোর পর status line চাপলে বা English layout-এ `Z` ধরে রাখলে undo হয়।
- অ্যাপের **অডিও ফাইল থেকে লেখা** অংশে audio file বেছে নিয়ে transcription করুন।
- **বাংলা + English শিখুন** অংশে বাক্য লিখে সংশোধন, অনুবাদ, ব্যাখ্যা ও অনুশীলন
  নিন।

AI লেখা বা নির্বাচিত audio Google Gemini-তে পাঠানো হয়; voice typing Android-এর
speech-recognition service ব্যবহার করে। Password ও private input field-এ keyboard
থেকে voice ও AI বন্ধ থাকে। গোপন তথ্য AI-তে পাঠাবেন না।
