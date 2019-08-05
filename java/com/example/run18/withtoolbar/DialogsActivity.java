package com.example.run18.withtoolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DialogsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    static final String LOG = "LOG_DialogsActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;
    private static final String tagUserId = "user_id";
    private static final String tagUserName = "user_name";
    private static final String tagUserPicture = "user_picture";
    private static final String tagLastMessage = "last_message";
    private static final String tagLastMessageTimestamp = "last_message_timestamp";

    private List<FeedDialog> feedsDialogs;
    private RecyclerView mRecyclerView;
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
        setContentView(R.layout.dialogs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        new GetDialogs().execute();
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
        getMenuInflater().inflate(R.menu.dialogs_toolbar, menu);
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

    private class GetDialogs extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... params) {
            Log.d(LOG, "Connect: START1");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=get_dialogs").build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {
                    JSONArray dialogs = resObj.getJSONArray("DATA");
                    feedsDialogs = new ArrayList<>();
                    for(int i = 0; i < dialogs.length(); i++) {
                        JSONObject c = dialogs.getJSONObject(i);

                        FeedDialog item = new FeedDialog();
                        item.setUserId(c.optString(tagUserId));
                        item.setUserName(c.optString(tagUserName));
                        item.setUserPicture(c.optString(tagUserPicture));
                        item.setLastMessage(c.optString(tagLastMessage));
                        item.setLastMessageTimestamp(c.optString(tagLastMessageTimestamp));
                        feedsDialogs.add(item);
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
            DialogsRecyclerViewAdapter adapter = new DialogsRecyclerViewAdapter(DialogsActivity.this, feedsDialogs);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onItemClick(FeedDialog item) {
                    Intent in = new Intent(getApplicationContext(), MessagesActivity.class);
                    in.putExtra(tagUserId, item.getUserId());
                    in.putExtra(tagUserName, item.getUserName());
                    startActivity(in);
                }
            });
        }
    }
}
