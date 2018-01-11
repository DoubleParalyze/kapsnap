package com.zivkaputa.camerademo.Activities.SwipeFragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;


import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.zivkaputa.camerademo.PreviewPopup;
import com.zivkaputa.camerademo.PreviewManager;
import com.zivkaputa.camerademo.R;

import java.io.File;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * CameraFragment
 *
 * An activity that allows the user to view a live camera preview and to capture images and videos
 *
 * Relies the CameraKit library for functionality ( https://github.com/gogopop/CameraKit-Android )
 *
 */
public class CameraFragment extends android.support.v4.app.Fragment {

    @BindView(R.id.camera) CameraView cameraView;
    @BindView(R.id.captureImage) ImageButton captureImageButton;
    @BindView(R.id.startVideo) ImageButton startVideoButton;
    @BindView(R.id.change_media_type) ImageButton changeModeButton;
    @BindView(R.id.stopVideo) ImageButton stopVideoButton;
    @BindView(R.id.video_length_bar) ProgressBar videoTimeBar;


    @BindView(R.id.mainConstraintView) ConstraintLayout mainConstraintView;


    // Used only for cleaning up media when app is closed
    PreviewManager curPreviewManager;

    PreviewPopup pWindow;
    Thread startThread;
    boolean stillPhotoMode = true;
    boolean recording = false;
    int recordTimeMilliseconds = 0;

    public static final int CAMERA_DELAY_MILLISECONDS = 500;
    public static final int MAX_VIDEO_TIME = 6000;



    /**
     * Called when the fragment is created
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {

        super.onCreate(savedInstanceState);


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_camera, container, false);
        ButterKnife.bind( this, view );


        // Set listeners for camera tor retrieve image and video data
        cameraView.setCameraListener( new CameraListener() {

            // Called when an image is taken
            @Override
            public void onPictureTaken( byte[] picture ) {
                super.onPictureTaken( picture );

                // Create a bitmap image and show it in the imageView
                Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                displayMedia( result );

            }

            // Called when a video is completed
            @Override
            public void onVideoTaken( File video ) {

                super.onVideoTaken( video );
                displayMedia( video );

            }
        } );

        return view;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        videoTimeBar.setMax( MAX_VIDEO_TIME );
        videoTimeBar.setProgress( 0 );
        videoTimeBar.getProgressDrawable().setColorFilter( ContextCompat.getColor( getContext(), R.color.themePrimary ), android.graphics.PorterDuff.Mode.MULTIPLY);



    }

    /**
     * Displays the media by generating and displaying a new view
     *
     * @param media - The media to be displayed
     */
    public void displayMedia( Object media ) {

        pWindow = PreviewPopup.getRecordPreviewPopup( media, this );
        pWindow.showAtLocation( mainConstraintView, Gravity.CENTER,0,0);

    }


    /**
     * Takes an image
     */
    @OnClick(R.id.captureImage)
    public void captureImage() {

        cameraView.captureImage();

    }


    /**
     * Starts the video countdown
     */
    public void startCountDown(){

        recordTimeMilliseconds = MAX_VIDEO_TIME;
        updateVideoCountDownTime();

    }


    /**
     * Starts the video countdown
     */
    public void updateVideoCountDownTime(){

        final Handler h = new Handler();
        final int delay = 10;

        h.postDelayed(new Runnable(){
            public void run(){


                recordTimeMilliseconds -= delay;
                videoTimeBar.setProgress( recordTimeMilliseconds );

                if ( recordTimeMilliseconds < 0 ){
                    stopVideo();
                }

                if ( recording ) {
                    h.postDelayed(this, delay);
                }

            }
        }, delay);

    }




    /**
     * Begins video capture
     */
    @OnClick(R.id.startVideo)
    public void startVideo() {

        recording = true;
        startCountDown();
        cameraView.startRecordingVideo();
        configureButtonsForRecording();

    }


    /**
     * Switches buttons
     */
    @OnClick(R.id.change_media_type)
    public void switchButtons() {

        if ( stillPhotoMode ){

            stillPhotoMode = false;
            captureImageButton.setVisibility( View.GONE );
            startVideoButton.setVisibility( View.VISIBLE );

        } else {

            stillPhotoMode = true;
            captureImageButton.setVisibility( View.VISIBLE );
            startVideoButton.setVisibility( View.GONE );

        }


    }


    /**
     * Stops video capture
     */
    @OnClick(R.id.stopVideo)
    public void stopVideo() {

        if ( recording ) {
            recording = false;
            cameraView.stopRecordingVideo();
        }

    }


    /**
     * Hides the preview View
     */
    public void hidePreview() {

        showMainButtons();

    }


    /**
     * Shows the main capture buttons
     */
    private void showMainButtons() {

        videoTimeBar.setVisibility( View.GONE );
        if ( stopVideoButton.getVisibility() == View.VISIBLE ){
            stopVideoButton.setVisibility( View.GONE );
            startVideoButton.setVisibility( View.VISIBLE );
        }
        changeModeButton.setVisibility( View.VISIBLE );

    }


    /**
     * Configures the buttons for recording by removing the "Take Image" and "Start Video" buttons
     * and replacing them with a single "Stop Video" button
     */
    private void configureButtonsForRecording() {

        captureImageButton.setVisibility(View.GONE);
        startVideoButton.setVisibility(View.GONE);
        stopVideoButton.setVisibility(View.VISIBLE);
        changeModeButton.setVisibility(View.GONE);
        videoTimeBar.setVisibility(View.VISIBLE);

    }


    /**
     * Called when the Activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // If media is playing, clean it up before exiting
        if ( curPreviewManager != null ){

            curPreviewManager.clean();

        }


    }


    /**
     * Called when the Activity resumes
     */
    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                startThread = Thread.currentThread();
                try {
                    startThread.sleep( CAMERA_DELAY_MILLISECONDS );
                    cameraView.start();
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }).start();


    }


    /**
     * Called when the Activity is paused
     */
    @Override
    public void onPause() {

        if(startThread != null) {
            if (startThread.isAlive()) {
                startThread.interrupt();
            }
        }

        cameraView.stop();
        super.onPause();

    }


}




























