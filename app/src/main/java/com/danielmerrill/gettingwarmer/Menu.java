package com.danielmerrill.gettingwarmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Menu extends AppCompatActivity {
    // GPSTracker class
    GPSTracker gps;
    EditText friendUserName_input;
    private ListView flv;
    private ListView rlv;
    private TextView dtv;
    private String username;
    private ArrayList<String> friendsList;
    private  int ANIMATION_DURATION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_homepage);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = prefs.getString("username", "");
        TextView userView = (TextView) findViewById(R.id.username_view);
        userView.setText(username);
        friendUserName_input = (EditText)findViewById(R.id.input_friendUsername);

        rlv = (ListView) findViewById(R.id.requests_list);
        flv = (ListView) findViewById(R.id.friends_list);
        dtv = (TextView) findViewById(R.id.distfromtarget);

        refreshFriendsList();
        refreshRequestsList();

        // Set up a listener to run request friend from softkeyboard
        friendUserName_input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    requestFriend(v);
                    return true;
                }
                return false;
            }
        });


        // set up friend request list click handler to accept requests
        rlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id)
            {

                String selectedFromList = (rlv.getItemAtPosition(position).toString());
                //deleteCell(view, position); // comment to disable animations
                acceptFriend(selectedFromList);

            }
        });

        flv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get location info
                // create class object
                final String friend = flv.getItemAtPosition(position).toString();
                gps = new GPSTracker(Menu.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    final double latitude = gps.getLatitude();
                    final double longitude = gps.getLongitude();

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
                                double targetLat = Double.parseDouble(model.getLatitudeTarget());
                                double targetLong = Double.parseDouble(model.getLongitudeTarget());

                                double diff = distFrom(targetLat, targetLong, latitude, longitude);
                                dtv.setText("Distance from " + friend + ": " + (int)diff + " meters");
                                Toast.makeText(getApplicationContext(), "Your lat: " + latitude + "\nTarget lat: " + targetLat + "\nYour lng:" + longitude + "\nTarget lng: " + targetLong +  "\nOff by " + diff + " meters", Toast.LENGTH_LONG).show();


                            } else if (model.getStatus().equals("0")) {

                            } else if (model.getStatus().equals("2")) {

                            }




                        }

                        @Override
                        public void failure(RetrofitError error) {

                            String merror = error.getMessage();
                            Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });



        flv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // get location info
                // create class object
                final String friend = flv.getItemAtPosition(pos).toString();
                gps = new GPSTracker(Menu.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    //Creating Rest Services
                    RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
                    final RestInterface restInterface = adapter.create(RestInterface.class);

                    //Calling method
                    restInterface.setLocation(friend, username, latitude, longitude, 0.0, 0.0, new Callback<LoginModel>() {

                        @Override
                        public void success(LoginModel model, Response response) {

                            if (model.getStatus().equals("1")) {  //setlocation Success
                                Toast.makeText(Menu.this, "Set location to " + friend, Toast.LENGTH_SHORT).show();


                            } else if (model.getStatus().equals("0")) {

                            } else if (model.getStatus().equals("2")) {

                            }




                        }

                        @Override
                        public void failure(RetrofitError error) {

                            String merror = error.getMessage();
                            Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Setting location - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

                return true;
            }
        });



    }




    public void refresh(View v) {
        refreshFriendsList();
        refreshRequestsList();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void acceptFriend(String friendName) {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
        final String friend = friendName;

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method to signup
            restInterface.requestFriend(username, friend, new Callback<LoginModel>() {

                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //AddFriend Success
                        refreshFriendsList();
                        Toast.makeText(Menu.this, "Accepted friend request from " + friend, Toast.LENGTH_SHORT).show();
                        

                    } else if (model.getStatus().equals("0"))  // Friend add failure
                    {

                        Toast.makeText(Menu.this, friend + " does not exist", Toast.LENGTH_SHORT).show();
                    }else if (model.getStatus().equals("3")) { // previous request from friend was already sent, so add friends
                        refreshFriendsList();
                        refreshRequestsList();
                        // hide keyboard

                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        Toast.makeText(Menu.this, "Now friends with " + friend, Toast.LENGTH_SHORT).show();

                    }else if (model.getStatus().equals("4")) { // already sent request
                        // hide keyboard

                        Toast.makeText(Menu.this, "Request to " + friend + " already sent", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
                }
            });
        }


    public void requestFriend(View v) {

        final String friendUserName = friendUserName_input.getText().toString();
        //setContentView(R.layout.progressbar_layout);
        //making object of RestAdapter
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);


        //Calling method to signup
        if (friendUserName.toLowerCase().equals(username.toLowerCase())) {
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            friendUserName_input.startAnimation(shake);
            Toast.makeText(Menu.this, "That's you!", Toast.LENGTH_SHORT).show();
        } else if (isValid(friendUserName, friendUserName_input)) {
            restInterface.requestFriend(username, friendUserName, new Callback<LoginModel>() {


                @Override
                public void success(LoginModel model, Response response) {


                    if (model.getStatus().equals("1")) {  //AddFriend Success
                        refreshFriendsList();
                        Toast.makeText(Menu.this, "Sent a friend request to " + friendUserName, Toast.LENGTH_SHORT).show();
                        friendUserName_input.setText("");

                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);


                    } else if (model.getStatus().equals("0"))  // Friend add failure
                    {
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(Menu.this, friendUserName + " does not exist", Toast.LENGTH_SHORT).show();
                    }else if (model.getStatus().equals("3")) { // previous request from friend was already sent, so add friends
                        refreshFriendsList();
                        refreshRequestsList();
                        // hide keyboard
                        friendUserName_input.setText("");
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        Toast.makeText(Menu.this, "Now friends with " + friendUserName, Toast.LENGTH_SHORT).show();

                    }else if (model.getStatus().equals("4")) { // already sent request
                        // hide keyboard
                        friendUserName_input.setText("");
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(Menu.this, "Request to " + friendUserName + " already sent", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
                }
            });
        }

    }




    public void dropPin(View v) {
        // get location info
        // create class object
        gps = new GPSTracker(Menu.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
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
        Intent i = new Intent(Menu.this, LoginActivity.class);
        startActivity(i);
    }

    //checking field are empty
    private boolean isValid(String s, EditText e){
        boolean valid=true;
        if(s.equals("")) {
            e.setError("There's nothing here!");
            valid = false;
        }
        return valid;
    }

    private void setFriendsList(ArrayList<String> fl) {

        Collections.sort(fl, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                fl);

        flv.setAdapter(arrayAdapter);
    }

    private void setRequestsList(ArrayList<String> rl) {

        Collections.sort(rl, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                rl);

        rlv.setAdapter(arrayAdapter);
    }

    private void refreshFriendsList() {

        setFriendsList(new ArrayList<String>());
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);


        //Calling method to get friends list
            restInterface.getFriends(username, new Callback<LoginModel>() {


                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //get friends Success
                        ArrayList<String> friends = (ArrayList<String>) model.getFriends();
                        //Toast.makeText(Homepage.this, "Got " + friends.size() + " friends: " + friends.get(0), Toast.LENGTH_SHORT).show();
                        setFriendsList(friends);

                    } else if (model.getStatus().equals("2")) { // database error

                        Toast.makeText(Menu.this, "Couldn't get friends", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void refreshRequestsList() {

        setRequestsList(new ArrayList<String>());

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);


        //Calling method to get friends list
        restInterface.getRequests(username, new Callback<LoginModel>() {


            @Override
            public void success(LoginModel model, Response response) {

                if (model.getStatus().equals("1")) {  //get friend requests Success
                    ArrayList<String> requests = (ArrayList<String>) model.getRequests();
                    //Toast.makeText(Homepage.this, "Got " + friends.size() + " friends: " + friends.get(0), Toast.LENGTH_SHORT).show();
                    setRequestsList(requests);

                } else if (model.getStatus().equals("2")) { // database error

                    Toast.makeText(Menu.this, "Couldn't get friend requests", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void failure(RetrofitError error) {

                String merror = error.getMessage();
                Toast.makeText(Menu.this, merror, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist =  earthRadius * c;

        return dist;
    }

    /* Requests list animation

    private void deleteCell(final View v, final int index) {
        AnimationListener al = new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                //rlv.remove(index);

                //ViewHolder vh = (ViewHolder)v.getTag();
                //vh.needInflate = true;

                //mMyAnimListAdapter.notifyDataSetChanged();
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        };

        collapse(v, al);
    }

    private void collapse(final View v, AnimationListener al) {
        final int initialHeight = v.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                }
                else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        if (al!=null) {
            anim.setAnimationListener(al);
        }
        anim.setDuration(ANIMATION_DURATION);
        v.startAnimation(anim);
    }
    */
}
