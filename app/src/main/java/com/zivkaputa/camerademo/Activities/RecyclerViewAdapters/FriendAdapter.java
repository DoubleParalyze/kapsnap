package com.zivkaputa.camerademo.Activities.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;

import java.util.ArrayList;



/**
 * FriendAdapter
 *
 * Contains methods for updating recycler view content
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    ArrayList<FriendRequest> friends;
    SimpleSnapApp curApp;

    /**
     * Constructor for FriendAdapter
     *
     * @param friends - The friends to be used in the adapter
     */
    public FriendAdapter( ArrayList<FriendRequest> friends, SimpleSnapApp curApp ) {

        this.friends = friends;
        this.curApp = curApp;

    }


    /**
     * Fills the on-screen elements with information and recycles views when scrolling is complete
     *
     * @param parent the parent object ( RecyclerView )
     * @param viewType the type of view to fill
     * @return the new ViewHolder allocated
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {

        // use XML definition to create an object
        final View friendItem = LayoutInflater.from( parent.getContext() ).
                inflate( R.layout.friend_item , parent, false );

        return new ViewHolder( friendItem );

    }

    /**
     * This function gets called each time a ViewHolder needs to hold data for a different
     * position in the list.  We don't need to create any views (because we're recycling), but
     * we do need to update the contents in the views.
     *
     * @param holder the ViewHolder that knows about the Views we need to update
     * @param position the index into the array of friends
     */
    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {

        final FriendRequest friend = friends.get( position );

        holder.usernameView.setText( friend.senderUsername );
        holder.fullNameView.setText( friend.senderFullName );


    }

    /**
     * Gets the number of items in the collection. Used by the Recycler view to determine when
     * end of collection is reached
     *
     * @return the number of friends in the array.
     */
    @Override
    public int getItemCount() {
        return friends.size();
    }


    /**
     * A ViewHolder class for our adapter that 'caches' the references to the
     * subviews, so we don't have to look them up each time.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView fullNameView;
        public TextView usernameView;


        public ViewHolder( View itemView ) {

            // Link the members with their widgets in the XML
            super(itemView);
            view = itemView;
            fullNameView = (TextView) itemView.findViewById( R.id.friend_item_full_name );
            usernameView = (TextView) itemView.findViewById( R.id.friend_item_username );


        }
    }
}


