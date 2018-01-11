package com.zivkaputa.camerademo.UserModel;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Ziv on 4/18/17.
 */

/**
 * User
 *
 * Basic user class to hold data for each user
 */
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String fullName;

    @Exclude
    public static final String MEDIA_PATH = "media";

    @Exclude
    public static final int DEFAULT_PHOTO_TIME_MILLISECONDS = 8000;

    @Exclude
    public String ID;

    @Exclude
    DatabaseReference mDatabase;

    @Exclude
    StorageReference mStorage;

    @Exclude
    DatabaseReference userReference;

    @Exclude
    DatabaseReference usernamesReference;

    public Map<String,FriendRequest> incoming_requests;
    public Map<String,MediaMessage> incoming_media;
    public Map<String,MediaMessage> outgoing_media;
    public Map<String,FriendRequest> friends;

    /**
     * Default constructor ( required for calls to DataSnapshot.getValue(User.class) )
     */
    public User() {

    }


    /**
     * Constructor for user object
     *
     * @param username - The new user's username
     * @param fullName - The new user's full name
     * @param email - The new user's email
     */
    public User( String username, String fullName, String email ) {

        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.ID = null;
        this.incoming_media = new HashMap<>();
        this.incoming_requests = new HashMap<>();
        this.outgoing_media = new HashMap<>();
        this.friends = new HashMap<>();

    }


    /**
     * Sets the user's ID
     * @param ID
     */
    public void setID( String ID ){
        this.ID = ID;
    }


    /**
     * Sets the database references required for other User methods
     */
    public void setGlobalReferences(){

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference().child( MEDIA_PATH );
        userReference = mDatabase.child("users").child( ID );
        usernamesReference = mDatabase.child( "usernames" ).child( username );

    }


    /**
     * Uploads the user's information to Firebase
     */
    public void uploadInitialInfo(){

        setGlobalReferences();

        userReference.setValue( this );
        usernamesReference.setValue( ID );

    }


    /**
     * Sends a friend request to the specified user
     *
     * @param friendID - The ID of the requested user
     */
    public void addFriend( String friendID ){

        setGlobalReferences();

        // Get current time to send with request
        String curTime = getCurrentTime();

        // Manually create friend request
        FriendRequest newFriendReq = new FriendRequest( ID, username, fullName, curTime );
        FriendRequest basicFriendReq = new FriendRequest( null, null, null, curTime );

        DatabaseReference friendReference = mDatabase.child( "users" ).child( friendID );
        DatabaseReference requestList = friendReference.child( "incoming_requests" );
        DatabaseReference friendList = userReference.child( "friends" );

        requestList.child( ID ).setValue( newFriendReq );
        friendList.child( friendID ).setValue( basicFriendReq );

    }


    /**
     * Accepts a friend from the incoming request list
     *
     * @param friendID - The friend to accept
     */
    public void acceptFriend( String friendID ){

        setGlobalReferences();

        DatabaseReference friendReference = mDatabase.child("users").child( friendID );
        DatabaseReference requestList = userReference.child( "incoming_requests" );
        DatabaseReference friendfriendList = friendReference.child( "friends" );
        DatabaseReference friendList = userReference.child( "friends" );

        // Accept friend in database
        requestList.child( friendID ).removeValue();

        friendList.child( friendID ).setValue( incoming_requests.get( friendID ) );
        friendList.child( friendID ).child( "accepted" ).setValue( true );

        friendfriendList.child( ID ).child( "accepted" ).setValue( true );
        friendfriendList.child( ID ).child( "senderFullName" ).setValue( fullName );
        friendfriendList.child( ID ).child( "senderID" ).setValue( ID );
        friendfriendList.child( ID ).child( "senderUsername" ).setValue( username );




    }


    /**
     * Rejects a friend from the incoming request list
     *
     * @param friendID - The friend to reject
     */
    public void rejectFriend( String friendID ){

        setGlobalReferences();

        DatabaseReference friendReference = mDatabase.child("users").child( friendID );
        DatabaseReference requestList = userReference.child( "incoming_requests" );
        DatabaseReference friendList = friendReference.child( "friends" );

        // Accept friend in database
        requestList.child( friendID ).removeValue();
        friendList.child( ID ).removeValue();


    }


    /**
     * Sends a message to the given users
     *
     * @param recipientIDs - The recipient's IDs
     * @param type - The media type
     * @param media - The media object
     */
    public void sendMessages( HashSet<String> recipientIDs, MediaMessage.MediaType type, Object media ){

        setGlobalReferences();

        String mediaPath = ID + System.currentTimeMillis();

        // Upload the media once
        if ( type == MediaMessage.MediaType.PHOTO ){
            uploadPhoto( (Bitmap) media, mediaPath );
        } else if ( type == MediaMessage.MediaType.VIDEO ){
            uploadVideo( (File) media, mediaPath );
        }

        for ( String recipientID : recipientIDs ){
            sendMessage( recipientID, type, mediaPath );
        }



    }

    /**
     * Sends a message to the given user
     *
     * @param recipientID - The recipient's ID
     * @param type - The media type
     * @param path - The path the media was uploaded to
     */
    private void sendMessage( String recipientID, MediaMessage.MediaType type, String path ){

        // Create message object
        MediaMessage newMediaMessage = new MediaMessage( ID, recipientID, path, type,
                getCurrentTime(), DEFAULT_PHOTO_TIME_MILLISECONDS );

        // Get list references
        DatabaseReference incomingRef = mDatabase.child("users").child( recipientID ).
                child( "incoming_media" );
        DatabaseReference outgoingRef = userReference.child( "outgoing_media" );

        incomingRef.child( path ).setValue( newMediaMessage );
        outgoingRef.child( path ).setValue( newMediaMessage );


    }


    /**
     * Uploads the photo to firebase storage
     *
     * @param photo - The photo to be uploaded
     * @param path - The path to upload the photo to
     */
    public void uploadPhoto( Bitmap photo, String path ){

        // Define media information
        String imageName = path;
        StorageReference testImageRef = mStorage.child( ID ).child( imageName );

        // Begin upload process
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = testImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

            }
        });


    }


    /**
     * Uploads the video to firebase storage
     *
     * @param video - The video to be uploaded
     * @param path - The path to upload the video to
     */
    public void uploadVideo( File video, String path ){

        // Define media information
        String videoName = path;
        StorageReference videoRef = mStorage.child( ID ).child( videoName );

        // Begin upload process
        InputStream stream = null;
        try {
            stream = new FileInputStream(video);
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        }

        UploadTask uploadTask = videoRef.putStream( stream );
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });


    }

    /**
     * Opens a message and updates the database
     *
     * @param messageToOpen The message object
     */
    public void openMessage( MediaMessage messageToOpen ){

        setGlobalReferences();


        // Get list references
        DatabaseReference incomingRef = mDatabase.child("users").child( messageToOpen.recipient ).
                child( "incoming_media" );
        DatabaseReference outgoingRef = mDatabase.child("users").child( messageToOpen.sender ).
                child( "outgoing_media" );

        /// Mark message as opened in the database
        incomingRef.child( messageToOpen.path ).child( "opened" ).setValue( true );
        incomingRef.child( messageToOpen.path ).child( "time" ).setValue( getCurrentTime() );

        outgoingRef.child( messageToOpen.path ).child( "opened" ).setValue( true );
        outgoingRef.child( messageToOpen.path ).child( "time" ).setValue( getCurrentTime() );


    }


    /**
     * Gets the current time
     *
     * @return The current time as a string
     */
    @Exclude
    public String getCurrentTime(){

        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date date = new Date();

        return df.format(date);

    }

    /**
     * Overidden equals method
     *
     * @param obj
     * @return Whether or not the objects are equal
     */
    @Override
    public boolean equals( Object obj ) {

        if ( obj == null ) {
            return false;
        }

        if ( !(obj instanceof User) ) {
            return false;
        }

        final User other = (User) obj;

        return  this.username.equals( other.username ) &&
                this.email.equals( other.email );


    }


}