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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    static final String LOG = "LOG_SettingsActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;

    private ProgressBar progressBar;
    CheckBox in_search;
    CheckBox search_man;
    CheckBox search_woman;
    EditText search_min_age;
    EditText search_max_age;
    Switch notifications_likes;
    Switch notifications_messages;
    String[] set_settings;

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
        setContentView(R.layout.settings);

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
        in_search = (CheckBox) findViewById(R.id.in_search);
        search_man = (CheckBox) findViewById(R.id.search_man);
        search_woman = (CheckBox) findViewById(R.id.search_woman);
        search_min_age = (EditText) findViewById(R.id.search_min_age);
        search_max_age = (EditText) findViewById(R.id.search_max_age);
        notifications_likes = (Switch) findViewById(R.id.notifications_likes);
        notifications_messages = (Switch) findViewById(R.id.notifications_messages);

        new GetSettings().execute();
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
        getMenuInflater().inflate(R.menu.settings_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            set_settings = new String[7];
            if(in_search.isChecked())
                set_settings[0] = "1";
            else
                set_settings[0] = "0";
            if(search_man.isChecked())
                set_settings[1] = "1";
            else
                set_settings[1] = "0";
            if(search_woman.isChecked())
                set_settings[2] = "1";
            else
                set_settings[2] = "0";
            set_settings[3] = search_min_age.getText().toString();
            set_settings[4] = search_max_age.getText().toString();
            if(notifications_likes.isChecked())
                set_settings[5] = "1";
            else
                set_settings[5] = "0";
            if(notifications_messages.isChecked())
                set_settings[6] = "1";
            else
                set_settings[6] = "0";

            new SetSettings().execute(set_settings);
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
            Intent link = new Intent(Intent.ACTION_VIEW, address);
            startActivity(link);
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

    private class GetSettings extends AsyncTask<String, JSONObject, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected JSONObject doInBackground(String... user_id) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=get_settings").build();
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
                search_min_age.setText(user.getString("search_min_age"));
                search_max_age.setText(user.getString("search_max_age"));
                if(user.getInt("in_search") == 1)
                    in_search.setChecked(true);
                if(user.getInt("search_man") == 1)
                    search_man.setChecked(true);
                if(user.getInt("search_woman") == 1)
                    search_woman.setChecked(true);
                if(user.getInt("notifications_likes") == 1)
                    notifications_likes.setChecked(true);
                if(user.getInt("notifications_messages") == 1)
                    notifications_messages.setChecked(true);
            } catch (Exception e) {
                Log.d(LOG, "Connect: "+e.toString());
            }
        }
    }

    private class SetSettings extends AsyncTask<String, String[], String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String[] doInBackground(String[] settings) {
            Log.d(LOG, "Connect: START");
            try {
                JSONArray m = new JSONArray(settings);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=set_settings&settings="+m).build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {

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

        protected void onPostExecute(String[] user) {
            progressBar.setVisibility(View.GONE);
            try {
                Toast.makeText(SettingsActivity.this, "Настройки сохранены.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d(LOG, "Connect: "+e.toString());
            }
        }
    }
}
