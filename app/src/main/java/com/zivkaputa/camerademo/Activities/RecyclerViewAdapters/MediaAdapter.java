package com.zivkaputa.camerademo.Activities.RecyclerViewAdapters;

/**
 * Created by Ziv on 5/5/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zivkaputa.camerademo.PreviewPopup;
import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.SimpleSnapApp;
import com.zivkaputa.camerademo.UserModel.MediaMessage;
import com.zivkaputa.camerademo.UserModel.User;

import java.io.File;
import java.util.ArrayList;


/**
 * MediaAdapter
 *
 * Contains methods for updating media recycler view content
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    ArrayList<MediaMessage> messages;
    SimpleSnapApp curApp;
    LinearLayout mainView;
    Activity parentActivity;

    /**
     * Constructor for MediaAdapter
     *
     * @param messages - The messages to be used in the adapter
     */
    public MediaAdapter( ArrayList<MediaMessage> messages, SimpleSnapApp curApp,
                         LinearLayout mainView, Activity parentActivity ) {

        this.messages = messages;
        this.curApp = curApp;
        this.mainView = mainView;
        this.parentActivity = parentActivity;

    }


    /**
     * Fills the on-screen elements with information and recycles views when scrolling is complete
     *
     * @param parent the parent object ( RecyclerView )
     * @param viewType the type of view to fill
     * @return the new ViewHolder allocated
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType ) {

        // use XML definition to create an object
        final View messageItem = LayoutInflater.from( parent.getContext() ).
                inflate( R.layout.mesage_list_item, parent, false );

        return new ViewHolder( messageItem );

    }

    /**
     * This function gets called each time a ViewHolder needs to hold data for a different
     * position in the list.  We don't need to create any views (because we're recycling), but
     * we do need to update the contents in the views.
     *
     * @param holder the ViewHolder that knows about the Views we need to update
     * @param position the index into the array of messages
     */
    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {

        final MediaMessage message = messages.get( position );
        final ViewHolder curHolder = holder;

        User curUser = curApp.getUser();

        curHolder.media = message;
        curHolder.parentView = mainView;
        curHolder.parentActivity = parentActivity;

        // Differentiate between incoming and outgoing messages
        if ( message.incoming ){
            // Incoming message
            curHolder.usernameView.setText( curUser.friends.get( message.sender ).senderUsername );
            curHolder.fullNameView.setText( curUser.friends.get( message.sender ).senderFullName );
            curHolder.timeView.setText( "Received " + message.time );
            curHolder.instructionView.setText( "Tap to Load." );
            curHolder.viewable = true;


        } else {

            // Outgoing message
            curHolder.usernameView.setText( curUser.friends.get( message.recipient ).senderUsername );
            curHolder.fullNameView.setText( curUser.friends.get( message.recipient ).senderFullName );
            curHolder.timeView.setText( "Sent " + message.time );
            curHolder.instructionView.setText( "" );
            curHolder.viewable = false;

        }


        if ( message.opened ){
            curHolder.timeView.setText( "Opened " + message.time );
            curHolder.instructionView.setText("");
            curHolder.viewable = false;
        }

        // Get the correct image
        String iconPath = message.type == MediaMessage.MediaType.PHOTO ? "photo_" : "video_";
        iconPath += message.incoming? "incoming_" : "outgoing_";
        iconPath += message.opened ? "opened" : "unopened";

        Context context = curHolder.iconView.getContext();
        int id = context.getResources().getIdentifier(iconPath, "drawable", context.getPackageName());
        curHolder.iconView.setImageResource(id);

        curHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curHolder.handleClick();
            }
        });





    }

    /**
     * Gets the number of items in the collection. Used by the Recycler view to determine when
     * end of collection is reached
     *
     * @return the number of friends in the array.
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }


    /**
     * A ViewHolder class for our adapter that 'caches' the references to the
     * subviews, so we don't have to look them up each time.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public ImageView iconView;
        public TextView fullNameView;
        public TextView usernameView;
        public TextView instructionView;
        public TextView timeView;

        public MediaMessage media;
        public File rawMedia;

        public boolean downloaded;
        public boolean viewed;
        public boolean viewable;


        public Activity parentActivity;
        public LinearLayout parentView;


        public ViewHolder( View itemView ) {

            // Link the members with their widgets in the XML
            super(itemView);
            view = itemView;
            iconView = (ImageView) itemView.findViewById( R.id.icon_view );
            fullNameView = (TextView) itemView.findViewById( R.id.message_full_name );
            usernameView = (TextView) itemView.findViewById( R.id.message_username );
            instructionView = (TextView) itemView.findViewById( R.id.message_instruction );
            timeView = (TextView) itemView.findViewById( R.id.message_time_date );


        }

        public void handleClick(){

            if ( !viewable ){
                return;
            }

            if (!downloaded){
                instructionView.setText("Loading...");
                downloadMediaAndUpdateView();
            } else if (!viewed){
                instructionView.setText("");
                ((SimpleSnapApp) parentActivity.getApplication()).getUser().openMessage( media );
                showMedia();
            }

        }

        public void downloadMediaAndUpdateView(){

            String suffix = (media.type == MediaMessage.MediaType.PHOTO) ? "bmp" : "mp4";

            try {
                rawMedia = File.createTempFile("media", suffix);
            } catch (Exception e){
                e.printStackTrace();
            }

            StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("media");
            StorageReference mediaRef = mStorage.child( media.sender ).child( media.path );
            mediaRef.getFile( rawMedia ).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    instructionView.setText("Tap to view.");
                    downloaded = true;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });


        }

        /**
         * Show the media
         */
        public void showMedia(){

            Object correctTypeMedia;

            if ( media.type == MediaMessage.MediaType.PHOTO ){
                String filePath = rawMedia.getPath();
                Bitmap image = BitmapFactory.decodeFile(filePath);
                correctTypeMedia = image;
            } else {
                correctTypeMedia = rawMedia;
            }

            PreviewPopup pWindow = PreviewPopup.getViewPreviewPopup( correctTypeMedia, parentActivity );
            viewable = false;

            pWindow.showAtLocation( parentView, Gravity.CENTER,0,0);

        }

    }
}


