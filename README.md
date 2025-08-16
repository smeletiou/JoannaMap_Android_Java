# 📸 Android Photo Mapping App

An Android application developed for EPL498 Homework 2 that enables users to capture photos, save location and orientation data, and view photos on a map using marker clustering. All photo metadata is stored locally in a Room database.

## 🧭 Features Overview

### 👋 Welcome Screen
- Displays a simple welcome message.
- Two navigation buttons:
  - **Capture Photo** – navigates to the camera.
  - **Map** – opens the interactive map screen.

### 📷 Camera Functionality
- Captures photos and saves:
  - Longitude & Latitude
  - Timestamp
  - Address (via `Geocoder`)
  - Orientation (portrait or landscape)
- A banner shows location feedback when a photo is captured.
- Includes a **Map** button for quick access.

### 🗺️ Map View
- Displays markers for all stored photos.
- Supports orientation filtering (portrait vs. landscape).
- Photos are grouped by location using marker clustering.
- As you zoom in, additional photos are revealed.
- Clicking on a marker navigates to the **Photo Details** view.

### 🖼️ Photo Details
- Shows:
  - Captured image
  - Location (address)
  - Orientation
  - Timestamp
- Option to **delete** the photo from the database.

## 🗄️ Data Storage
- Local Room database stores:
  - Image URI
  - Location (lat, long)
  - Address
  - Orientation
  - Timestamp

## 🏗️ Architecture & Files

- `WelcomeFragment`: Home screen with navigation buttons.
- `CameraFragment`: Extended from lab example, captures photo + metadata.
- `MapFragment`: Loads the map, fetches photos, creates markers, and handles orientation-based filtering.
- `PhotoDetailFragment`: Displays photo information and allows deletion.
- `AddressHelper`: Converts coordinates into address using `Geocoder`.
- `AppStorage`: Abstract class for storing photos and metadata.
- `Cluster`: Handles marker clustering (not fully implemented).
- `DatabaseManager`: Manages Room queries.
- `PhotoInfoDao`: Executes Room queries.
- `PhotoInfoDB`: Room schema definition.
- `MapViewModel`: Manages the map state for markers.

## ⚠️ Known Issues
- When displaying images (in markers or detail pages), images were rotated 90° counter-clockwise.
- As a workaround, a rotation function was applied to all image display points to match the original orientation.

## 📌 Limitations
- When multiple photos exist at the same location, only the top image is shown.
- No photo swapping mechanism yet; to see other images, the top one must be deleted.

## 👤 Author

**Sotiris Meletiou**  
