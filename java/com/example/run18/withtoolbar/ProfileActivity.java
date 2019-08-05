package com.example.run18.withtoolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final String LOG = "LOG_ProfileActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;

    private String user_id;
    private String user_name;
    private static final String TAG_USER_ID = "id";
    private static final String TAG_USER_NAME = "name";

    private TextView viewName;
    private TextView viewBirthday;
    private ImageView viewPicture;
    private TextView viewAbout;
    private ProgressBar progressBar;

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
        setContentView(R.layout.profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        viewName = (TextView) findViewById(R.id.name);
        viewPicture = (ImageView) findViewById(R.id.picture);
        viewAbout = (TextView) findViewById(R.id.about_text);

        Intent i = getIntent();
        user_id = i.getStringExtra(TAG_USER_ID);
        user_name = i.getStringExtra(TAG_USER_NAME);
        setTitle(user_name);
        new GetProfile().execute(user_id);
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
        getMenuInflater().inflate(R.menu.profile_toolbar, menu);
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
            startActivity(new Intent(this, ProfileEditActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_list_places) {
            startActivity(new Intent(this, PlacesActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_dialogs) {
            startActivity(new Intent(this, DialogsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if(id == R.id.nav_help) {
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

    private class GetProfile extends AsyncTask<String, JSONObject, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(String... user_id) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=get_user_by_id&id=" + user_id[0]).build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {
                    JSONArray users = resObj.getJSONArray("DATA");
                    for(int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);

                        return c;
                    }
                } else if(resObj.getString("DATA").equals("unknown token")) {
                    SharedPreferences.Editor editor = ApiSettings.edit();
                    editor.putString(APP_PREFERENCES_API_TOKEN, "");
                    editor.apply();
                }
            } catch (Exception e) {
                Log.d(LOG, "Connect: "+e.toString());
            }
            return null;
        }

        protected void onPostExecute(JSONObject user) {
            progressBar.setVisibility(View.GONE);
            try {
                String birthday = user.getString("birthday");
                Calendar calendarBirthday = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                calendarBirthday.setTime(sdf.parse(birthday));
                Calendar calendarNow = Calendar.getInstance();
                int yearNow = calendarNow.get(Calendar.YEAR);
                int yearBirthday = calendarBirthday.get(Calendar.YEAR);
                int years = yearNow - yearBirthday;

                if(user_name == null)
                    setTitle(user.getString("name")+", "+years);

                viewName.setText(user.getString("name")+", "+years);
                viewAbout.setText(user.getString("about"));
                Picasso.with(getApplicationContext()).load(user.getString("picture"))
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(viewPicture);
            } catch (Exception e) {
                Log.d(LOG, "Connect: "+e.toString());
            }
        }
    }
}
