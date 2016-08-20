package com.danielmerrill.gettingwarmer;

import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by danielmerrill on 8/20/16.
 */
public class Friend {
    public boolean hasNewLocation;
    public String name;

    public Friend() {
        super();
    }

    public Friend(String name, ArrayList<String> friendsWithNewLocation){
        super();
        this.name = name;
        if (friendsWithNewLocation.contains(name)) {
            hasNewLocation = true;
        } else {
            hasNewLocation = false;
        }
    }

}
