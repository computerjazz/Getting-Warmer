package com.danielmerrill.gettingwarmer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ColorActivity extends AppCompatActivity {
    private DrawerLayout mDrawer;

    private ImageView mFriends;
    private TextView percentageDisplay;
    private TextView distanceDisplay;

    private GPSTracker gps;
    private String username;
    private String friendUsername;

    private double initialDist;
    private double currentDist;

    private double targetLatitude;
    private double targetLongitude;
    private double initialLatitude;
    private double initialLongitude;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = prefs.getString("username", "");

        mFriends = (ImageView) findViewById(R.id.friendsIcon);
        percentageDisplay = (TextView) findViewById(R.id.percentage_display);
        distanceDisplay = (TextView) findViewById(R.id.distance_display);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ActionsFragment actionsFragment = new ActionsFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            actionsFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, actionsFragment).commit();
        }






        View drawerView = findViewById(R.id.drawer_layout);
        if (drawerView != null && drawerView instanceof DrawerLayout) {
            mDrawer = (DrawerLayout)drawerView;
            mDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View view, float v) {

                }

                @Override
                public void onDrawerOpened(View view) {


                }

                @Override
                public void onDrawerClosed(View view) {

                }

                @Override
                public void onDrawerStateChanged(int i) {

                }
            });
        }

        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.LEFT);
            }
        });
    }



    private void updateDisplays() {
        percentageDisplay.setText(String.valueOf((int) getPercentageComplete() + "%"));
        distanceDisplay.setText(String.valueOf((int) getCurrentDistance() + " meters"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void setTargetLocation(double latitude, double longitude) {
        targetLatitude = latitude;
        targetLongitude = longitude;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
        TextView tv = (TextView) findViewById(R.id.users_title);
        tv.setText(username + " to " + friendUsername);
        checkLocation(friendUsername);


    }

    private void setCurrentCoordinates(double latitude, double longitude) {
        Toast.makeText(getApplicationContext(), latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        currentLatitude = latitude;
        currentLongitude = longitude;
    }

    public double getPercentageComplete() {
        initialDist = Utils.distFrom(initialLatitude, initialLongitude, targetLatitude, targetLongitude);
        currentDist = Utils.distFrom(currentLatitude, currentLongitude, targetLatitude, targetLongitude);
        return currentDist/initialDist;
    }

    public double getCurrentDistance() {
        return Utils.distFrom(currentLatitude, currentLongitude, targetLatitude, targetLongitude);
    }



    private void checkLocation(String friendUsername) {
        final String friend = friendUsername;
        gps = new GPSTracker(ColorActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            setCurrentCoordinates(latitude, longitude);

            //Creating Rest Services
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
            final RestInterface restInterface = adapter.create(RestInterface.class);

            //Calling method
            restInterface.getLocation(friend, username, new Callback<LoginModel>() {

                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //getlocation Success
                        if (model.getIsNew().equals("1")) { // check if location is new
                            Toast.makeText(getApplicationContext(), "New Pin Detected!", Toast.LENGTH_SHORT).show();

                        }
                        targetLatitude = Double.parseDouble(model.getLatitudeTarget());
                        targetLongitude = Double.parseDouble(model.getLongitudeTarget());

                        initialLatitude = Double.parseDouble(model.getLatitudeStart());
                        initialLongitude = Double.parseDouble(model.getLongitudeStart());


                        double diff = Utils.distFrom(targetLatitude, targetLongitude, latitude, longitude);
                        //distFromTarget.setText("Distance from " + friend + ": " + (int)diff + " meters");
                        Toast.makeText(getApplicationContext(), "Your lat: " + latitude + "\nTarget lat: " + targetLatitude + "\nYour lng:" + longitude + "\nTarget lng: " + targetLongitude +  "\nOff by " + diff + " meters", Toast.LENGTH_SHORT).show();
                        updateDisplays();


                    } else if (model.getStatus().equals("0")) {


                    } else if (model.getStatus().equals("2")) {

                    }




                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(getApplicationContext(), merror, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    // when logout button is clicked
    public void logOut() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", "");
        editor.commit();
        finish();
        Intent i = new Intent(ColorActivity.this, LoginActivity.class);
        startActivity(i);
    }
}
