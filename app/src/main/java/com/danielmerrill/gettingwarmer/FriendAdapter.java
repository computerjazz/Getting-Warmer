package com.danielmerrill.gettingwarmer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by danielmerrill on 8/20/16.
 */
public class FriendAdapter extends ArrayAdapter<Friend> {
    Context context;
    int layoutResourceId;
    Friend data[] = null;

    public FriendAdapter(Context context, int layoutResourceId, Friend[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FriendHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FriendHolder();
            holder.new_icon = (ImageView)row.findViewById(R.id.list_item_new_icon);
            holder.friend_name = (TextView)row.findViewById(R.id.list_item_friend_name);

            row.setTag(holder);
        }
        else {
            holder = (FriendHolder)row.getTag();
        }

        Friend friend = data[position];
        holder.friend_name.setText(friend.name);
        if (friend.hasNewLocation) {
            holder.new_icon.setVisibility(View.VISIBLE);
        } else {
            holder.new_icon.setVisibility(View.INVISIBLE);
        }
        holder.new_icon.setImageResource(R.drawable.ic_fiber_new_grey_24dp);


        return row;
    }

    static class FriendHolder
    {
        ImageView new_icon;
        TextView friend_name;
    }
}
