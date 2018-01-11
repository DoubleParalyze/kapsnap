package com.zivkaputa.camerademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import com.zivkaputa.camerademo.UserModel.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * Tests basic calls to a Firebase Database.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseTest {

    public static final int DATABASE_SLEEP_TIME = 2000;
    private Context context;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private User firstUser;
    private User secondUser;
    private DatabaseReference firstUserReference;
    private DatabaseReference secondUserReference;
    private DatabaseReference usernameReference;
    private String firstUserID = "123456789az";
    private String secondUserID = "987654321b";


    /**
     * Sets up global variables required for Firebase access
     * @throws Exception
     */
    @Before
    public void useAppContext() throws Exception {

        // Setup app context and database
        context = InstrumentationRegistry.getTargetContext();
        assertEquals("com.zivkaputa.storagedemo4", context.getPackageName());

        FirebaseApp.initializeApp( context );
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference().child( "media" );

        // Set frequently-used references
        firstUserReference = mDatabase.child("users").child( firstUserID );
        secondUserReference = mDatabase.child("users").child( secondUserID );
        usernameReference = mDatabase.child("usernames");



    }

    /**
     * Creates two User objects for upload to the database
     */
    public void setupUsersWithLocalData(){

        String name = "John Smith";
        String username = "Jsmith";
        String email = "john.smith@fakemail.com";
        firstUser = new User( name, username, email );

        name = "Jack Brown";
        username = "JackBBB";
        email = "jack.brown@fakemail.com";
        secondUser = new User( name, username, email );

    }


    /**
     * Uses information on the database to read two User objects
     * @throws Exception
     */
    public void setupUsersWithOnlineData() throws Exception{

        // Listener for the first User
        ValueEventListener firstUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get User object and compare to manually created object
                User newUser = dataSnapshot.getValue( User.class );

                if ( newUser != null ) {
                    firstUser = newUser;
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Getting Post failed, log a message
                fail( "Could not download data" );

            }
        };


        // Listener for the second User
        ValueEventListener secondUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get User object and compare to manually created object
                User newUser = dataSnapshot.getValue( User.class );

                if ( newUser != null ) {
                    secondUser = newUser;
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Getting Post failed, log a message
                fail( "Could not download data" );

            }
        };

        // Add listeners and delay ( to allow for download )
        firstUserReference.addValueEventListener( firstUserListener );
        secondUserReference.addValueEventListener( secondUserListener );

        pauseForDatabase();

    }

    /**
     * Clears the two users from the database
     * @throws Exception
     */
    @Test
    public void clearUsers() throws Exception{

        secondUserReference.removeValue();
        firstUserReference.removeValue();
        usernameReference.removeValue();
        pauseForDatabase();

    }

    /**
     * Simple delay function to allow for database actions
     * @throws Exception
     */
    public void pauseForDatabase() throws Exception {

        Thread.sleep( DATABASE_SLEEP_TIME );

    }


    /**
     * Uploads the first user's data to the database, then checks to see that it was uploaded properly
     * @throws Exception
     */
    @Test
    public void uploadFirstUser() throws Exception {

        setupUsersWithLocalData();

        firstUserReference.setValue( firstUser );

        // The error-checking is done in the listener below
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );

        usernameReference.child( firstUser.fullName ).setValue( firstUserID );


        pauseForDatabase();

    }


    /**
     * Uploads the first user's data to the database, then checks to see that it was uploaded properly
     * @throws Exception
     */
    @Test
    public void uploadSecondUser() throws Exception {

        setupUsersWithLocalData();

        secondUserReference.setValue( secondUser );

        // The error-checking is done in the listener below
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );

        usernameReference.child( secondUser.fullName ).setValue( secondUserID );


        pauseForDatabase();

    }


    /**
     * Imitates a friend request between to users. Uploads and downloads data for error-checking.
     * @throws Exception
     */
    @Test
    public void requestFriend() throws Exception {


        setupUsersWithOnlineData();

        // Manually create friend request
        FriendRequest newFriendReq = new FriendRequest( firstUserID, null, null,  "10/12/13:4:46:09" );
        FriendRequest basicFriendReq = new FriendRequest( null, null, null, "10/12/13:4:46:09" );

        DatabaseReference requestList = secondUserReference.child( "incoming_requests" );
        DatabaseReference friendList = firstUserReference.child( "friends" );

        requestList.child( firstUserID ).setValue( newFriendReq );
        friendList.child( secondUserID ).setValue( basicFriendReq );


        // Compare resulting values
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );


        pauseForDatabase();


    }


    /**
     * Imitates a friend acceptance. Uploads and downloads data for error-checking.
     * @throws Exception
     */
    @Test
    public void acceptFriend() throws Exception{


        setupUsersWithOnlineData();
        DatabaseReference requestList = secondUserReference.child( "incoming_requests" );
        DatabaseReference friendList = firstUserReference.child( "friends" );

        // Accept friend in database
        requestList.child( firstUserID ).removeValue();
        friendList.child( secondUserID ).child( "accepted" ).setValue( true );


        // Compare values
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );


        pauseForDatabase();


    }

    /**
     * Imitates a friend denial. Uploads and downloads data for error-checking.
     * @throws Exception
     */
    @Test
    public void denyFriend() throws Exception {


        setupUsersWithOnlineData();
        DatabaseReference requestList = secondUserReference.child( "incoming_requests" );
        DatabaseReference friendList = firstUserReference.child( "friends" );

        // Remove pending friend and request
        requestList.child( firstUserID ).removeValue();
        friendList.child( secondUserID ).removeValue();


        // Compare values
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );


        pauseForDatabase();


    }


    /**
     * Imitates sending media message information between users. Uploads and downloads
     * data for error-checking.
     *
     * @throws Exception
     */
    @Test
    public void sendMediaMessage() throws Exception {


        setupUsersWithOnlineData();

        // Manually create friend request
        MediaMessage newMediaMessage = new MediaMessage( firstUserID, secondUserID, "http://whatever", MediaMessage.MediaType.VIDEO, "10/12/13:4:46:09", 5000 );

        // Get list references
        DatabaseReference incomingRef = secondUserReference.child( "incoming_media" );
        DatabaseReference outgoingRef = firstUserReference.child( "outgoing_media" );

        incomingRef.child( firstUserID ).setValue( newMediaMessage );
        outgoingRef.child( secondUserID ).setValue( newMediaMessage );


        // Compare values
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );


        pauseForDatabase();


    }


    /**
     * Imitates opening a media message. Uploads and downloads data for error-checking.
     * @throws Exception
     */
    @Test
    public void openMediaMessage() throws Exception {


        setupUsersWithOnlineData();

        // Manually create friend request
        MediaMessage newMediaMessage = new MediaMessage( firstUserID, secondUserID, "http://whatever", MediaMessage.MediaType.VIDEO, "10/12/13:4:46:09", 5000 );

        // Get list references
        DatabaseReference incomingRef = secondUserReference.child( "incoming_media" );
        DatabaseReference outgoingRef = firstUserReference.child( "outgoing_media" );

        /// Mark message as opened in the database
        incomingRef.child( firstUserID ).child( "opened" ).setValue( true );
        outgoingRef.child( secondUserID ).child( "opened" ).setValue( true );


        // Compare values
        firstUserReference.addValueEventListener( compareResultingUserTo( firstUser ) );
        secondUserReference.addValueEventListener( compareResultingUserTo( secondUser ) );


        pauseForDatabase();


    }


    /**
     * Uploads a photo to Firebase storage
     *
     * ( In the real app, the photo will not be an Android Studio resource, but rather
     *  a bitmap or video file generated by the camera )
     *
     * @throws Exception
     */
    @Test
    public void uploadPhoto() throws Exception{


        // Define media information
        String imageName = secondUserID + ":" + "123123";
        StorageReference testImageRef = mStorage.child( firstUserID ).child( imageName );
        Bitmap testImage = BitmapFactory.decodeResource( context.getResources(), R.drawable.login_logo );

        // Begin upload process
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });

        pauseForDatabase();

    }


    @Test
    public void deleteImage() throws Exception {

        // Define media information
        String imageName = secondUserID + ":" + "123123";
        StorageReference testImageRef = mStorage.child( firstUserID ).child( imageName );

        // Delete the file
        testImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

        pauseForDatabase();

    }

    /**
     * Defines a listener object that compares the database information with a local object
     *
     * @param manualUser The user to compare
     * @throws Exception
     */
    public ValueEventListener compareResultingUserTo( final User manualUser ){

        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get User object and compare to manually created object
                User newUser = dataSnapshot.getValue( User.class );

                if ( newUser != null ) {
                    assertTrue( newUser.equals( manualUser ) );
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Getting Post failed, log a message
                fail( "Could not download data" );

            }
        };

        return userListener;

    }





}

