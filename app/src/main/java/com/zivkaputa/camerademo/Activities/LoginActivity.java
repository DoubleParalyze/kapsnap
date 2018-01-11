package com.zivkaputa.camerademo.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.zivkaputa.camerademo.ErrorHandling.ToastUtils;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * LoginActivity
 *
 * Activity for logging user in with Firebase Authentication
 */
public class LoginActivity extends AppCompatActivity{

    @BindView( R.id.sign_up_button ) TextView signUpButton;
    @BindView( R.id.email ) EditText emailTextView;
    @BindView( R.id.password ) EditText passwordTextView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ButterKnife.bind( this );

        // Initialize Firebase and check authorization
        FirebaseApp.initializeApp( this );
        mAuth = FirebaseAuth.getInstance();

        signUpButton.setClickable(true);
        signUpButton.setMovementMethod(LinkMovementMethod.getInstance());

        final Context curContext = this;

        // Triggered when authorization state changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if ( firebaseAuth.getCurrentUser() != null ){

                    // Inform app to begin tracking user data
                    SimpleSnapApp curApp = (SimpleSnapApp) getApplication();
                    curApp.setupUser();

                    // Open main app
                    Intent intent = new Intent( curContext, SwipeActivity.class );
                    startActivity( intent );
                }

            }
        };

        mAuth.addAuthStateListener( mAuthListener );

    }


    /**
     * Called when the android back button is pressed
     */
    @Override
    public void onBackPressed() {
        // Do nothing - Don't want user to go back to welcome screen
    }


    /**
     * Launches the sign up activity
     */
    @OnClick( R.id.sign_up_button )
    public void startSignUpActivity(){

        Intent intent = new Intent( this, SignUpActivity.class );
        startActivity( intent );

    }


    /**
     * Starts the login process
     */
    @OnClick( R.id.login )
    public void logIn(){

        final Context curContext = this;

        // Perform basic data validation and attempt to login
        if ( fieldsAreValid() ){

            String email = emailTextView.getText().toString();
            String password = passwordTextView.getText().toString();

            mAuth.signInWithEmailAndPassword( email, password )
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if ( !task.isSuccessful() ) {
                                ToastUtils.shortToast( getString(R.string.incorrect_user_or_pass), curContext );
                            }

                        }

                    });


        }


    }


    /**
     * Determines whether the given input fields are valid
     *
     * @return boolean indicating whether the fields are valid
     */
    private boolean fieldsAreValid(){

        String email = emailTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        return ( !email.isEmpty() && !password.isEmpty() );

    }




}
