package com.zivkaputa.camerademo.Activities.SwipeFragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.Activities.AddFriendActivity;
import com.zivkaputa.camerademo.Activities.LoginActivity;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.FriendAdapter;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.RequestAdapter;
import com.zivkaputa.camerademo.Activities.RequestListActivity;
import com.zivkaputa.camerademo.Activities.SignUpActivity;
import com.zivkaputa.camerademo.Activities.SwipeActivity;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * FriendListFragment
 *
 * Fragment for displaying and interacting with the user's friends
 */
public class FriendListFragment extends Fragment {


    @BindView( R.id.friends_recycler_view ) RecyclerView mRecyclerView;

    private ArrayList<FriendRequest> allFriends = new ArrayList<>();
    private FriendAdapter friendAdapter;
    boolean initialized = false;

    /**
     * Called when the fragment is created
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);

    }


    /**
     * Called when the view is created
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // Handle XML linking
        View view = inflater.inflate(R.layout.friend_list, container, false);
        ButterKnife.bind( this, view );

        // Setup the recycler view
        mRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity(), LinearLayoutManager.VERTICAL, false ) );

        SimpleSnapApp curApp = (SimpleSnapApp) getActivity().getApplication();

        friendAdapter = new FriendAdapter( allFriends, curApp );
        mRecyclerView.setAdapter( friendAdapter );

        curApp.friendListFragment = this;

        // Set listener to update recyclerView
        if ( curApp.getUser() != null ){
            setupFriendListener();
        }

        return view;

    }


    /**
     * Sets up the listener used to pull data from the database and update the recycler view
     */
    public void setupFriendListener(){

        initialized = true;

        SimpleSnapApp curApp = (SimpleSnapApp) getActivity().getApplication();
        DatabaseReference mDatabase = curApp.getDatabase();
        User curUser = curApp.getUser();

       DatabaseReference friendListRef = mDatabase.child(getString(R.string.users_db_tag))
                .child( curUser.ID ).child(getString(R.string.friends_db_tag));

        // Create listener for any changes in the request list
        ValueEventListener friendListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allFriends.clear();
                for ( DataSnapshot requestSnapshot : dataSnapshot.getChildren() ){

                    FriendRequest curRequest = requestSnapshot.getValue( FriendRequest.class );

                    // If the friend has been accepted, add them to the friend list
                    if ( curRequest.accepted ) {
                        allFriends.add(curRequest);
                    }

                }

                // Update recycler view
                friendAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.friend_list_activity_tag), getString(R.string.failed_to_get_friends) );

            }
        };

        friendListRef.addValueEventListener( friendListener );


    }


    /**
     * Launches the add friend activity
     */
    @OnClick ( R.id.add_friend )
    public void addFriend(){

        Intent intent = new Intent( getActivity(), AddFriendActivity.class );
        startActivity( intent );

    }


    /**
     * Launches the requests activity
     */
    @OnClick ( R.id.view_requests )
    public void viewRequests(){

        Intent intent = new Intent( getActivity(), RequestListActivity.class );
        startActivity( intent );

    }


    /**
     * Signs the user out and goes to the main screen
     */
    @OnClick ( R.id.sign_out )
    public void signOut(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent( getContext() , LoginActivity.class );
        startActivity( intent );

    }







}
