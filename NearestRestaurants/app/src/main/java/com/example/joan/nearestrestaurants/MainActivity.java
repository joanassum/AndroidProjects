package com.example.joan.nearestrestaurants;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&keyword=cruise&key=AIzaSyB02LSTA9AvtvAma_3a1u-2KfNFS0HpJPM
    private LocationManager locationManager;
    private LocationListener locationListener;
    ArrayList<String> mNames = new ArrayList<>();
    ArrayAdapter mArrayAdapter;
    SQLiteDatabase mDatabase;
    private final String raduis = "5000"; //within 5 km
    private final String apiKey =  "AIzaSyB02LSTA9AvtvAma_3a1u-2KfNFS0HpJPM";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();
                double lng = lastKnownLocation.getLongitude();

                String apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + lat + "," + lng + "&radius=" + raduis + "&type=restaurant&key=" + apiKey;

                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(apiUrl, lat + "", lng + "");

                updateListView(lat, lng);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        mNames.add("Loading");
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mNames);
        listView.setAdapter(mArrayAdapter);

        Log.i("Database", "creating");
        mDatabase = this.openOrCreateDatabase("Restaurants", MODE_PRIVATE, null);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS restaurants " +
                "(id INTEGER PRIMARY KEY, name VARCHAR,  lat DOUBLE(3, 9), lng DOUBLE(3, 9))");

        Log.i("Database", "sucessfully created");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                String apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + lat + "," + lng + "&radius=" + raduis + "&type=restaurant&key=" + apiKey;

                Log.i("Dwonload", "Downloading");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(apiUrl, lat + "", lng + "");
                Log.i("Download", "successful");

                updateListView(lat, lng);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();
                double lng = lastKnownLocation.getLongitude();

                String apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + lat + "," + lng + "&radius=" + raduis + "&type=restaurant&key=" + apiKey;

                Log.i("Dwonload", "Downloading");
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(apiUrl, lat + "", lng + "");
                Log.i("Download", "successful");

                updateListView(lat, lng);
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void updateListView(double myLat, double myLng) {

        Cursor c = mDatabase.rawQuery("SELECT * FROM restaurants", null);
        int nameIndex = c.getColumnIndex("name");
        int latIndex = c.getColumnIndex("lat");
        int lngIndex = c.getColumnIndex("lng");

        if (c.moveToFirst()) {
            mNames.clear();

            do {
                double lat = c.getDouble(latIndex);
                double lng = c.getDouble(lngIndex);
                float[] result = new float[10];
                Location.distanceBetween(myLat, myLng, lat, lng, result);
                mNames.add(c.getString(nameIndex) + " (" + (result[0] / 1000) + " km)");
            } while (c.moveToNext());

            mArrayAdapter.notifyDataSetChanged();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        private double lat;
        private double lng;

        @Override
        protected String doInBackground(String... params) {

            lat = Double.parseDouble(params[1]);
            lng = Double.parseDouble(params[2]);

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    result += (char) data;
                    data = reader.read();
                }

                Log.i("restaurants", result);

                JSONObject resultObject = new JSONObject(result);

                String resultList = resultObject.getString("results");

                JSONArray jsonArray = new JSONArray(resultList);
                int maxNumOfItem = 20;

                mDatabase.execSQL("DELETE FROM restaurants");

                for (int i = 0; i < Math.min(jsonArray.length(), maxNumOfItem); i++) {
                    String restaurantInfo = jsonArray.getString(i);

                    JSONObject jsonObject = new JSONObject(restaurantInfo);

                    if (!jsonObject.isNull("name") && !jsonObject.isNull("geometry")) {
                        String restaurantName = jsonObject.getString("name");
                        String restaurantGeometry = jsonObject.getString("geometry");
                        String latStr = "";
                        String lngStr = "";
                        JSONObject geoObject = new JSONObject(restaurantGeometry);
                        if (!geoObject.isNull("location")) {
                            String restaurantLocation = geoObject.getString("location");
                            JSONObject locationObject = new JSONObject(restaurantLocation);
                            if (!locationObject.isNull("lat") && !locationObject.isNull("lng")) {
                                latStr = locationObject.getString("lat");
                                lngStr = locationObject.getString("lng");

                                String sql = "INSERT INTO restaurants (name, lat, lng) VALUES ( ?, ?,?)";
                                SQLiteStatement statement = mDatabase.compileStatement(sql);

                                statement.bindString(1, restaurantName);
                                statement.bindString(2, latStr);
                                statement.bindString(3, lngStr);

                                statement.execute();
                                Log.i("no. of storiesloaded: ", "" + i);
                            }
                        }
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView(lat, lng);
        }
    }
}
