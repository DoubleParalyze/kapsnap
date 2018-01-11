package com.zivkaputa.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.zivkaputa.camerademo.Activities.ChooseFriendActivity;
import com.zivkaputa.camerademo.Activities.SwipeFragments.CameraFragment;
import com.zivkaputa.camerademo.UserModel.MediaMessage;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Ziv on 5/3/17.
 */

public class PreviewPopup extends PopupWindow {

    private PreviewManager previewManager;

    PreviewPopup( View view, int width, int height ){
        super( view, width, height );
    }

    public static PreviewPopup getRecordPreviewPopup(final Object media, final CameraFragment camFrag ){

        final Context context = camFrag.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View popupView = inflater.inflate( R.layout.preview_popup, null );
        ConstraintLayout mainConstraintView = (ConstraintLayout) popupView.findViewById( R.id.preview_constraint_view );

        // Get the image/video we need as a view
        final PreviewManager pManager = new PreviewManager( media, context );
        View mediaView = pManager.getPreview();

        // Add the media to the popup
        mainConstraintView.addView( mediaView );

        // Bring buttons to front in view
        ImageButton beginSendButton = (ImageButton) popupView.findViewById( R.id.begin_send_message );
        beginSendButton.bringToFront();

        ImageButton cancelButton = (ImageButton) popupView.findViewById( R.id.cancel_from_media );
        cancelButton.bringToFront();

        final PreviewPopup previewPopup = new PreviewPopup( popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        final MediaMessage.MediaType curType = pManager.getCurType();

        previewPopup.previewManager = pManager;

        //Set button actions
        beginSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Pass info to friend-choosing activity
                Intent intent = new Intent( context , ChooseFriendActivity.class );
                intent.putExtra(context.getString(R.string.media_type_extra_tag), curType);
                ((SimpleSnapApp) ((Activity) context).getApplication()).setPassableMedia( media );
                context.startActivity( intent );

            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                camFrag.hidePreview();
                pManager.clean();
                previewPopup.dismiss();

            }
        });


        return previewPopup;

    }


    public static PreviewPopup getViewPreviewPopup(final Object media, final Context context ){


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        View popupView = inflater.inflate( R.layout.preview_popup, null );
        ConstraintLayout mainConstraintView = (ConstraintLayout) popupView.findViewById( R.id.preview_constraint_view );

        // Get the image/video we need as a view
        final PreviewManager pManager = new PreviewManager( media, context );
        View mediaView = pManager.getPreview();

        // Add the media to the popup
        mainConstraintView.addView( mediaView );

        // Bring buttons to front in view
        ImageButton beginSendButton = (ImageButton) popupView.findViewById( R.id.begin_send_message );
        beginSendButton.setVisibility(View.GONE);

        ImageButton cancelButton = (ImageButton) popupView.findViewById( R.id.cancel_from_media );
        cancelButton.setVisibility(View.GONE);

        final PreviewPopup previewPopup = new PreviewPopup( popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        final MediaMessage.MediaType curType = pManager.getCurType();

        previewPopup.previewManager = pManager;

        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pManager.clean();
                previewPopup.dismiss();

            }
        });


        return previewPopup;

    }



    /**
     * Gets the current preview manager
     * @return The PreviewManager associated with the preview
     */
    public PreviewManager getPreviewManager() {
        return previewManager;
    }
}
