package com.zivkaputa.camerademo.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.ChooseFriendAdapter;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.RequestAdapter;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.MediaMessage;
import com.zivkaputa.camerademo.UserModel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ChooseFriendActivity
 *
 * Activity that allows user to choose a recipient for the current message
 */
public class ChooseFriendActivity extends AppCompatActivity{

    @BindView( R.id.choose_friend_recycler_view ) RecyclerView mRecyclerView;
    @BindView( R.id.choose_friend_cancel ) Button cancelButton;
    @BindView( R.id.choose_friend_send ) Button sendButton;

    private ArrayList<FriendRequest> allFriends = new ArrayList<>();
    private ChooseFriendAdapter friendsAdapter;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_friend);
        ButterKnife.bind(this);

        // Link recycler view
        mRecyclerView.setLayoutManager( new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false ) );

        friendsAdapter = new ChooseFriendAdapter( allFriends, (SimpleSnapApp) getApplication() );
        mRecyclerView.setAdapter( friendsAdapter );

        setupRequestListener();

    }


    /**
     * Sets up the listener used to pull data from the database and update the potential friends
     */
    public void setupRequestListener(){

        SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
        DatabaseReference mDatabase = curApp.getDatabase();
        User curUser = curApp.getUser();

        DatabaseReference friendsListRef = mDatabase.child("users").child( curUser.ID ).child("friends");

        // Create listener for any changes in the request list
        ValueEventListener friendsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Clear all friends
                allFriends.clear();
                for ( DataSnapshot requestSnapshot : dataSnapshot.getChildren() ){

                    FriendRequest curRequest = requestSnapshot.getValue( FriendRequest.class );

                    // If the friend has been accepted, add them to the potential friend list
                    if ( curRequest.accepted ) {
                        allFriends.add(curRequest);
                    }

                }

                friendsAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.request_list_activity_tag), getString(R.string.failed_to_get_request_update) );

            }
        };

        friendsListRef.addValueEventListener( friendsListener );


    }


    @OnClick(R.id.choose_friend_cancel)
    public void cancelAdditions(){

        super.onBackPressed();

    }


    @OnClick(R.id.choose_friend_send)
    public void sendMessages(){

        SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
        User curUser = curApp.getUser();
        Object media = curApp.getPassableMedia();

        MediaMessage.MediaType type = (MediaMessage.MediaType) getIntent().getSerializableExtra("mediaType");

        // Send all messages
        curUser.sendMessages( friendsAdapter.getSelectedFriendIDs(), type, media );

        super.onBackPressed();

    }

}


