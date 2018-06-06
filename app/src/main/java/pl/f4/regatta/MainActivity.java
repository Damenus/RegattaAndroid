package pl.f4.regatta;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeTextView;
    private WebView webview;

    RequestQueue queue;
    String url ="http://vps485240.ovh.net:8080/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Send info
        queue = Volley.newRequestQueue(this);

        HttpClient.setMainActivity(this);

        //launchMapActivity();
        launchLoginActivity(queue);

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

//        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
//        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
//        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        mLastUpdateTime = "";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        startLocationUpdates();

        webview = (WebView) findViewById(R.id.webview);
        //webview.loadUrl("http://vps485240.ovh.net:8080/");
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadData(getMap(String.valueOf(18.0),String.valueOf(54.0), String.valueOf(15)), "text/html", null);
        //webview.loadData(summary, "text/html", null);

    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new ZonedDateTime());
                updateUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        updateUI();
                        break;
                }
                break;
        }
    }

    protected void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

//                        //noinspection MissingPermission
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return;
//                        }
//                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                                mLocationCallback, Looper.myLooper());
                        requestLocationUpdates();
                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
//            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
//                    mCurrentLocation.getLatitude()));
//            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
//                    mCurrentLocation.getLongitude()));
//            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
//                    mLastUpdateTimeLabel, mLastUpdateTime));

           // String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS";
           // DateTimeFormatter Parser = DateTimeFormatter.ofPattern(pattern).ISO_DATE;

            //webview.reload();
            HttpClient.SendPositionTask mAuthTask = new HttpClient.SendPositionTask(
                    Long.decode("1"),
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    mLastUpdateTime);
            mAuthTask.execute((Void) null);

//            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
//                    mCurrentLocation.getLatitude()));
//            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
//                    mCurrentLocation.getLongitude()));
//            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
//                    mLastUpdateTimeLabel, mLastUpdateTime));
            //queue.add(preparePostRequest());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    protected class Position{
        public double latitude;
        public double longitude;

        Position() {
            latitude = 0;
            longitude = 0;
        }

        Position(double latitude,  double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        Position position = new Position();
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

//                            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
//                                    mLatitudeLabel,
//                                    mLastLocation.getLatitude()));
//                            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f",
//                                    mLongitudeLabel,
//                                    mLastLocation.getLongitude()));

                            //position.Latitude = mLastLocation.getLatitude();
                            //position.Longitude =  mLastLocation.getLongitude();

                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    protected void launchLoginActivity(RequestQueue queue) {
        Intent intent = new Intent(this, LoginActivity.class);
        //intent.putExtra("queue", queue);
        startActivity(intent);
    }

    protected void launchMapActivity() {
        Intent intent = new Intent(this, map.class);
        startActivity(intent);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
//        View container = findViewById(R.id.main_activity_container);
//        if (container != null) {
//            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
//        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    private StringRequest preparePostRequest() {
        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", "Alif");
                params.put("domain", "http://itsalif.info");

                return params;
            }
        };
//queue.add(postRequest);
    }

    public String getMap(String lon, String lat, String zoom) {

        return "\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" +
                "        <title>OpenSeaMap</title>\n" +
                "\n" +
                "\n" +
                "        <style type=\"text/css\">\n" +
                "            .olImageLoadError {\n" +
                "                display: none !important;\n" +
                "            }\n" +
                "        </style>\n" +
                "\n" +
                "        <!-- bring in the OpenLayers javascript library\n" +
                "            (here we bring it from the remote site, but you could\n" +
                "            easily serve up this javascript yourself) -->\n" +
                "        <script src=\"http://www.openlayers.org/api/OpenLayers.js\"></script>\n" +
                "\n" +
                "        <!-- bring in the OpenStreetMap OpenLayers layers.\n" +
                "            Using this hosted file will make sure we are kept up\n" +
                "            to date with any necessary changes -->\n" +
                "        <script src=\"http://www.openstreetmap.org/openlayers/OpenStreetMap.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/harbours.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/map_utils.js\"></script>\n" +
                "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/utilities.js\"></script>\n" +
                "        <script type=\"text/javascript\">\n" +
                "\n" +
                "            var map;\n" +
                "            var layer_mapnik;\n" +
                "            var layer_tah;\n" +
                "            var layer_seamark;\n" +
                "            var marker;\n" +
                "\n" +
                "            // Position and zoomlevel of the map\n" +
//                "            var lon = 12.0915;\n" +
//                "            var lat = 54.1878;\n" +
//                "            var zoom = 15;\n" +
                "            var lon = " + lon + ";\n" +
                "            var lat = " + lat + ";\n" +
                "            var zoom = " + zoom + ";\n" +
                "                \n" +
                "            var linkTextSkipperGuide = \"Beschreibung auf SkipperGuide\";\n" +
                "            var linkTextWeatherHarbour = \"Meteogramm\";\n" +
                "            var language = 'de';\n" +
                "\n" +
                "            \n" +
                "            function jumpTo(lon, lat, zoom) {\n" +
                "                var x = Lon2Merc(lon);\n" +
                "                var y = Lat2Merc(lat);\n" +
                "                map.setCenter(new OpenLayers.LonLat(x, y), zoom);\n" +
                "                return false;\n" +
                "            }\n" +
                "\n" +
                "            function Lon2Merc(lon) {\n" +
                "                return 20037508.34 * lon / 180;\n" +
                "            }\n" +
                "\n" +
                "            function Lat2Merc(lat) {\n" +
                "                var PI = 3.14159265358979323846;\n" +
                "                lat = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);\n" +
                "                return 20037508.34 * lat / 180;\n" +
                "            }\n" +
                "\n" +
                "            function addMarker(layer, lon, lat, popupContentHTML) {\n" +
                "                var ll = new OpenLayers.LonLat(Lon2Merc(lon), Lat2Merc(lat));\n" +
                "                var feature = new OpenLayers.Feature(layer, ll);\n" +
                "                feature.closeBox = true;\n" +
                "                feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(260, 100) } );\n" +
                "                feature.data.popupContentHTML = popupContentHTML;\n" +
                "                feature.data.overflow = \"hidden\";\n" +
                "\n" +
                "                marker = new OpenLayers.Marker(ll);\n" +
                "                marker.feature = feature;\n" +
                "\n" +
                "                var markerClick = function(evt) {\n" +
                "                    if (this.popup == null) {\n" +
                "                        this.popup = this.createPopup(this.closeBox);\n" +
                "                        map.addPopup(this.popup);\n" +
                "                        this.popup.show();\n" +
                "                    } else {\n" +
                "                        this.popup.toggle();\n" +
                "                    }\n" +
                "                    OpenLayers.Event.stop(evt);\n" +
                "                };\n" +
                "                marker.events.register(\"mousedown\", feature, markerClick);\n" +
                "\n" +
                "                layer.addMarker(marker);\n" +
                "                map.addPopup(feature.createPopup(feature.closeBox));\n" +
                "            }\n" +
                "\n" +
                "            function getTileURL(bounds) {\n" +
                "                var res = this.map.getResolution();\n" +
                "                var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));\n" +
                "                var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));\n" +
                "                var z = this.map.getZoom();\n" +
                "                var limit = Math.pow(2, z);\n" +
                "                if (y < 0 || y >= limit) {\n" +
                "                    return null;\n" +
                "                } else {\n" +
                "                    x = ((x % limit) + limit) % limit;\n" +
                "                    url = this.url;\n" +
                "                    path= z + \"/\" + x + \"/\" + y + \".\" + this.type;\n" +
                "                    if (url instanceof Array) {\n" +
                "                        url = this.selectUrl(path, url);\n" +
                "                    }\n" +
                "                    return url+path;\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            function drawmap() {\n" +
                "\n" +
                "\n" +
                "                map = new OpenLayers.Map('map', {\n" +
                "                    projection: new OpenLayers.Projection(\"EPSG:900913\"),\n" +
                "                    displayProjection: new OpenLayers.Projection(\"EPSG:4326\"),\n" +
                "                    eventListeners: {\n" +
                "                        \"moveend\": mapEventMove,\n" +
                "                        //\"zoomend\": mapEventZoom\n" +
                "                    },\n" +
                "                    controls: [\n" +
                "                        new OpenLayers.Control.Navigation(),\n" +
                "                        new OpenLayers.Control.ScaleLine({topOutUnits : \"nmi\", bottomOutUnits: \"km\", topInUnits: 'nmi', bottomInUnits: 'km', maxWidth: '40'}),\n" +
//                "                       new OpenLayers.Control.LayerSwitcher(),\n" +
//                "                        new OpenLayers.Control.MousePosition(),\n" +
//                "                        new OpenLayers.Control.PanZoomBar()" +
                "],\n" +
                "                        maxExtent:\n" +
                "                        new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),\n" +
                "                    numZoomLevels: 18,\n" +
                "                    maxResolution: 156543,\n" +
                "                    units: 'meters'\n" +
                "                });\n" +
                "\n" +
                "                // Add Layers to map-------------------------------------------------------------------------------------------------------\n" +
                "                // Mapnik\n" +
                "                layer_mapnik = new OpenLayers.Layer.OSM.Mapnik(\"Mapnik\");\n" +
                "                // Seamark\n" +
                "                layer_seamark = new OpenLayers.Layer.TMS(\"Seezeichen\", \"http://tiles.openseamap.org/seamark/\", { numZoomLevels: 18, type: 'png', getURL: getTileURL, isBaseLayer: false, displayOutsideMaxExtent: true});\n" +
                "                // Harbours\n" +
                "                layer_pois = new OpenLayers.Layer.Vector(\"Häfen\", { projection: new OpenLayers.Projection(\"EPSG:4326\"), visibility: true, displayOutsideMaxExtent:true});\n" +
                "                layer_pois.setOpacity(0.8);\n" +
                "                \n" +
                "                map.addLayers([layer_mapnik, layer_seamark, layer_pois]);\n" +
                "                jumpTo(lon, lat, zoom);\n" +
                "\n" +
                "                // Update harbour layer\n" +
                "                refreshHarbours();\n" +
                "            }\n" +
                "\n" +
                "            // Map event listener moved\n" +
                "            function mapEventMove(event) {\n" +
                "                // Update harbour layer\n" +
                "                refreshHarbours();\n" +
                "            }\n" +
                "    </script>\n" +
                "\n" +
                "</head>\n" +
                "\n" +
                "<!-- body.onload is called once the page is loaded (call the 'init' function) -->\n" +
                "<body onload=\"drawmap();\">\n" +
                "\n" +
                "    <!-- define a DIV into which the map will appear. Make it take up the whole window -->\n" +
                "    <div style=\"width:100%; height:100%\" id=\"map\"></div>\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }

    String summary = "\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n" +
            "        <title>OpenSeaMap</title>\n" +
            "\n" +
            "\n" +
            "        <style type=\"text/css\">\n" +
            "            .olImageLoadError {\n" +
            "                display: none !important;\n" +
            "            }\n" +
            "        </style>\n" +
            "\n" +
            "        <!-- bring in the OpenLayers javascript library\n" +
            "            (here we bring it from the remote site, but you could\n" +
            "            easily serve up this javascript yourself) -->\n" +
            "        <script src=\"http://www.openlayers.org/api/OpenLayers.js\"></script>\n" +
            "\n" +
            "        <!-- bring in the OpenStreetMap OpenLayers layers.\n" +
            "            Using this hosted file will make sure we are kept up\n" +
            "            to date with any necessary changes -->\n" +
            "        <script src=\"http://www.openstreetmap.org/openlayers/OpenStreetMap.js\"></script>\n" +
            "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/harbours.js\"></script>\n" +
            "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/map_utils.js\"></script>\n" +
            "        <script type=\"text/javascript\" src=\"http://map.openseamap.org/map/javascript/utilities.js\"></script>\n" +
            "        <script type=\"text/javascript\">\n" +
            "\n" +
            "            var map;\n" +
            "            var layer_mapnik;\n" +
            "            var layer_tah;\n" +
            "            var layer_seamark;\n" +
            "            var marker;\n" +
            "\n" +
            "            // Position and zoomlevel of the map\n" +
            "            var lon = 12.0915;\n" +
            "            var lat = 54.1878;\n" +
            "            var zoom = 15;\n" +
            "                \n" +
            "            var linkTextSkipperGuide = \"Beschreibung auf SkipperGuide\";\n" +
            "            var linkTextWeatherHarbour = \"Meteogramm\";\n" +
            "            var language = 'de';\n" +
            "\n" +
            "            \n" +
            "            function jumpTo(lon, lat, zoom) {\n" +
            "                var x = Lon2Merc(lon);\n" +
            "                var y = Lat2Merc(lat);\n" +
            "                map.setCenter(new OpenLayers.LonLat(x, y), zoom);\n" +
            "                return false;\n" +
            "            }\n" +
            "\n" +
            "            function Lon2Merc(lon) {\n" +
            "                return 20037508.34 * lon / 180;\n" +
            "            }\n" +
            "\n" +
            "            function Lat2Merc(lat) {\n" +
            "                var PI = 3.14159265358979323846;\n" +
            "                lat = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);\n" +
            "                return 20037508.34 * lat / 180;\n" +
            "            }\n" +
            "\n" +
            "            function addMarker(layer, lon, lat, popupContentHTML) {\n" +
            "                var ll = new OpenLayers.LonLat(Lon2Merc(lon), Lat2Merc(lat));\n" +
            "                var feature = new OpenLayers.Feature(layer, ll);\n" +
            "                feature.closeBox = true;\n" +
            "                feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(260, 100) } );\n" +
            "                feature.data.popupContentHTML = popupContentHTML;\n" +
            "                feature.data.overflow = \"hidden\";\n" +
            "\n" +
            "                marker = new OpenLayers.Marker(ll);\n" +
            "                marker.feature = feature;\n" +
            "\n" +
            "                var markerClick = function(evt) {\n" +
            "                    if (this.popup == null) {\n" +
            "                        this.popup = this.createPopup(this.closeBox);\n" +
            "                        map.addPopup(this.popup);\n" +
            "                        this.popup.show();\n" +
            "                    } else {\n" +
            "                        this.popup.toggle();\n" +
            "                    }\n" +
            "                    OpenLayers.Event.stop(evt);\n" +
            "                };\n" +
            "                marker.events.register(\"mousedown\", feature, markerClick);\n" +
            "\n" +
            "                layer.addMarker(marker);\n" +
            "                map.addPopup(feature.createPopup(feature.closeBox));\n" +
            "            }\n" +
            "\n" +
            "            function getTileURL(bounds) {\n" +
            "                var res = this.map.getResolution();\n" +
            "                var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));\n" +
            "                var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));\n" +
            "                var z = this.map.getZoom();\n" +
            "                var limit = Math.pow(2, z);\n" +
            "                if (y < 0 || y >= limit) {\n" +
            "                    return null;\n" +
            "                } else {\n" +
            "                    x = ((x % limit) + limit) % limit;\n" +
            "                    url = this.url;\n" +
            "                    path= z + \"/\" + x + \"/\" + y + \".\" + this.type;\n" +
            "                    if (url instanceof Array) {\n" +
            "                        url = this.selectUrl(path, url);\n" +
            "                    }\n" +
            "                    return url+path;\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            function drawmap() {\n" +
            "\n" +
            "\n" +
            "                map = new OpenLayers.Map('map', {\n" +
            "                    projection: new OpenLayers.Projection(\"EPSG:900913\"),\n" +
            "                    displayProjection: new OpenLayers.Projection(\"EPSG:4326\"),\n" +
            "                    eventListeners: {\n" +
            "                        \"moveend\": mapEventMove,\n" +
            "                        //\"zoomend\": mapEventZoom\n" +
            "                    },\n" +
            "                    controls: [\n" +
            "                        new OpenLayers.Control.Navigation(),\n" +
            "                        new OpenLayers.Control.ScaleLine({topOutUnits : \"nmi\", bottomOutUnits: \"km\", topInUnits: 'nmi', bottomInUnits: 'km', maxWidth: '40'}),\n" +
//                "                       new OpenLayers.Control.LayerSwitcher(),\n" +
//                "                        new OpenLayers.Control.MousePosition(),\n" +
//                "                        new OpenLayers.Control.PanZoomBar()" +
            "],\n" +
            "                        maxExtent:\n" +
            "                        new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),\n" +
            "                    numZoomLevels: 18,\n" +
            "                    maxResolution: 156543,\n" +
            "                    units: 'meters'\n" +
            "                });\n" +
            "\n" +
            "                // Add Layers to map-------------------------------------------------------------------------------------------------------\n" +
            "                // Mapnik\n" +
            "                layer_mapnik = new OpenLayers.Layer.OSM.Mapnik(\"Mapnik\");\n" +
            "                // Seamark\n" +
            "                layer_seamark = new OpenLayers.Layer.TMS(\"Seezeichen\", \"http://tiles.openseamap.org/seamark/\", { numZoomLevels: 18, type: 'png', getURL: getTileURL, isBaseLayer: false, displayOutsideMaxExtent: true});\n" +
            "                // Harbours\n" +
            "                layer_pois = new OpenLayers.Layer.Vector(\"Häfen\", { projection: new OpenLayers.Projection(\"EPSG:4326\"), visibility: true, displayOutsideMaxExtent:true});\n" +
            "                layer_pois.setOpacity(0.8);\n" +
            "                \n" +
            "                map.addLayers([layer_mapnik, layer_seamark, layer_pois]);\n" +
            "                jumpTo(lon, lat, zoom);\n" +
            "\n" +
            "                // Update harbour layer\n" +
            "                refreshHarbours();\n" +
            "            }\n" +
            "\n" +
            "            // Map event listener moved\n" +
            "            function mapEventMove(event) {\n" +
            "                // Update harbour layer\n" +
            "                refreshHarbours();\n" +
            "            }\n" +
            "    </script>\n" +
            "\n" +
            "</head>\n" +
            "\n" +
            "<!-- body.onload is called once the page is loaded (call the 'init' function) -->\n" +
            "<body onload=\"drawmap();\">\n" +
            "\n" +
            "    <!-- define a DIV into which the map will appear. Make it take up the whole window -->\n" +
            "    <div style=\"width:100%; height:100%\" id=\"map\"></div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "</html>";

}
