# ✈️ SmartFRS - Smart Flight Reservation System

**SmartFRS** is a modern, feature-rich Android application designed to provide a seamless flight booking, ticket management, and travel experience. Built with **Kotlin**, **Android Jetpack**, and **Retrofit**, SmartFRS offers real-time flight search, secure payment integration, QR code boarding passes, PDF ticket downloads, ticket cancellation, and an AI-powered travel assistant chatbot.

---

## 🌟 Key Features

* **🔐 User Authentication:** Secure User Registration, Login, and OTP Verification system.
* **🔍 Flight Search & Selection:** Search flights by departure/destination cities, travel dates, passenger counts, and seat class preferences.
* **📝 Passenger Information Management:** Multi-passenger details entry with validation and seat selections.
* **💳 Secure Payment Gateway:** Integrated checkout workflow with payment security dialogs and confirmation.
* **📱 Digital Boarding Pass & QR Code:** Automatic QR Code generation for boarding passes for quick airport check-ins.
* **📄 PDF Ticket Export:** Export and download flight tickets as formatted PDF files directly to your device storage.
* **📋 Booking History & Ticket Management:** View all active and past flight bookings with full ticket breakdown details.
* **❌ Ticket Cancellation:** Easily request ticket cancellations and process refund status.
* **🤖 AI Travel Assistant:** Integrated AI chatbot (`SupportActivity`) to assist travelers with instant query resolution and customer support.

---

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin
* **UI Components:** XML Layouts, Material Design Components, RecyclerViews, Custom Adapters
* **Networking:** Retrofit 2, Gson Converter
* **Utilities & Libraries:**
  * ZXing / QR Code Generator (`QRCodeGenerator.kt`)
  * PDF Document Exporter (`TicketPdfExporter.kt`)
  * Firebase / Google Services Integration
* **Architecture Pattern:** Repository Pattern (`TicketRepository.kt`), Clean Activity/Adapter separation

---

## 📁 Project Structure

```
SmartFRS/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/smartfrs/
│   │   │   │   ├── MainActivity.kt               # Login Screen
│   │   │   │   ├── RegisterActivity.kt           # Registration
│   │   │   │   ├── HomeActivity.kt               # Main Dashboard
│   │   │   │   ├── SearchFlightActivity.kt       # Flight Search
│   │   │   │   ├── FlightListActivity.kt         # Flight Results
│   │   │   │   ├── PassengerInfoActivity.kt      # Passenger Entry
│   │   │   │   ├── PaymentActivity.kt            # Payment Checkout
│   │   │   │   ├── TicketDetailsActivity.kt      # Ticket Summary
│   │   │   │   ├── QRCodeGenerator.kt            # QR Generator
│   │   │   │   ├── TicketPdfExporter.kt          # PDF Ticket Export
│   │   │   │   ├── SupportActivity.kt            # AI Chatbot Support
│   │   │   │   └── ...
│   │   │   ├── res/                              # Layouts, Drawables, Values
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** (Koala / Ladybug / Jellyfish or newer recommended)
* **Android SDK** API Level 24+ (Android 7.0 Nougat minimum, target SDK 34/35)
* **JDK 17+**

### Installation & Build

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/rafat1976/SmartFRS-Andriod-Apps.git
   ```
2. **Open in Android Studio:**
   * Open Android Studio -> `File` -> `Open` -> Select the cloned `SmartFRS` directory.
3. **Gradle Sync:**
   * Allow Gradle to download dependencies and sync automatically.
4. **Run Application:**
   * Connect an Android device or launch an AVD Emulator, then click **Run (Shift + F10)**.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 👤 Author

* **Arif Istique Rafat** - [*GitHub Profile*](https://github.com/rafat1976)
