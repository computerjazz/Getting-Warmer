package com.danielmerrill.gettingwarmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Homepage extends AppCompatActivity {
    // GPSTracker class
    GPSTracker gps;
    EditText friendUserName_input;
    private ListView lv;
    private String username;
    private ArrayList<String> friendsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = prefs.getString("username", "");
        TextView userView = (TextView) findViewById(R.id.username_view);
        userView.setText(username);
        refreshFriendsList();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    public void addFriend(View v) {
        friendUserName_input = (EditText)findViewById(R.id.input_friendUsername);
        final String friendUserName = friendUserName_input.getText().toString();
        //setContentView(R.layout.progressbar_layout);
        //making object of RestAdapter
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);


        //Calling method to signup
        if (isValid(friendUserName, friendUserName_input)) {
            restInterface.addFriend(username, friendUserName, new Callback<LoginModel>() {


                @Override
                public void success(LoginModel model, Response response) {


                    if (model.getStatus().equals("1")) {  //AddFriend Success
                        refreshFriendsList();
                        Toast.makeText(Homepage.this, "Added " + friendUserName + " to friends", Toast.LENGTH_SHORT).show();
                        friendUserName_input.setText("");

                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);


                    } else if (model.getStatus().equals("0"))  // Friend add failure
                    {
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(Homepage.this, friendUserName + " does not exist", Toast.LENGTH_SHORT).show();
                    }else if (model.getStatus().equals("3")) { // already friends
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(Homepage.this, "Already friends with " + friendUserName, Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(Homepage.this, merror, Toast.LENGTH_LONG).show();
                }
            });
        }

    }




    public void dropPin(View v) {
        // get location info
        // create class object
        gps = new GPSTracker(Homepage.this);

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
        Intent i = new Intent(Homepage.this, LoginActivity.class);
        startActivity(i);
    }

    //checking field are empty
    private boolean isValid(String s, EditText e){

        boolean valid=true;
        if(s.equals("")) {
            e.setError("Can't be Empty");
            valid = false;
        }
        return valid;

    }

    private void setFriendsList(ArrayList<String> fl) {

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        java.util.Collections.sort(fl);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                fl);

        lv.setAdapter(arrayAdapter);
    }

    private void refreshFriendsList() {
        lv = (ListView) findViewById(R.id.friends_list);
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

                        Toast.makeText(Homepage.this, "Couldn't get friends", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(Homepage.this, merror, Toast.LENGTH_LONG).show();
                }
            });




    }
}
