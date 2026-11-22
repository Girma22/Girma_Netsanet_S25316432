# ScotTraffic – Android Application

## Project Description
ScotTraffic is an Android application developed to provide users with Scottish traffic and roadworks information.

## Technologies
- Android Studio
- Java
- XML
- Android XML Pull Parser
- ListView 
- Google Maps 

## API Credentials and Configuration

This application uses Traffic Scotland API credentials and a Google Maps API key.

For security reasons, `local.properties` is not included in this GitHub repository.

To run the application:

1. Open the project in Android Studio.
2. Create or configure the `local.properties` file in the project root.
3. Add the following properties using your own valid credentials:

```properties
TRAFFIC_CLIENT_ID=YOUR_TRAFFIC_CLIENT_ID
TRAFFIC_CLIENT_KEY=YOUR_TRAFFIC_CLIENT_KEY
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

4. Save the file.
5. Synchronise the project with Gradle.
6. Build and run the application.

The application uses these local properties to configure its Traffic Scotland credentials and Google Maps integration.

## Main Features
- Download traffic information from the Traffic Scotland XML feeds.
- Display traffic and roadworks information.
- Search and filter traffic information.
- Filter traffic incidents by distance.
- View detailed traffic information.
- Display traffic locations on a map.
- Support portrait and landscape layouts.

## How to Import
1. Download or clone this repository.
2. Open Android Studio.
3. Select Open.
4. Select the extracted GirmaAndriodproject folder.
5. Allow Gradle to sync.
6. Configure any required API keys locally.
7. Run the application on an Android device or emulator.

## API Key Configuration
Private API keys and local.properties are not included in this repository. Configure your own required keys locally before running the application.

## Author
Netsanet Girma 