package com.example.run18.withtoolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends FragmentActivity {
    static final String LOG = "LOG_LoginActivity";
    private static String url_db = "http://flirtodrom.miroplat.nichost.ru/api.php";
    private SharedPreferences ApiSettings;
    public static final String APP_PREFERENCES = "SETTINGS";
    public static final String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    private String ApiToken;
    private static final String[] sMyScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.PHOTOS,
            VKScope.NOHTTPS,
            VKScope.MESSAGES
    };
    private boolean isResumed = false;
    PagerAdapter sliderAdapter;
    ViewPager slider;
    static final int SLIDE_COUNT = 3;
    ImageButton vkLogin;
    ImageButton fbLogin;
    private ProgressBar progressBar;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        ApiSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // START: VK login
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                if (isResumed) {
                    switch (res) {
                        case LoggedOut:
                            if (ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
                                ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
                                if(ApiToken.length() > 0)
                                    startActivity(new Intent(getApplicationContext(), PlacesActivity.class));
                            }
                            break;
                        case LoggedIn:
                            if (ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
                                ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
                                if(ApiToken.length() > 0)
                                    startActivity(new Intent(getApplicationContext(), PlacesActivity.class));
                            }
                            break;
                        case Pending:
                            Log.d(LOG, "VK_LoginState: " + res);
                            break;
                        case Unknown:
                            Log.d(LOG, "VK_LoginState: " + res);
                            break;
                    }
                }
            }

            @Override
            public void onError(VKError error) { }
        });
        vkLogin = (ImageButton)findViewById(R.id.vk_icon);
        vkLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VKSdk.login(LoginActivity.this, sMyScope);
            }
        });
        // END: VK login

        // START: Facebook login
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {
                                Log.d("FACEBOOK_LOG", object.toString());
                                try {
                                    String user_id = object.getString("id");
                                    String user_name = object.getString("name");
                                    String user_birthday = object.getString("age_range");
                                    String user_gender = object.getString("gender");
                                    String user_picture = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                    new AuthUser().execute("facebook", user_id, user_name, user_birthday, user_gender, user_picture);
                                } catch(JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,age_range,picture.width(660).height(440),email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "on cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("FACEBOOK", exception.toString());
                LoginManager.getInstance().logOut();
            }
        });
        fbLogin = (ImageButton)findViewById(R.id.fb_icon);
        fbLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
            }
        });
        // END: Facebook login

        // START: Slider
        slider = (ViewPager) findViewById(R.id.pager);
        sliderAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        slider.setAdapter(sliderAdapter);
        slider.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(LOG, "onPageSelected, slider position: " + position);
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        // END: Slider

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            if (ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
                ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
                if(ApiToken.length() > 0)
                    startActivity(new Intent(this, PlacesActivity.class));
            }
        } else {
            if (ApiSettings.contains(APP_PREFERENCES_API_TOKEN)) {
                ApiToken = ApiSettings.getString(APP_PREFERENCES_API_TOKEN, "");
                if(ApiToken.length() > 0)
                    startActivity(new Intent(this, PlacesActivity.class));
            }
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                VKParameters parameters = new VKParameters();
                parameters.put("fields", "sex,bdate,photo_50,photo_100,photo_200_orig,photo_200,photo_400_orig,photo_max,photo_max_orig,nickname");
                VKRequest request = new VKRequest("users.get", parameters);
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        try {
                            JSONObject obj =  response.json.getJSONArray("response").getJSONObject(0);
                            Log.d("VK_LOG", obj.toString());

                            String user_id = obj.getString("id");
                            String user_name = obj.getString("first_name") + " " + obj.getString("nickname") + " " + obj.getString("last_name");
                            String user_birthday = obj.getString("bdate");
                            String user_gender = obj.getString("sex");
                            String user_picture = obj.getString("photo_max_orig");

                            new AuthUser().execute("vk", user_id, user_name, user_birthday, user_gender, user_picture);
                        } catch(JSONException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Log.d(LOG, "VK_get_params: "+error.toString());
                    }
                });
            }
            @Override
            public void onError(VKError error) {
                Log.d(LOG, "VK_get_params: "+error.toString());
            }
        };
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.d(LOG, "VK_get_params: "+resultCode);
        }
    }

    class AuthUser extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        protected Integer doInBackground(String[] params) {
            Log.d(LOG, "Connect: START");
            try {
                // service, user_id, user_name, user_birthday, user_gender, user_picture
                Request request = new Request.Builder().url(url_db + "?action=login" +
                        "&firebase_instance_id=" + FirebaseInstanceId.getInstance().getToken() +
                        "&service=" + params[0] +
                        "&user_id=" + params[1] +
                        "&user_name=" + params[2] +
                        "&user_birthday=" + params[3] +
                        "&user_gender=" + params[4] +
                        "&user_picture=" + params[5]
                ).build();
                Response response = new OkHttpClient().newCall(request).execute();
                String result = response.body().string();

                Log.d(LOG, result);
                JSONObject resObj = new JSONObject(result);

                if(resObj.getString("TYPE").equals("OK")) {
                    SharedPreferences.Editor editor = ApiSettings.edit();
                    editor.putString(APP_PREFERENCES_API_TOKEN, resObj.getString("API_TOKEN"));
                    editor.apply();
                    return 1;
                } else {
                    Log.d(LOG, "Connect: Unknown error");
                    return 0;
                }
            } catch (Exception e) {
                Log.d(LOG, "Connect: "+e.toString());
                return 0;
            }
        }
        protected void onPostExecute(Integer result) {
            //progressBar.setVisibility(View.GONE);
            if(result.equals(1)) {
                startActivity(new Intent(getApplicationContext(), PlacesActivity.class));
            }
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return SliderFragment.newInstance(position);
        }
        @Override
        public int getCount() {
            return SLIDE_COUNT;
        }
    }
}