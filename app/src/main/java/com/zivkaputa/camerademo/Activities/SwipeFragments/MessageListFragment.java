package com.zivkaputa.camerademo.Activities.SwipeFragments;

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
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.FriendAdapter;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.MediaAdapter;
import com.zivkaputa.camerademo.Activities.RecyclerViewAdapters.RequestAdapter;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.FriendRequest;
import com.zivkaputa.camerademo.UserModel.MediaMessage;
import com.zivkaputa.camerademo.UserModel.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * MessageListFragment
 *
 * Fragment for displaying and interacting with the user's incoming messages
 */
public class MessageListFragment extends Fragment{

    @BindView( R.id.messages_recycler_view ) RecyclerView mRecyclerView;
    @BindView( R.id.main_message_layout ) LinearLayout mainLayout;

    private ArrayList<MediaMessage> allMessages = new ArrayList<>();
    private ArrayList<MediaMessage> incomingMessages = new ArrayList<>();
    private ArrayList<MediaMessage> outgoingMessages = new ArrayList<>();
    private MediaAdapter messageAdapter;
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

        View view = inflater.inflate(R.layout.messages_list, container, false);
        ButterKnife.bind( this, view );

        // Setup the recycler view
        mRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity(), LinearLayoutManager.VERTICAL, false ) );

        SimpleSnapApp curApp = (SimpleSnapApp) getActivity().getApplication();

        messageAdapter = new MediaAdapter( allMessages, curApp, mainLayout, getActivity() );
        mRecyclerView.setAdapter( messageAdapter );

        curApp.messageListFragment = this;

        // Set listener to update recyclerView
        if ( curApp.getUser() != null ){
            setupMessageListeners();
        }

        return view;

    }


    /**
     * Sets up the listener used to pull data from the database and update the recycler view
     */
    public void setupMessageListeners(){

        initialized = true;

        SimpleSnapApp curApp = (SimpleSnapApp) getActivity().getApplication();
        DatabaseReference mDatabase = curApp.getDatabase();
        User curUser = curApp.getUser();

        DatabaseReference incomingRef = mDatabase.child(getString(R.string.users_db_tag))
                .child( curUser.ID ).child("incoming_media");
        final DatabaseReference outgoingRef = mDatabase.child(getString(R.string.users_db_tag))
                .child( curUser.ID ).child("outgoing_media");

        // Create listener for any changes in the request list
        ValueEventListener incomingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                incomingMessages.clear();

                for ( DataSnapshot messageSnapshot : dataSnapshot.getChildren() ){

                    MediaMessage curMessage = messageSnapshot.getValue(MediaMessage.class);
                    curMessage.incoming = true;
                    incomingMessages.add( curMessage );
                }

                allMessages.clear();
                allMessages.addAll( incomingMessages );
                allMessages.addAll( outgoingMessages );
                sortMessages();

                // Update recycler view
                messageAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.message_list_tag),getString(R.string.failed_to_get_messages));

            }
        };

        incomingRef.addValueEventListener( incomingListener );



        // Create listener for any changes in the outgoing list list
        ValueEventListener outgoingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                outgoingMessages.clear();

                for ( DataSnapshot messageSnapshot : dataSnapshot.getChildren() ){

                    MediaMessage curMessage = messageSnapshot.getValue(MediaMessage.class);
                    curMessage.incoming = false;
                    outgoingMessages.add( curMessage );

                }

                allMessages.clear();
                allMessages.addAll( incomingMessages );
                allMessages.addAll( outgoingMessages );
                sortMessages();

                // Update recycler view
                messageAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d( getString(R.string.message_list_tag),getString(R.string.failed_to_get_messages));

            }
        };

        outgoingRef.addValueEventListener( outgoingListener );


    }


    /**
     * Sorts the message by time
     */
    private void sortMessages(){

        Collections.sort( allMessages, new Comparator<MediaMessage>() {
            DateFormat f = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            @Override
            public int compare( MediaMessage message1, MediaMessage message2 ) {
                try {
                    return f.parse(message2.time).compareTo(f.parse(message1.time));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

    }



}
