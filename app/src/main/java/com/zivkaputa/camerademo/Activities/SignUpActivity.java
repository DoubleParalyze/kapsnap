package com.zivkaputa.camerademo.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.ErrorHandling.ToastUtils;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * SignUpActivity
 *
 * Activity for signing the user up
 */
public class SignUpActivity extends AppCompatActivity {


    @BindView( R.id.email ) EditText emailTextView;
    @BindView( R.id.username ) EditText usernameTextView;
    @BindView( R.id.password ) EditText passwordTextView;
    @BindView( R.id.confirm_password ) EditText confirmPasswordTextView;
    @BindView( R.id.firstName ) EditText firstNameTextView;
    @BindView( R.id.lastName ) EditText lastNameTextView;

    private FirebaseAuth mAuth;
    private User newUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    /**
     * Called when the activity begins
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.sign_up_screen );
        ButterKnife.bind( this );

        // Get firebase references
        FirebaseApp.initializeApp( this );
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final Context curContext = this;

        // Triggered when authorization state changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if ( firebaseAuth.getCurrentUser() != null ){

                    // Upload user information
                    newUser.setID( mAuth.getCurrentUser().getUid() );
                    newUser.uploadInitialInfo();

                    // Inform app to begin tracking user data
                    SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
                    curApp.setupUser();

                    // Open main app
                    Intent intent = new Intent( SignUpActivity.this , SwipeActivity.class );
                    startActivity( intent );
                }

            }
        };

        mAuth.addAuthStateListener( mAuthListener );


    }


    /**
     * Begins the signup process
     */
    @OnClick( R.id.sign_up_with_info )
    public void beginSignUpFlow(){

        // Get all text information from the view
        final String username = usernameTextView.getText().toString();
        final String email = emailTextView.getText().toString();
        String firstName = firstNameTextView.getText().toString();
        String lastName = lastNameTextView.getText().toString();
        final String password = passwordTextView.getText().toString();
        final String confirmPassword = confirmPasswordTextView.getText().toString();

        // If the fields are invalid stop the sign up process
        if ( !dataFieldsAreValid( username, email, firstName, lastName, password, confirmPassword ) ){
            return;
        }

        // Create the user's name
        String fullName = firstName + " " + lastName;

        newUser = new User( username, fullName, email );

        final DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().
                child("usernames").child( username );

        // Create listener to check if user already exists
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get user information and call callback method to interpret
                String userID = (String) dataSnapshot.getValue();
                boolean userExists = false;
                if ( userID != null ){
                    userExists = true;
                }
                usernameRef.removeEventListener( this );
                userSearchCallback( userExists, email, password, username );


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( "SignUpActivity", "Failed to check for user" );

            }
        };

        usernameRef.addValueEventListener( userListener );



    }


    /**
     * Determines whether the current data is valid
     *
     * @param username - The current username
     * @param email - The current email
     * @param firstName - The current first name
     * @param lastName - The current last name
     * @param password - The current password
     * @param confirmPassword - The current confirmed password
     *
     * @return boolean indicating whether or not the fields are valid
     */
    private boolean dataFieldsAreValid( String username,
                                        String email,
                                        String firstName,
                                        String lastName,
                                        String password,
                                        String confirmPassword  ){


        // Check that passwords are the same
        if ( !password.equals( confirmPassword ) ){
            ToastUtils.shortToast( "Passwords must be the same.", this );
            return false;
        }

        // Check that no fields are empty
        if ( username.isEmpty() || email.isEmpty() ){
            return false;
        }

        if ( password.isEmpty() || confirmPassword.isEmpty() ){
            return false;
        }

        if ( firstName.isEmpty() || lastName.isEmpty() ){
            return false;
        }

        return true;

    }


    /**
     * Handles user login and related error messages
     * (Called after user is checked for in database)
     *
     * @param userExists - Whether or not the user exists
     * @param email - The current email
     * @param password - The current password
     * @param username - The current username
     */
    private void userSearchCallback( boolean userExists, String email, String password, String username ){

        if ( userExists ){

            ToastUtils.shortToast( "Usern1ame is already taken.", this );
            return;

        } else {

            attemptSignUp( email, password, username );

        }

    }


    /**
     * Last stage in the signup process - Tells Firebase to create a new user
     *
     * @param email - The user's email
     * @param password - The user's password
     * @param username - The user's username
     */
    private void attemptSignUp( final String email, final String password, final String username ){

        // Create an account with the email and password
        mAuth.createUserWithEmailAndPassword( email, password )
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {

                                ToastUtils.shortToast( "User with this email already exist.", SignUpActivity.this );

                            } else {

                                ToastUtils.shortToast( "Invalid Email.", SignUpActivity.this );

                            }



                        }

                    }
                });


    }




}
