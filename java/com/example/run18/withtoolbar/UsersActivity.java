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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsersActivity extends AppCompatActivity {
    static final String LOG = "LOG_ListUsersActivity";
    private static String url_db;
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;
    private String place_id;
    private String place_name;
    private static final String TAG_PLACE_ID = "id";
    private static final String TAG_USER_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_PICTURE = "picture";
    private static final String TAG_BIRTHDAY = "birthday";
    private static final String TAG_LIKE = "like";
    private static final String TAG_LIKE_TOO = "like_too";

    private List<FeedUser> feedsUsers;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;

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
        setContentView(R.layout.users);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Intent i = getIntent();
        place_id = i.getStringExtra(TAG_PLACE_ID);
        place_name = i.getStringExtra(TAG_NAME);
        String place_picture = i.getStringExtra(TAG_PICTURE);
        ImageView image = (ImageView) findViewById(R.id.place_picture);
        if(!TextUtils.isEmpty(place_picture)) {
            Picasso.with(this).load(place_picture)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(image);
        }
        setTitle(place_name);
        new GetListUsers().execute(place_id);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class GetListUsers extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=get_checked_users&id=" + place_id).build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {
                    JSONArray users = resObj.getJSONArray("DATA");
                    feedsUsers = new ArrayList<>();
                    for(int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);

                        FeedUser item = new FeedUser();
                        item.setUserId(c.optString(TAG_USER_ID));

                        String birthday = c.optString(TAG_BIRTHDAY);
                        Calendar calendarBirthday = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        calendarBirthday.setTime(sdf.parse(birthday));
                        Calendar calendarNow = Calendar.getInstance();
                        int yearNow = calendarNow.get(Calendar.YEAR);
                        int yearBirthday = calendarBirthday.get(Calendar.YEAR);
                        int years = yearNow - yearBirthday;

                        item.setName(c.optString(TAG_NAME));
                        item.setAge(years);
                        item.setPicture(c.optString(TAG_PICTURE));
                        item.setLikeStatus(c.optString(TAG_LIKE));
                        item.setLikeStatusToo(c.optString(TAG_LIKE_TOO));
                        feedsUsers.add(item);
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
            UsersRecyclerViewAdapter adapter = new UsersRecyclerViewAdapter(UsersActivity.this, feedsUsers);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnUserClickListener(new OnUserClickListener() {
                @Override
                public void onItemClick(FeedUser item) {
                    if(item.getLikeStatus().equals("1")) {
                        if(item.getLikeStatusToo().equals("1")) {
                            Intent in = new Intent(getApplicationContext(), MessagesActivity.class);
                            in.putExtra("user_id", item.getUserId());
                            in.putExtra("user_name", item.getName());
                            startActivity(in);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Вам пока еще не ответили лайком.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Поставьте лайк, чтобы начать переписку.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
            adapter.setOnLikeListener(new OnLikeListener() {
                @Override
                public void onItemClick(FeedUser item, UsersRecyclerViewAdapter.CustomViewHolder customViewHolder) {
                    if(customViewHolder.like_status.getText().equals("0")) {
                        customViewHolder.user_like_button.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                        item.setLikeStatus("1");
                        customViewHolder.like_status.setText("1");
                        new Like().execute(customViewHolder.user_id.getText().toString(), "1");
                    } else {
                        customViewHolder.user_like_button.setBackgroundResource(R.drawable.ic_favorite_border_black_24dp);
                        item.setLikeStatus("0");
                        customViewHolder.like_status.setText("0");
                        new Like().execute(customViewHolder.user_id.getText().toString(), "0");
                    }
                }
            });
        }
    }

    private class Like extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url_db + "&action=like&receiver_id=" + params[0] + "&like=" + params[1]).build();
                Response response = client.newCall(request).execute();
                String result = response.body().string();

                JSONObject resObj = new JSONObject(result);
                if(resObj.getString("TYPE").equals("OK")) {
                } else {
                }
            } catch (Exception e) {
                Log.d(LOG, "Connect2: "+e.toString());
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
