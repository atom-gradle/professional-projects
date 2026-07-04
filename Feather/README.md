# WeChat-Inspired Social Android App

A feature-rich, Android social messaging application inspired by WeChat, built natively with Java. This project serves as a comprehensive exercise in Android development, covering UI/UX design, fragment management, and real-time bidirectional communication.

## 📖 Table of Contents

- [Features](#-features)
- [Tech Stack & Dependencies](#-tech-stack--dependencies)
- [App Architecture](#-app-architecture)
- [Installation & Setup](#-installation--setup)
- [Usage](#-usage)
- [Project Structure](#-project-structure)
- [Socket Implementation Notes](#-socket-implementation-notes)
- [License](#-license)

## ✨ Features

- **User Authentication:** A sleek `LoginActivity` for user sign-in
- **Modern Main Interface:** A `MainActivity` with bottom navigation containing three core fragments:
  - `HomeFragment`: Main chat list/discovery feed
  - `ContactFragment`: Friends and contact management
  - `MeFragment`: User profile and settings
- **Real-time Chat:** Full-featured `ChatActivity` supporting:
  - Text messages
  - Image sharing
  - Real-time message delivery
- **Dual Socket Architecture:** Implemented using both:
  - `java.io.Socket` (Blocking I/O)
  - `java.nio.ServerSocket` (Non-blocking I/O)

## 🛠 Tech Stack & Dependencies

- **Language:** Java
- **Minimum SDK:** Android 10.0 (API 29)
- **Core Android Components:**
  - `Activity` - For screen-level UI containers
  - `Fragment` - For modular, reusable UI components
  - `Intent` - For navigation and inter-component communication
  - `RecyclerView` - For efficient list display of chats and contacts
- **Image Loading:** [Glide](https://github.com/bumptech/glide) - For efficient image handling
- **Network Communication:** Custom socket implementation
- **Icons & Assets:** Material Design Icons from [Pictogrammers](https://pictogrammers.com/library/mdi/)
- **Architecture:** Standard Android Architecture Components

## 🏗 App Architecture (lists only part of the whole project)
- app/
- ├── src/main/
- │   ├── java/com/qian/
- │   │   ├── activity/
- │   │   │   ├── LoginActivity.java
- │   │   │   ├── MainActivity.java
- │   │   │   └── ChatActivity.java
- │   │   ├── fragment/
- │   │   │   ├── HomeFragment.java
- │   │   │   ├── ContactFragment.java
- │   │   │   └── MeFragment.java
- │   │   ├── adapter/
- │   │   │   ├── ChatRecyclerViewAdapter.java (RecyclerView.Adapter)
- │   │   │   ├── ContactRecyclerViewAdapter.java (RecyclerView.Adapter)
- │   │   │   └── MainRecyclerViewAdapter.java (RecyclerView.Adapter)
- │   │   ├── model/
- │   │   │   ├── User.java
- │   │   │   ├── Chat.java
- │   │   │   └── Msg.java
- │   │   ├── network/
- │   │   │   ├── Client.java
- │   │   │   ├── NIOClient.java
- │   │   └── util/
- │   └── res/
- │       ├── layout/
- │       │   ├── activity_login.xml
- │       │   ├── activity_main.xml
- │       │   ├── activity_chat.xml
- │       │   ├── fragment_home.xml
- │       │   ├── fragment_contact.xml
- │       │   ├── fragment_me.xml
- │       │   ├── item_chat.xml (RecyclerView item)
- │       │   ├── item_contact.xml (RecyclerView item)
- │       │   └── item_message.xml (RecyclerView item)
- │       ├── drawable/ (MDI icons from Pictogrammers)
- │       └── values/

## 🎯 Key Android Components Demonstrated

### Activities
- `LoginActivity`: Handles user authentication
- `MainActivity`: Hosts fragments and manages bottom navigation
- `ChatActivity`: Manages real-time messaging interface

### Fragments
- `HomeFragment`: Displays chat list using RecyclerView
- `ContactFragment`: Shows contact list with RecyclerView
- `MeFragment`: User profile management

### Intents
- Used for navigation between activities
- Data passing between components
- Implicit and explicit intent usage

### RecyclerView
- **Chat List:** Efficiently displays conversation history
- **Contact List:** Shows friends and contacts
- **Message List:** Real-time message display in chat
- Custom adapters for different data types

### Resources & Assets
- **Icons:** Material Design Icons sourced from [Pictogrammers MDI Library](https://pictogrammers.com/library/mdi/)
- **Layouts:** Responsive XML layouts for various screen sizes
- **Themes:** Consistent Material Design theme throughout
