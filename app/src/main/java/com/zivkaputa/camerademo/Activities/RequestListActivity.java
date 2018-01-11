package com.zivkaputa.camerademo.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.RequestAdapter;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RequestListActivity
 *
 * Activity for accepting/rejecting incoming requests
 */
public class RequestListActivity extends AppCompatActivity{

    @BindView( R.id.request_recycler_view ) RecyclerView mRecyclerView;

    private ArrayList<FriendRequest> allRequests = new ArrayList<>();
    private RequestAdapter requestAdapter;


    /**
     * Called when the activity begins
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_requests);
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager( new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false ) );

        requestAdapter = new RequestAdapter( allRequests, (SimpleSnapApp) getApplication() );
        mRecyclerView.setAdapter( requestAdapter );

        setupRequestListener();

    }


    /**
     * Sets up the listener for downloading data and updating the request list
     */
    public void setupRequestListener(){

        SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
        DatabaseReference mDatabase = curApp.getDatabase();
        User curUser = curApp.getUser();

        DatabaseReference requestListRef = mDatabase.child("users").child( curUser.ID ).child("incoming_requests");

        // Create listener for any changes in the request list
        ValueEventListener requestListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get all requests from the list
                allRequests.clear();
                for ( DataSnapshot requestSnapshot : dataSnapshot.getChildren() ){

                    FriendRequest curRequest = requestSnapshot.getValue( FriendRequest.class );
                    allRequests.add( curRequest );

                }

                // Tell adapter the data has changed
                requestAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( "RequestListActivity", "Failed to get friend requests update" );

            }
        };

        requestListRef.addValueEventListener( requestListener );


    }

}
