package com.zivkaputa.camerademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zivkaputa.camerademo.R;
import com.zivkaputa.camerademo.UserModel.MediaMessage;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ziv on 4/14/17.
 */

/**
 * PreviewManager
 *
 * Contains methods for creating and displaying Views that display images and videos
 */
public class PreviewManager implements TextureView.SurfaceTextureListener {

    private View previewView;
    private Context context;
    private String videoPath;
    private MediaPlayer mMediaPlayer;
    private MediaMessage.MediaType curType;

    /**
     * PreviewManager constructor
     *
     * @param media - The media to be embedded in the view
     * @param context - The context the view will be embedded in
     */
    public PreviewManager( Object media, Context context ) {


        LayoutInflater inflater = LayoutInflater.from(context);

        this.context = context;

        // Inflate and setup the correct type of view based on the given media
        if ( media instanceof Bitmap ) {

            curType = MediaMessage.MediaType.PHOTO;
            ImageView newImageView = (ImageView) inflater.inflate(R.layout.image_preview, null);
            newImageView.setImageBitmap( (Bitmap) media );
            previewView = newImageView;

        } else if ( media instanceof File ) {

            curType = MediaMessage.MediaType.VIDEO;
            TextureView newTextureView = (TextureView) inflater.inflate(R.layout.video_preview, null);
            previewView = newTextureView;
            initTextureView( (File) media );


        } else {

            previewView = null;

        }

    }


    /**
     *  Cleans up the current media if before it is removed
     */
    public void clean(){

        // Release the media player if it exists
        if ( mMediaPlayer != null ) {

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

        }

    }


    /**
     * Retrieves the preview object
     */
    public View getPreview(){

        return previewView;

    }


    /**
     * Getter for the current media type
     * @return The type
     */
    public MediaMessage.MediaType getCurType() {
        return curType;
    }


    /**
     * Initializes the texture view for displaying video
     *
     * @param video - The video to be played
     */
    private void initTextureView( File video ) {


        videoPath = video.getAbsolutePath();
        TextureView textureView = (TextureView) previewView;
        ( (TextureView) previewView ).setSurfaceTextureListener( this );


    }


    /**
     * Sets the video of the texture view
     *
     * Code primarily from Stack Overflow:
     * http://stackoverflow.com/questions/19429811/android-videoview-crop-center
     */
    public void setVideo() {

        TextureView textureView = (TextureView) previewView;
        Surface surface = new Surface( textureView.getSurfaceTexture() );

        String TAG = context.getString( R.string.texture_tag );
        try {

            // Setup and scale the media player
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource( videoPath );
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            scaleVideo( mMediaPlayer, textureView );

            // Play video when the media is ready to play
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    mediaPlayer.start();

                }
            });


        } catch (IllegalArgumentException e) {

            Log.d( TAG, e.getMessage() );

        } catch (SecurityException e) {

            Log.d( TAG, e.getMessage() );

        } catch (IllegalStateException e) {

            Log.d( TAG, e.getMessage() );

        } catch (IOException e) {

            Log.d( TAG, e.getMessage() );

        }
    }


    /**
     * Required for SurfaceTextureListener
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {

        setVideo();

    }


    /**
     * Required for SurfaceTextureListener
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {

    }


    /**
     * Required for SurfaceTextureListener
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

        return true;

    }


    /**
     * Required for SurfaceTextureListener
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    /**
     * Scales the media player for appropriate playback
     *
     * Code primarily from Stack Overflow:
     * http://stackoverflow.com/questions/19429811/android-videoview-crop-center
     *
     * @param mPlayer
     * @param textureView
     */
    private void scaleVideo(MediaPlayer mPlayer, TextureView textureView) {


        // Get layout parameters and display information from the current context
        ViewGroup.LayoutParams videoParams = textureView.getLayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        ( (Activity) context ).getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        final int height = dm.heightPixels;
        final int width = dm.widthPixels;
        int videoHeight = mPlayer.getVideoHeight();
        int videoWidth = mPlayer.getVideoWidth();

        double hRatio = 1;
        hRatio = (height * 1.0 / videoHeight) / (width * 1.0 / videoWidth);

        videoParams.width = (int) (hRatio <= 1 ? 0 : Math.round((-(hRatio - 1) / 2)
                * width));

        videoParams.height = (int) (hRatio >= 1 ? 0 : Math
                .round((((-1 / hRatio) + 1) / 2) * height));

        videoParams.width = width - videoParams.width - videoParams.width;
        videoParams.height = height - videoParams.height - videoParams.height;

        textureView.setScaleX(1.00001f);//<-- this line enables smoothing of the picture in TextureView.

        textureView.requestLayout();
        textureView.invalidate();

    }


}
