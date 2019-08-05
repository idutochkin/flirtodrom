package com.example.run18.withtoolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileEditActivity extends AppCompatActivity {
    public String LOG = "Log_ProfileEditActivity";
    public String url_db;
    public ProgressBar progressBar;
    public EditText viewName;
    public TextView viewBirthday;
    public ImageView viewPicture;
    public TextView viewPictureURI;
    public EditText viewAbout;
    Calendar dateAndTime = Calendar.getInstance();

    public SharedPreferences ApiSettings;
    public String ApiToken;
    public String APP_PREFERENCES = "SETTINGS";
    public String APP_PREFERENCES_API_TOKEN = "API_TOKEN";
    public JSONObject user_data;

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_GALLERY = 2;

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
        setContentView(R.layout.profile_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        viewName = (EditText) findViewById(R.id.name);
        viewBirthday = (TextView) findViewById(R.id.birthday);
        viewPicture = (ImageView) findViewById(R.id.picture);
        viewPictureURI = (TextView) findViewById(R.id.pictureURI);
        viewAbout = (EditText) findViewById(R.id.about_text);

        viewPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_GALLERY);
            }
        });

        new GetProfile().execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_edit_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_apply) {
            user_data = new JSONObject();
            try {
                user_data.put("name", viewName.getText());
                user_data.put("birthday", viewBirthday.getText());
                user_data.put("picture", viewPictureURI.getText());
                user_data.put("about", viewAbout.getText());
                String edit_user_data = user_data.toString();

                new EditProfile().execute(edit_user_data);
            } catch (JSONException e) {
                Log.d(LOG, "Connect: "+e.toString());
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Bitmap bitmap = null;
        ImageView picture = (ImageView) findViewById(R.id.picture);

        switch(requestCode) {
            case REQUEST_GALLERY:
                if(resultCode == RESULT_OK){
                    Uri imageUri = imageReturnedIntent.getData();
                    viewPictureURI.setText(imageUri.toString());
                    if(checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        picture.setImageBitmap(bitmap);
                    }
                }
        }
    }
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public void setDate(View v) {
        new DatePickerDialog(ProfileEditActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }
    private void setInitialDateTime() {
        viewBirthday.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setInitialDateTime();
        }
    };

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
                Request request = new Request.Builder().url(url_db + "&action=get_user_by_id&id=").build();
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
                viewName.setText(user.getString("name"));
                viewBirthday.setText(user.getString("birthday"));
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

    private class EditProfile extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String[] edit_user_data) {
            Log.d(LOG, "Connect: START edit");
            try {
                String picture = new JSONObject(edit_user_data[0]).getString("picture");
                if(picture.isEmpty()) {
                    Request request = new Request.Builder().url(url_db + "&action=edit_user&user_data=" + edit_user_data[0]).build();
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).execute();
                } else {
                    File file = new File(getRealPathFromURI_API19(getApplicationContext(), Uri.parse(picture)));
                    String contentType = file.toURL().openConnection().getContentType();
                    RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);
                    final String filename = "file_" + System.currentTimeMillis() / 1000L;

                    Request request = new Request.Builder()
                            .url(url_db + "&action=edit_user")
                            .post(
                                    new MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart("user_data", edit_user_data[0])
                                            .addFormDataPart("picture", filename + ".jpg", fileBody)
                                            .build()
                            )
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("IMAGE_UPLOAD_ERROR", e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.d("IMAGE_UPLOAD_GOOD", response.body().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.d("IMAGE_UPLOAD_GOOD", e.getMessage());
                                    }
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(LOG, "ERROR edit: "+e.toString());
            }
            return null;
        }

        protected void onPostExecute(Void user) {
            progressBar.setVisibility(View.GONE);
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
    }
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        try { // FIXME NPE error when select image from QuickPic, Dropbox etc
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();

            return filePath;
        } catch (Exception e) { // this is the fix lol
            String result;
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = uri.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
            return result;
        }
    }
}