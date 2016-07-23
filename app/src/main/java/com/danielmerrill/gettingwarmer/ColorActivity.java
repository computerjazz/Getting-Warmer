package com.danielmerrill.gettingwarmer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ColorActivity extends AppCompatActivity  {

    private int TIMER_TICKS_BETWEEN_RING_DISPLAY = 10;

    private DrawerLayout mDrawer;

    private Timer myTimer;

    private ImageView mFriends;
    private TextView percentageDisplay;
    private TextView distanceDisplay;
    private TextView initialDisplay;
    private RelativeLayout mainColorDisplay;

    private ImageView ringView;
    private boolean scaleToggle;

    private GPSTracker gps;

    private String username;
    private String friendUsername;

    private double initialDist;
    private double lastDist;
    private double currentDist;
    private boolean youAreCloser;
    private int locationTickCounter;

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
        friendUsername = prefs.getString("friendUsername", "");
        if (friendUsername.length() > 0) {
            setFriendUsername(friendUsername);
        }

        mFriends = (ImageView) findViewById(R.id.friendsIcon);
        percentageDisplay = (TextView) findViewById(R.id.percentage_display);
        distanceDisplay = (TextView) findViewById(R.id.distance_display);
        initialDisplay = (TextView) findViewById(R.id.initial_display);
        mainColorDisplay = (RelativeLayout) findViewById(R.id.main_color_display);
        ringView = (ImageView) findViewById(R.id.ringView);
        locationTickCounter = 0;




        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout


        View drawerView = findViewById(R.id.drawer_layout);
        if (drawerView != null && drawerView instanceof DrawerLayout) {
            mDrawer = (DrawerLayout)drawerView;
        }

        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.LEFT);
            }
        });

        mainColorDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayRing();
            }
        });

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                ActionsFragment actionsFragment = new ActionsFragment();
                actionsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, actionsFragment).commit();
            }
        }
    }

    private void displayRing() {
        if (youAreCloser) {
            ringView.getDrawable().setColorFilter(Color.MAGENTA, PorterDuff.Mode.MULTIPLY);
        } else {
            ringView.getDrawable().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
        }

        ringView.setScaleX(.1f);
        ringView.setScaleY(.1f);
        ringView.setAlpha(1f);
        ringView.animate().scaleX(4).scaleY(4).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(1500).withEndAction(hideRing).start();

                /*
                final ShapeDrawable circle = new ShapeDrawable(new OvalShape());
                circle.setIntrinsicWidth (20);
                circle.setIntrinsicHeight (20);
                circle.getPaint().setStyle(Paint.Style.STROKE);
                circle.getPaint().setColor(getResources().getColor(R.color.white));
                circle.getPaint().setStrokeWidth(5.0f);

                ValueAnimator animation = ValueAnimator.ofFloat(0.0f,1.0f);
                animation.setDuration(1500);

                animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Float tickValue = (Float)animation.getAnimatedValue();
                        int padding = (int)(tickValue*100);
                        int sideLength = (int)(tickValue*3000);

                        ringView.getLayoutParams().height = sideLength;
                        ringView.getLayoutParams().width = sideLength;


                        ringView.setImageDrawable(circle);
                        percentageDisplay.setText(animation.getAnimatedValue().toString());
                    }
                });

                animation.start();
                */
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTimer.cancel();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void TimerMethod()
    {
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            //R uns in the same thread as the UI
            //Toast.makeText(getApplicationContext(), String.valueOf(locationTickCounter), Toast.LENGTH_SHORT).show();
            checkLocation(friendUsername);
            if (++locationTickCounter >= TIMER_TICKS_BETWEEN_RING_DISPLAY) {
                youAreCloser = (currentDist < lastDist) ? true : false;
                if (currentDist != lastDist) {
                    displayRing();
                }
                lastDist = currentDist;
                locationTickCounter = 0;
            }

        }
    };

    private Runnable hideRing = new Runnable() {
        public void run() {
            //This method runs in the same thread as the UI.
            ringView.animate().alpha(0).setDuration(1000).start();

        }
    };

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_homepage, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_logout:
                        logOut();
                        return true;

                    default:
                        return false;

                }
            }
        });
    }






    private void updateDisplays() {
        percentageDisplay.setText(String.valueOf(getPercentageComplete() + "%"));
        distanceDisplay.setText(String.valueOf((int) getCurrentDistance() + "m"));
        initialDisplay.setText(String.valueOf((int) getInitialDistance() + "m"));
        int blue = (int) ((getPercentageComplete()/100.0) * 255);
        if (blue > 255) {
            blue = 255;
        }

        int red = 255 - blue;

        mainColorDisplay.setBackgroundColor(Color.rgb(red,0,blue));
    }

    private void setBlankBackground() {
        mainColorDisplay.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
    }



    public void setTargetLocation(double latitude, double longitude) {
        targetLatitude = latitude;
        targetLongitude = longitude;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("friendUsername", friendUsername);
        editor.commit();
        TextView tv = (TextView) findViewById(R.id.users_title);
        tv.setText(friendUsername);
        checkLocation(friendUsername);


    }

    private void setCurrentCoordinates(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
    }

    public int getPercentageComplete() {

        initialDist = Utils.distFrom(initialLatitude, initialLongitude, targetLatitude, targetLongitude);
        currentDist = Utils.distFrom(currentLatitude, currentLongitude, targetLatitude, targetLongitude);
        return (int) ((currentDist/initialDist) * 100);
    }

    public double getCurrentDistance() {
        return Utils.distFrom(currentLatitude, currentLongitude, targetLatitude, targetLongitude);
    }

    public double getInitialDistance() {
        return Utils.distFrom(initialLatitude, initialLongitude, targetLatitude, targetLongitude);
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
            restInterface.getLocation(username, friend, new Callback<LoginModel>() {

                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //getlocation Success

                        if (model.getLatitudeTarget() == null && model.getLongitudeTarget() == null) {
                            Toast.makeText(getApplicationContext(), "Target not set!", Toast.LENGTH_SHORT).show();
                            setBlankBackground();
                            myTimer.cancel();
                        } else {

                            targetLatitude = Double.parseDouble(model.getLatitudeTarget());
                            targetLongitude = Double.parseDouble(model.getLongitudeTarget());
                            if (model.getIsNew().equals("1")) { // check if location is new
                                Toast.makeText(getApplicationContext(), "New Pin Detected!", Toast.LENGTH_SHORT).show();
                                initialLatitude = latitude;
                                initialLongitude = longitude;
                                setInitialLocation(friend, targetLatitude, targetLongitude, currentLatitude, currentLongitude);

                            } else {
                                initialLatitude = Double.parseDouble(model.getLatitudeStart());
                                initialLongitude = Double.parseDouble(model.getLongitudeStart());

                            }

                            double diff = Utils.distFrom(targetLatitude, targetLongitude, latitude, longitude);
                            //distFromTarget.setText("Distance from " + friend + ": " + (int)diff + " meters");
                            //Toast.makeText(getApplicationContext(), "Your lat: " + latitude + "\nTarget lat: " + targetLatitude + "\nYour lng:" + longitude + "\nTarget lng: " + targetLongitude +  "\nOff by " + diff + " meters", Toast.LENGTH_SHORT).show();
                            updateDisplays();

                        }

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

    private void setInitialLocation(String friendUsername, double targetLatitude, double targetLongitude, double currentLatitude, double currentLongitude) {

        final String friend = friendUsername;

        //Creating Rest Services
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method
        restInterface.setLocation(username, friend, targetLatitude, targetLongitude, currentLatitude, currentLongitude, new Callback<LoginModel>() {

            @Override
            public void success(LoginModel model, Response response) {
                if (model.getStatus().equals("1")) {  //setlocation Success
                    Toast.makeText(getApplicationContext(), "Set initial location to " + friend, Toast.LENGTH_SHORT).show();
                } else if (model.getStatus().equals("0")) {
                } else if (model.getStatus().equals("2")) {
                }
            }

            @Override
            public void failure(RetrofitError error) {

                String merror = error.getMessage();
                Toast.makeText(getApplicationContext(), merror, Toast.LENGTH_LONG).show();
            }
        });
        // Toast.makeText(getActivity().getApplicationContext(), "Setting location - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
    }

    // when logout button is clicked
    public void logOut() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", "");
        editor.putString("friendUsername", "");
        editor.commit();
        myTimer.cancel();
        finish();
        Intent i = new Intent(ColorActivity.this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
