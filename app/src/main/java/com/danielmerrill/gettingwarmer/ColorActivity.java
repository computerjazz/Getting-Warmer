package com.danielmerrill.gettingwarmer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ColorActivity extends AppCompatActivity  {

    private int TIMER_TICKS_BETWEEN_RING_DISPLAY = 10;
    private int TIME_BETWEEN_TIMER_TICKS = 1000;

    // Meters away "On Fire" message displays
    private int WIN_DISTANCE = 5;
    private int MAX_COLOR_RANGE = 195;
    private int MIN_COLOR_VALUE = 60;

    private ActionsFragment actionsFragment;

    private DrawerLayout mDrawer;

    private Timer myTimer;

    private ImageView mFriends;
    private TextView percentageDisplay;
    private TextView distanceDisplay;
    private TextView initialDisplay;
    private TextView friendUsernameTitle;
    private ImageView winMessage;

    private RelativeLayout mainColorDisplay;

    private ImageView ringView;
    private boolean detailViewToggle;
    private boolean isInGame;

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

    private SharedPreferences prefs;
    private PopupMenu popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = prefs.getString("username", "");


        if (username.length() == 0) {
            finish();
            Intent i = new Intent(ColorActivity.this, LoginActivity.class);
            startActivity(i);

        }

        friendUsername = prefs.getString("friendUsername", "");
        if (friendUsername.length() > 0) {
            setFriendUsername(friendUsername);
        }

        mFriends = (ImageView) findViewById(R.id.friendsIcon);
        percentageDisplay = (TextView) findViewById(R.id.percentage_display);
        distanceDisplay = (TextView) findViewById(R.id.distance_display);
        initialDisplay = (TextView) findViewById(R.id.initial_display);
        friendUsernameTitle = (TextView) findViewById(R.id.users_title);
        winMessage = (ImageView) findViewById(R.id.win);

        mainColorDisplay = (RelativeLayout) findViewById(R.id.main_color_display);
        ringView = (ImageView) findViewById(R.id.ringView);
        locationTickCounter = 0;

        reset();


        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout


        View drawerView = findViewById(R.id.drawer_layout);
        if (drawerView != null && drawerView instanceof DrawerLayout) {
            mDrawer = (DrawerLayout) drawerView;
        }

        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.LEFT);
            }
        });

        // Set up hidden distance displays
        friendUsernameTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int visibility = detailViewToggle ? View.INVISIBLE : View.VISIBLE;
                detailViewToggle = !detailViewToggle;

                percentageDisplay.setVisibility(visibility);
                distanceDisplay.setVisibility(visibility);
                initialDisplay.setVisibility(visibility);

                return false;
            }
        });


        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                actionsFragment = new ActionsFragment();
                actionsFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, actionsFragment).commit();
            }
        }
    }

    private void displayRing() {
        if (youAreCloser) {
            ringView.getDrawable().setColorFilter(getResources().getColor(R.color.warmer), PorterDuff.Mode.MULTIPLY);
        } else {
            ringView.getDrawable().setColorFilter(getResources().getColor(R.color.colder), PorterDuff.Mode.MULTIPLY);
        }
        // to avoid pixellation, ring is created full-screen
        // then scaled-down before the animation expands it
        ringView.setScaleX(.1f);
        ringView.setScaleY(.1f);
        ringView.setAlpha(1f);
        ringView.animate().scaleX(4).scaleY(4).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(1500).withEndAction(hideRing).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (myTimer != null) {
            myTimer.cancel();
        }
        createAndStartTimer();
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


    private void createAndStartTimer() {
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, TIME_BETWEEN_TIMER_TICKS);
    }

    private void TimerMethod()
    {
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            //R uns in the same thread as the UI
            //Toast.makeText(getApplicationContext(), String.valueOf(locationTickCounter), Toast.LENGTH_SHORT).show();
            if (isInGame) {
                checkLocation(friendUsername);
                checkWinCondition();
            }
            triggerPeriodicUpdates();
        }
    };


    private void triggerPeriodicUpdates() {
        if (++locationTickCounter >= TIMER_TICKS_BETWEEN_RING_DISPLAY) {
            if (isInGame) {
                checkIfCloserAndDisplayRing();

            }

            refreshFriendsLists();
            locationTickCounter = 0;
        }
    }


    private boolean checkWinCondition() {
        if (currentDist < WIN_DISTANCE) {
            if (prefs.getBoolean("vibrate", true)) {
                vibrateOnFire();
            }

            winMessage.setVisibility(View.VISIBLE);
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            winMessage.startAnimation(shake);
            return true;
        } else {
            winMessage.setVisibility(View.INVISIBLE);
            return false;
        }
    }

    private void vibrateOnFire() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        int dot = 200;      // Length of a Morse Code "dot" in milliseconds
        int dash = 500;     // Length of a Morse Code "dash" in milliseconds
        int short_gap = 200;    // Length of Gap Between dots/dashes
        int medium_gap = 500;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words
        long[] pattern = {
                0,  // Start immediately
                dot    // s

        };
        v.vibrate(pattern, -1);
    }

    private void checkIfCloserAndDisplayRing() {
        youAreCloser = currentDist < lastDist;
        if (currentDist != lastDist) {
            displayRing();

        }
        lastDist = currentDist;
    }

    private void refreshFriendsLists() {
        if (actionsFragment != null) {
            actionsFragment.refresh();
        }
    }

    private Runnable hideRing = new Runnable() {
        public void run() {
            //This method runs in the same thread as the UI.
            ringView.animate().alpha(0).setDuration(1000).start();

        }
    };

    private PopupMenu getPopup(View v) {
        if (popup == null) {
            popup = new PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_homepage, popup.getMenu());
            popup.getMenu().findItem(R.id.action_vibrate).setChecked(prefs.getBoolean("vibrate", true));

        }
        return popup;
    }

    public void showPopup(View v) {
        PopupMenu popup = getPopup(v);
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_logout:
                        logOut();
                        return true;

                    case R.id.action_vibrate:

                        boolean b = prefs.getBoolean("vibrate", true);
                        b = !b;
                        item.setChecked(!item.isChecked());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("vibrate", b);
                        editor.commit();
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


        // map colors from range 0 - MAX
        int red = (int) ((getPercentageComplete()/100.0) * MAX_COLOR_RANGE);
        int blue = MAX_COLOR_RANGE - red;

        // shift into range 60 < color < 255
        blue = blue + MIN_COLOR_VALUE;
        red = red + MIN_COLOR_VALUE;


        mainColorDisplay.setBackgroundColor(Color.rgb(red,70,blue));
    }

    private void setBlankBackground() {
        winMessage.setVisibility(View.INVISIBLE);
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
        setupFriendUsername();
        friendUsernameTitle.setText(friendUsername);
        checkLocation(friendUsername);
        if (myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }

        createAndStartTimer();

    }

    private void setupFriendUsername() {
        friendUsernameTitle = (TextView) findViewById(R.id.users_title);
    }

    private void setCurrentCoordinates(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
    }

    // Returns an int from 0 - 100 that represents the current percentage of the distance covered
    // based on the initial distance set
    public int getPercentageComplete() {
        initialDist = Utils.distFrom(initialLatitude, initialLongitude, targetLatitude, targetLongitude);
        currentDist = Utils.distFrom(currentLatitude, currentLongitude, targetLatitude, targetLongitude);
        int percentageRemaining = (int) ((currentDist/initialDist) * 100);
        if (percentageRemaining > 100) {
            percentageRemaining = 100;
        }
        int percentageComplete = 100 - percentageRemaining;
        return percentageComplete;
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
                            reset();
                        } else {

                            isInGame = true;

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

    public void reset() {
        isInGame = false;
        setBlankBackground();
        if (myTimer != null) {
            myTimer.cancel();
        }
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
