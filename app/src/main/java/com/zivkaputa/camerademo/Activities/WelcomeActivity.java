package com.zivkaputa.camerademo.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Ziv on 5/1/17.
 */

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);

        // Initialize Firebase and check authorization
        FirebaseApp.initializeApp( this );
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        // If the user is signed in go straight to the app, otherwise log in
        if ( mAuth.getCurrentUser() != null ){
            Intent intent = new Intent(this, SwipeActivity.class);
            startActivity( intent );
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity( intent );
        }

    }


}
