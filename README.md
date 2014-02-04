MyLocalWeather
==============

A test app using OpenWeather REST API

Based on the Google I/O 2010 talk by Virgil Dobjanschi

http://www.google.com/events/io/2010/sessions/developing-RESTful-android-apps.html

Virgil proposed 3 different design patterns when using Android and REST services.
– Use a Service API
– Use the ContentProvider API
– Use the ContentProvider API and a SyncAdapter

I'm using the last approach for this sample.  

User experience:

There's only one activity, called Main.  There's a basic display of the local
weather forecast and some details on location and last request timestamp.  There's
also a button, "Update Forecast".  When a user presses this button, we use the
content resolver to request a sync.  Once the sync is complete and the forecast
db table has been updated, the UI is updated with the new forecast data.

Components:

- Activity
  The UI Thread.  It registers a ContentObserver on the Forecast table to get
  notifications when the table has been updated.  This then calls a AsyncTask
  to update the UI.  To get around issues using AsyncTask in an Activity, I'm
  using the pattern mentioned in http://www.shanekirk.com/2012/04/asynctask-missteps/ 
- Broadcast Receiver
  A simple broadcast receiver implementation which starts the required services
  on boot complete.
- Content Provider
  A simple ContentProvider implementation based on the Google Developer tutorial
  which wraps the SQLite access and when a change occurs, sends a notification to
  any ContentObservers registered.
- Sync Adaptor
  A simple Sync Adaptor implementation based on the Google Developer tutorial.  The
  users current location is got from the LocationManager here.
- Weather Forecast Processor thread
  Handles the communication with the REST apis.  This sample app uses two destinations.
  The first is the Google Geocode API, and does a reverse geocode lookup on the location.
  I use this API rather than the in built Geocoder implementation as it may not be
  present in the stock Android, and even when it is present, it's buggy in the version
  of JellyBean on my phone - http://code.google.com/p/android/issues/detail?id=38009.
  The second is the OpenWeater API to get the current weather forecast from this location.
  
  The responses from both APIs are in JSON and are parsed using GSON.
