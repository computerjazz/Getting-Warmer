package com.danielmerrill.gettingwarmer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ActionsFragment extends android.support.v4.app.Fragment {
    // GPSTracker class
    GPSTracker gps;
    EditText friendUserName_input;
    private ImageView closeDrawer;
    private AppCompatButton addFriendIcon;
    private ListView flv;
    private ListView rlv;
    private NoScrollListView requestsListView;
    private LinearLayout layout_requests;
    private TextView distFromTarget;
    private TextView friendListTitle;
    private NoScrollListView friendsListView;
    private String username;
    private boolean hasLocation;
    private boolean isNewLocation;

    private ArrayList<String> friendsList;
    private ArrayList<String> requestsList;

    private  int ANIMATION_DURATION = 200;
    private DrawerLayout mDrawerLayout;
    private ColorActivity colorActivity;
    private double targetLatitude;
    private double targetLongitude;
    private UsernameValidator usernameValidator;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_actions, container, false);

        hasLocation = false;
        isNewLocation = false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        username = prefs.getString("username", "");
        TextView userView = (TextView) v.findViewById(R.id.username_view);
        userView.setText(username);
        friendUserName_input = (EditText)v.findViewById(R.id.input_friendUsername);

        flv = (ListView) v.findViewById(R.id.friends_list);
        rlv = (ListView) v.findViewById(R.id.requests_list);
        friendListTitle = (TextView) v.findViewById(R.id.textview_friends);
        friendsListView = (NoScrollListView) v.findViewById(R.id.friends_list);
        closeDrawer = (ImageView) v.findViewById(R.id.closeDrawer);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        addFriendIcon = (AppCompatButton) v.findViewById(R.id.btn_addfriendIcon);
        layout_requests = (LinearLayout) v.findViewById(R.id.layout_requests);
        usernameValidator = new UsernameValidator();

        friendsList = new ArrayList<>();
        requestsList = new ArrayList<>();
        setRequestsList(new ArrayList<String>());
        setFriendsList(new ArrayList<String>());

        colorActivity = (ColorActivity) getActivity();

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

                final String selectedFromList = (rlv.getItemAtPosition(position).toString());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("FRIEND REQUEST");
                builder.setMessage(selectedFromList + " wants to be your friend");
                builder.setCancelable(true);
                builder.setIcon(R.drawable.ic_account_circle_grey_24dp);
                builder.setTitle(selectedFromList.toUpperCase());

                builder.setPositiveButton(
                        "Accept",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                acceptFriend(selectedFromList);
                                dialog.cancel();
                            }
                        });

                builder.setNegativeButton(
                        "Deny",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteRelationship(selectedFromList);
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        flv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String friend = flv.getItemAtPosition(position).toString();



                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(friend.toUpperCase());
                builder.setCancelable(true);
                builder.setIcon(R.drawable.ic_person_pin_circle_grey_24dp);
                builder.setMessage("Sending your location will overwrite any past locations sent to " + friend);

                builder.setPositiveButton(
                        "Find " + friend,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                colorActivity.reset();
                                colorActivity.setFriendUsername(friend);
                                dialog.cancel();
                                mDrawerLayout.closeDrawers();
                            }
                        });

                builder.setNegativeButton(
                        "Send Your Location",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                sendLocation(friend);
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });



        flv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // get location info
                // create class object
                final String friend = flv.getItemAtPosition(pos).toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("UNFRIEND");
                builder.setMessage("Remove " + friend + " from your friends?");
                builder.setCancelable(true);
                builder.setIcon(R.drawable.ic_delete_grey_24dp);

                builder.setPositiveButton(
                        "Remove",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteRelationship(friend);
                                dialog.cancel();
                            }
                        });

                builder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });


        closeDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
            }
        });

        addFriendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFriend(v);
            }
        });

        return v;

    }


    // Set location of friend with username 'f'
    private void sendLocation(String friendUsername) {
        final String friend = friendUsername;
        gps = new GPSTracker(getActivity());

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            //Creating Rest Services
            RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
            final RestInterface restInterface = adapter.create(RestInterface.class);

            //Calling method -- sending 0.0 is ugly but restInterface won't take null
            restInterface.setLocation(friend, username, latitude, longitude, 0.0, 0.0, new Callback<LoginModel>() {

                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //setlocation Success
                        Toast.makeText(getActivity().getApplicationContext(), "Sent your current location to " + friend, Toast.LENGTH_SHORT).show();


                    } else if (model.getStatus().equals("0")) {

                    } else if (model.getStatus().equals("2")) {

                    }
                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
                }
            });
            //Toast.makeText(getActivity().getApplicationContext(), "Setting location - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    public void refresh() {
        refreshFriendsList();
        refreshRequestsList();
    }

    //TODO
    private boolean isLocationSet(String friendName) {

        boolean locationSet = false;
        //Creating Rest Services

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method
        restInterface.getLocation(username, friendName, new Callback<LoginModel>() {

            @Override
            public void success(LoginModel model, Response response) {

                if (model.getStatus().equals("1")) {  //getlocation Success

                    if (model.getLatitudeTarget() == null && model.getLongitudeTarget() == null) {
                        hasLocation = false;
                    } else {
                        hasLocation = true;
                        if (model.getIsNew().equals("1")) { // check if location is new
                            isNewLocation = true;

                        } else {
                            isNewLocation = false;

                        }
                    }

                } else if (model.getStatus().equals("0")) {
                } else if (model.getStatus().equals("2")) {
                }
            }

            @Override
            public void failure(RetrofitError error) {
                String merror = error.getMessage();
                Toast.makeText(getActivity(), merror, Toast.LENGTH_SHORT).show();
            }
        });
        return hasLocation;
    }





    private void deleteRelationship(String friendName) {
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();
        final String friend = friendName;

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method to signup
        restInterface.deleteRelationship(username, friend, new Callback<LoginModel>() {

            @Override
            public void success(LoginModel model, Response response) {

                if (model.getStatus().equals("1")) {  //Delete relationship Success
                    refresh();
                    Toast.makeText(getActivity().getApplicationContext(), "Removed " + friend, Toast.LENGTH_SHORT).show();


                } else if (model.getStatus().equals("0")) { // Friend add failure


                    Toast.makeText(getActivity().getApplicationContext(), friend + " does not exist", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void failure(RetrofitError error) {
                String merror = error.getMessage();
                Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
            }
        });
    }

    //TODO
    private void checkForNewPins() {

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
                        Toast.makeText(getActivity().getApplicationContext(), "Accepted friend request from " + friend, Toast.LENGTH_SHORT).show();
                        

                    } else if (model.getStatus().equals("0")) { // Friend add failure


                        Toast.makeText(getActivity().getApplicationContext(), friend + " does not exist", Toast.LENGTH_SHORT).show();
                    } else if (model.getStatus().equals("3")) { // previous request from friend was already sent, so add friends
                        refreshFriendsList();
                        refreshRequestsList();
                        // hide keyboard

                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (getActivity().getCurrentFocus() != null) {
                            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                        }

                        Toast.makeText(getActivity(), "Now friends with " + friend, Toast.LENGTH_SHORT).show();

                    } else if (model.getStatus().equals("4")) { // already sent request
                        Toast.makeText(getActivity().getApplicationContext(), "Request to " + friend + " already sent", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    String merror = error.getMessage();
                    Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
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
            Animation shake = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.shake);
            friendUserName_input.startAnimation(shake);
            Toast.makeText(getActivity().getApplicationContext(), "That's you!", Toast.LENGTH_SHORT).show();
        } else if (isValid(friendUserName, friendUserName_input)) {
            restInterface.requestFriend(username, friendUserName, new Callback<LoginModel>() {


                @Override
                public void success(LoginModel model, Response response) {


                    if (model.getStatus().equals("1")) {  //AddFriend Success

                        refreshFriendsList();
                        Toast.makeText(getActivity().getApplicationContext(), "Sent a friend request to " + friendUserName, Toast.LENGTH_SHORT).show();
                        friendUserName_input.setText("");

                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);


                    } else if (model.getStatus().equals("0")) {  // Friend add failure

                        Animation shake = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(getActivity().getApplicationContext(), friendUserName + " does not exist", Toast.LENGTH_SHORT).show();

                    } else if (model.getStatus().equals("3")) { // previous request from friend was already sent, so add to friends

                        refreshFriendsList();
                        refreshRequestsList();

                        friendUserName_input.setText("");

                        // hide keyboard
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                        Toast.makeText(getActivity().getApplicationContext(), "Now friends with " + friendUserName, Toast.LENGTH_SHORT).show();

                    } else if (model.getStatus().equals("4")) { // already sent request
                        // hide keyboard
                        friendUserName_input.setText("");
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                        Animation shake = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.shake);
                        friendUserName_input.startAnimation(shake);
                        Toast.makeText(getActivity().getApplicationContext(), "Request to " + friendUserName + " already sent", Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    //checking field are empty
    private boolean isValid(String s, EditText e){
        if(s.equals("")) {
            e.setError("There's nothing here!");
        }
        return usernameValidator.validate(s);
    }

    private void setFriendsList(ArrayList<String> fl) {
        if (getActivity() != null) {

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.custom_listitem,
                    fl);

            flv.setAdapter(arrayAdapter);
        }

    }

    private void setRequestsList(ArrayList<String> rl) {
        if (getActivity() != null) {

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.custom_listitem,
                    rl);

            rlv.setAdapter(arrayAdapter);
            showOrHideRequestsList(rl);
        }


    }

    private void showOrHideRequestsList(List<String> requestList) {
        if (requestList.size() == 0) {
            layout_requests.setVisibility(View.GONE);
        } else {
            layout_requests.setVisibility(View.VISIBLE);
        }
    }


    public void refreshFriendsList() {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method to get friends list
            restInterface.getFriends(username, new Callback<LoginModel>() {

                @Override
                public void success(LoginModel model, Response response) {

                    if (model.getStatus().equals("1")) {  //get friends Success
                        ArrayList<String> friends = (ArrayList<String>) model.getFriends();
                        Collections.sort(friends, String.CASE_INSENSITIVE_ORDER);
                        if (!friends.equals(friendsList)) {
                            setFriendsList(friends);
                            friendsList = friends;
                        }


                    } else if (model.getStatus().equals("2")) { // database error

                        Toast.makeText(getActivity().getApplicationContext(), "Couldn't get friends", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                    String merror = error.getMessage();
                    Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
                }
            });
    }

    public void refreshRequestsList() {

        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(RestInterface.url).build();

        //Creating Rest Services
        final RestInterface restInterface = adapter.create(RestInterface.class);

        //Calling method to get friends list
        restInterface.getRequests(username, new Callback<LoginModel>() {

            @Override
            public void success(LoginModel model, Response response) {

                if (model.getStatus().equals("1")) {  //get friend requests Success
                    ArrayList<String> requests = (ArrayList<String>) model.getRequests();
                    Collections.sort(requests, String.CASE_INSENSITIVE_ORDER);
                    //Toast.makeText(getActivity().getApplicationContext(), "Got: " + requests.size() + " Had: " + requestsList.size(), Toast.LENGTH_SHORT).show();
                    if (!requests.equals(requestsList)) {
                        requestsList = requests;
                        setRequestsList(requests);
                    }


                } else if (model.getStatus().equals("2")) { // database error

                    Toast.makeText(getActivity().getApplicationContext(), "Couldn't get friend requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                String merror = error.getMessage();
                Toast.makeText(getActivity().getApplicationContext(), merror, Toast.LENGTH_LONG).show();
            }
        });
    }
}
