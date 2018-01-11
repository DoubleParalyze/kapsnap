package com.zivkaputa.camerademo.Activities.RecyclerViewAdapters;

/**
 * Created by Ziv on 5/2/17.
 */


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.User;


/**
 * RequestAdapter
 *
 * Contains methods for updating recycler view content
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    ArrayList<FriendRequest> requests;
    SimpleSnapApp curApp;

    /**
     * Constructor for RequestAdaptor
     *
     * @param requests - The requests to be used in the adapter
     */
    public RequestAdapter( ArrayList<FriendRequest> requests, SimpleSnapApp curApp ) {

        this.requests = requests;
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
        final View requestItem = LayoutInflater.from( parent.getContext() ).
                inflate( R.layout.request_item , parent, false );

        return new ViewHolder( requestItem );

    }

    /**
     * This function gets called each time a ViewHolder needs to hold data for a different
     * position in the list.  We don't need to create any views (because we're recycling), but
     * we do need to update the contents in the views.
     *
     * @param holder the ViewHolder that knows about the Views we need to update
     * @param position the index into the array of requests
     */
    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {

        final FriendRequest request = requests.get( position );

        holder.usernameView.setText( request.senderUsername );
        holder.fullNameView.setText( request.senderFullName );

        // Accept friend request
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                User curUser = curApp.getUser();
                curUser.acceptFriend( request.senderID );

            }
        });


        // Accept friend request
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                User curUser = curApp.getUser();
                curUser.rejectFriend( request.senderID );

            }
        });

    }

    /**
     * Gets the number of items in the collection. Used by the Recycler view to determine when
     * end of collection is reached
     *
     * @return the number of requests in the array.
     */
    @Override
    public int getItemCount() {
        return requests.size();
    }


    /**
     * A ViewHolder class for our adapter that 'caches' the references to the
     * subviews, so we don't have to look them up each time.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView fullNameView;
        public TextView usernameView;
        public ImageButton acceptButton;
        public ImageButton rejectButton;


        public ViewHolder( View itemView ) {

            // Link the members with their widgets in the XML
            super(itemView);
            view = itemView;
            fullNameView = (TextView) itemView.findViewById( R.id.request_full_name );
            usernameView = (TextView) itemView.findViewById( R.id.request_username );
            acceptButton = (ImageButton) itemView.findViewById( R.id.accept_request );
            rejectButton = (ImageButton) itemView.findViewById( R.id.reject_request );


        }
    }
}

