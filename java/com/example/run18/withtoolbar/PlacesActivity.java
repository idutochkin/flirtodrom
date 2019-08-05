package com.example.run18.withtoolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlacesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final String LOG = "LOG_ListPlacesActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;
    private static final String TAG_PLACE_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_PICTURE = "picture";
    private static final String TAG_COUNT_CHECK_IN = "count_check_in";

    private List<FeedPlace> feedsPlaces;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
            ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
            if(ApiToken.length() > 0) {
                url_db = "http://flirtodrom.miroplat.nichost.ru/api.php?api_token="+ApiToken;
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        setContentView(R.layout.places);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // Set padding for Tiles
        int tilePadding = 8;
        mRecyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                new GetListPlaces().execute(mLastLocation);
            } else {
                Toast.makeText(this, "Ваше местоположение не удалось определить.", Toast.LENGTH_LONG).show();
                new GetListPlaces().execute();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.places_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_list_places) {
            startActivity(new Intent(this, PlacesActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_dialogs) {
            startActivity(new Intent(this, DialogsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_help) {
            Uri address = Uri.parse("http://m.flirtodrom.com/");
            Intent openlink = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlink);
        } else if (id == R.id.nav_send) {
            SharedPreferences.Editor editor = ApiSettings.edit();
            editor.putString(APP_PREFERENCES_API_TOKEN, "");
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetListPlaces extends AsyncTask<Location, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Location[] mLocation) {
            Log.d(LOG, "Connect: START");
            try {
                url_db = url_db + "&action=get_list_places";
                if(mLocation.length > 0)
                    url_db = url_db + "&latitude=" + mLocation[0].getLatitude() + "&longitude=" + mLocation[0].getLongitude();
                Request request = new Request.Builder().url(url_db).build();
                Response response = new OkHttpClient().newCall(request).execute();
                String result = response.body().string();

                JSONObject resObj = new JSONObject(result);
                if(resObj.getString("TYPE").equals("OK")) {
                    JSONArray places = resObj.getJSONArray("DATA");
                    feedsPlaces = new ArrayList<>();
                    for(int i = 0; i < places.length(); i++) {
                        JSONObject c = places.getJSONObject(i);

                        FeedPlace item = new FeedPlace();
                        item.setPlaceId(c.optString(TAG_PLACE_ID));
                        item.setName(c.optString(TAG_NAME));
                        item.setPlaceAddress(c.optString(TAG_ADDRESS));
                        item.setPlacePicture(c.optString(TAG_PICTURE));
                        item.setCountCheckIn(c.optString(TAG_COUNT_CHECK_IN));
                        feedsPlaces.add(item);
                    }
                } else if(resObj.getString("DATA").equals("unknown token")) {
                    SharedPreferences.Editor editor = ApiSettings.edit();
                    editor.putString(APP_PREFERENCES_API_TOKEN, "");
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            } catch (Exception e) {
                Log.d(LOG, e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            PlacesRecyclerViewAdapter adapter = new PlacesRecyclerViewAdapter(PlacesActivity.this, feedsPlaces);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnPlaceClickListener(new OnPlaceClickListener() {
                @Override
                public void onItemClick(FeedPlace item) {
                    Intent in = new Intent(getApplicationContext(), UsersActivity.class);
                    in.putExtra(TAG_PLACE_ID, item.getPlaceId());
                    in.putExtra(TAG_NAME, item.getName());
                    in.putExtra(TAG_PICTURE, item.getPlacePicture());
                    startActivity(in);
                }
            });
        }
    }
}
