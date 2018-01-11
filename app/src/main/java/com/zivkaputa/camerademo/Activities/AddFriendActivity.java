package com.zivkaputa.camerademo.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.ErrorHandling.ToastUtils;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * AddFriendActivity
 *
 * Contains methods for the UI used to add new friends
 */
public class AddFriendActivity extends AppCompatActivity{


    @BindView( R.id.friend_username ) EditText friendUsernameTextView;
    User curUser;

    /**
     * Called when th activity is created
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);
        ButterKnife.bind( this );

    }


    /**
     * Starts the process of requesting a friend
     */
    @OnClick( R.id.add_new_friend )
    public void startFriendRequest(){

        // Get current App information
        SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
        curUser = curApp.getUser();

        String friendUsername = friendUsernameTextView.getText().toString();

        // Basic input validation
        if ( friendUsername.isEmpty() ){
            ToastUtils.shortToast( getString(R.string.enter_username), this );
            return;
        }

        if ( curUser == null ){
            ToastUtils.shortToast( getString(R.string.generic_error), this );
            return;
        }

        // Check that the username is valid and continue
        checkUsernameExists( friendUsername );


    }

    /**
     * Second part of the friend addition process. Checks that the username exists,
     * and if so, continues adding friend
     *
     * @param friendUsername - The new friend's username
     */
    private void checkUsernameExists( String friendUsername ){

        final DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().
                child(getString(R.string.usernames_db_tag)).child( friendUsername );

        // Check if username exists by querying the database
        ValueEventListener usernameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If friend's username exists, request friend
                String friendID = (String) dataSnapshot.getValue();
                if ( friendID != null ){

                    checkAlreadyFriends( friendID );


                    // If friend's username does not exist, notify user
                } else {

                    ToastUtils.shortToast( getString(R.string.no_user_error), AddFriendActivity.this );

                }

                usernameRef.removeEventListener( this );


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.signup_activity_tag), getString(R.string.did_not_check_for_user) );

            }
        };

        // Add the listener and wait
        usernameRef.addValueEventListener( usernameListener );

    }

    /**
     * Check whether the friend is already on the current user's list
     *
     * If they are: Notify user with appropriate response
     *
     * If they are not: Continue adding them as a friend
     *
     * @param friendId
     */
    private void checkAlreadyFriends(final String friendId ){


        final DatabaseReference friendOnFriendListRef = FirebaseDatabase.getInstance().getReference().
                child(getString(R.string.users_db_tag)).child( curUser.ID ).
                child( getString(R.string.friends_db_tag )).child( friendId );

        // Check if username exists on current friends list
        ValueEventListener friendListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If the current user has already requested this friend, notify them
                FriendRequest possibleFriend = dataSnapshot.getValue( FriendRequest.class );
                if ( possibleFriend != null ){

                    if ( possibleFriend.accepted ){
                        ToastUtils.shortToast( getString(R.string.already_friends), AddFriendActivity.this );
                    } else {
                        ToastUtils.shortToast( getString(R.string.already_requested), AddFriendActivity.this );
                    }

                // If current user does not have this friend, request them
                } else {

                    curUser.addFriend( friendId );
                    ToastUtils.shortToast( getString(R.string.friend_added_message), AddFriendActivity.this );

                }

                friendOnFriendListRef.removeEventListener( this );


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.signup_activity_tag), getString(R.string.did_not_check_for_user) );

            }
        };

        // Add the listener and wait
        friendOnFriendListRef.addValueEventListener( friendListener );


    }






}
