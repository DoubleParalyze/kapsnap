package com.zivkaputa.camerademo;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zivkaputa.camerademo.Activities.SwipeFragments.FriendListFragment;
import com.zivkaputa.camerademo.Activities.SwipeFragments.MessageListFragment;
import com.zivkaputa.camerademo.UserModel.User;

/**
 * SimpleSnapApp
 *
 * Custom Application - Used to hold references to frequently accessed data, such as
 * the currently logged-in user and common Database locations
 */
public class SimpleSnapApp extends Application{

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private DatabaseReference curUserReference;
    private FirebaseAuth mAuth;
    private User curUser;
    private Object passableMedia;

    public FriendListFragment friendListFragment;
    public MessageListFragment messageListFragment;

    /**
     * Called when the Application is created
     */
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp( this );
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference().child( getString(R.string.media_location) );

        // If already logged in, setup user data
        if ( mAuth.getCurrentUser() != null ){
            setupUser();
        }


    }


    /**
     * Gets the Applications current user
     *
     * @return The current user
     */
    public User getUser(){

        return curUser;

    }


    /**
     * Gets the Application's current Database
     *
     * @return The current database's reference
     */
    public DatabaseReference getDatabase(){

        return mDatabase;

    }


    /**
     * Creates a listener for the current user for realtime updating
     */
    public void setupUser(){

        mAuth = FirebaseAuth.getInstance();
        String curID = mAuth.getCurrentUser().getUid();
        curUserReference = mDatabase.child( "users" ).child( curID );

        // Update user whenever information changes
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                curUser = dataSnapshot.getValue( User.class );
                curUser.setID( mAuth.getCurrentUser().getUid() );

                // Tell friendList user is found
                if ( friendListFragment != null ){
                    friendListFragment.setupFriendListener();
                }

                // Tell friendList user is found
                if ( messageListFragment != null ){
                    messageListFragment.setupMessageListeners();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( "SimpleSnapApp", "There was an error retrieving user info." );

            }
        };

        curUserReference.addValueEventListener( userListener );

    }

    /**
     * Getter for the current media
     * @return The media object
     */
    public Object getPassableMedia() {
        return passableMedia;
    }

    /**
     * Setter for the current media
     * @return The media object
     */
    public void setPassableMedia(Object passableMedia) {
        this.passableMedia = passableMedia;
    }

    /**
     * Gets the current storage location
     * @return The storage reference
     */
    public StorageReference getmStorage() {
        return mStorage;
    }
}
