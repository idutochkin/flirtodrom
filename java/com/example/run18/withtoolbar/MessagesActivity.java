package com.example.run18.withtoolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MessagesActivity extends AppCompatActivity {
    static final String LOG = "LOG_MessagesActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;
    private String user_id;
    private String user_name;
    private String message;
    private static final String tagUserId = "user_id";
    private static final String tagUserName = "user_name";
    private static final String TAG_USER_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_PICTURE = "picture";
    private static final String TAG_TYPE = "type";
    private static final String TAG_DATE_CREATE = "date_create";

    private List<FeedMessage> feedsMessages;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private static boolean activityVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
            ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
            if(ApiToken.length() > 0) {
                url_db = "http://flirtodrom.miroplat.nichost.ru/api.php?api_token="+ApiToken;
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        setContentView(R.layout.messages);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Intent i = getIntent();
        user_id = i.getStringExtra(tagUserId);
        user_name = i.getStringExtra(tagUserName);
        setTitle(user_name);

        new CreateDialog().execute(user_id);
        new GetListMessages().execute(user_id);


        Button add_message_btn = (Button) findViewById(R.id.add_message_btn);
        add_message_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText add_message = (EditText) findViewById(R.id.add_message);
                message = add_message.getText().toString();
                new SendMessage().execute(message, user_id);
                add_message.getText().clear();
                new GetListMessages().execute(user_id);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessagesActivity.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessagesActivity.activityPaused();
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class CreateDialog extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=create_dialog&receiver_id=" + user_id).build();
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

        protected void onPostExecute(Void users) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private class GetListMessages extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=get_messages&id=" + user_id).build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();

                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {
                    JSONArray data = resObj.getJSONArray("DATA");
                    JSONObject users = resObj.getJSONObject("USERS");
                    feedsMessages = new ArrayList<>();
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);

                        FeedMessage item = new FeedMessage();
                        item.setMessage(c.optString(TAG_MESSAGE));

                        String date_create = c.optString(TAG_DATE_CREATE);
                        Calendar calendarTimestamp = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        calendarTimestamp.setTime(sdf.parse(date_create));
                        int date = calendarTimestamp.get(Calendar.DATE);
                        int month = calendarTimestamp.get(Calendar.MONTH);
                        int hour = calendarTimestamp.get(Calendar.HOUR_OF_DAY);
                        int minute = calendarTimestamp.get(Calendar.MINUTE);

                        month = month+1;

                        item.setDateCreate(String.format("%02d:%02d %02d.%02d", hour, minute, date, month));
                        item.setType(c.optString(TAG_TYPE));
                        if(c.optString(TAG_TYPE).equals("in")) {
                            item.setPicture(users.getJSONObject("TO_MESSAGE").getString(TAG_PICTURE));
                        } else {
                            item.setPicture(users.getJSONObject("CURRENT").getString(TAG_PICTURE));
                        }
                        feedsMessages.add(item);
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

        protected void onPostExecute(Void users) {
            progressBar.setVisibility(View.GONE);
            MessageRecyclerViewAdapter adapter = new MessageRecyclerViewAdapter(MessagesActivity.this, feedsMessages);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnMessageClickListener(new OnMessageClickListener() {
                @Override
                public void onItemClick(FeedMessage item) {
                    Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                    in.putExtra(TAG_USER_ID, user_id);
                    in.putExtra(TAG_NAME, user_name);
                    startActivity(in);
                }
            });
            mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount()-1);
        }
    }

    private class SendMessage extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=add_message&message=" + message + "&receiver_id=" + user_id).build();
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

        protected void onPostExecute(Void users) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
