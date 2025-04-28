Introduction:
The Smart Air Quality Checker is a mobile application designed to monitor the air quality (AQI, PM2.5, PM10) in real-time based on the user’s current location. It offers live updates, historical data visualization, and cloud synchronization.

Main Features:
1) Real-Time Air Quality Monitoring
Automatically fetches AQI, PM2.5, and PM10 based on the device’s live GPS location.
2) Historical Data Storage
Saves air quality readings over time in local SQLite database.
3) Graphical Visualization
Displays air quality trends over time using interactive charts.
4) Map Visualization
Shows recorded locations and air quality levels on a Google Map with colored markers (Green = Good, Orange = Moderate, Red = Bad).
5) Cloud Uploading
Uploads air quality data to ThingSpeak for remote access and further visualization.
6) Background Monitoring
Periodically checks air quality even when the app is closed.
7) Notifications
Sends alerts if the air quality becomes unhealthy (AQI > threshold).

How to Use the App:
1) Launch the App: Open the Smart Air Quality Checker from your device.
2) Grant Permissions: Allow Location and Notification permissions when prompted.
3) Check Air Quality: Press "Check Air Quality" to fetch the latest data.
4) View History: Tap "View History" to see a graph of past AQI measurements.
5) View Map: Tap "View Map" to view air quality at different locations.
6) Background Monitoring: The app automatically fetches and stores AQI every 30 minutes.
7) Cloud Dashboard: Data is sent to ThingSpeak, available remotely on your personal cloud dashboard.