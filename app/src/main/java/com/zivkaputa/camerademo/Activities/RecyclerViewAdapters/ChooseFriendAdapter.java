package com.zivkaputa.camerademo.Activities.RecyclerViewAdapters;

/**
 * Created by Ziv on 5/2/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * ChooseFriendAdapter
 *
 * Contains methods for updating recycler view content for the list of friend recipients
 */
public class ChooseFriendAdapter extends RecyclerView.Adapter<ChooseFriendAdapter.ViewHolder> {

    ArrayList<FriendRequest> friends;
    HashSet<String> selectedFriendIDs = new HashSet<>();
    SimpleSnapApp curApp;


    /**
     * Constructor for ChooseFriendAdaptor
     *
     * @param friends - The friends to be used in the adapter
     */
    public ChooseFriendAdapter( ArrayList<FriendRequest> friends, SimpleSnapApp curApp ) {

        this.friends = friends;
        this.curApp = curApp;

    }

    /**
     * Gets the selected friends
     * @return A set of the selected friends
     */
    public HashSet<String> getSelectedFriendIDs() {
        return selectedFriendIDs;
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
                inflate( R.layout.choose_friend_item , parent, false );

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
    public void onBindViewHolder(final ViewHolder holder, int position ) {

        final FriendRequest friend = friends.get( position );

        holder.usernameView.setText( friend.senderUsername );
        holder.fullNameView.setText( friend.senderFullName );

        // When the friend is chosen, add them to the overall selected list
        holder.userCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( holder.userCheckBox.isEnabled() ){

                    selectedFriendIDs.add( friend.senderID );

                } else {

                    selectedFriendIDs.remove( friend.senderID );

                }

            }
        });


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
        public CheckBox userCheckBox;


        public ViewHolder( View itemView ) {

            // Link the members with their widgets in the XML
            super(itemView);
            view = itemView;
            fullNameView = (TextView) itemView.findViewById( R.id.choose_friend_full_name );
            usernameView = (TextView) itemView.findViewById( R.id.choose_friend_username );
            userCheckBox = (CheckBox) itemView.findViewById( R.id.choose_friend_select );


        }
    }
}



