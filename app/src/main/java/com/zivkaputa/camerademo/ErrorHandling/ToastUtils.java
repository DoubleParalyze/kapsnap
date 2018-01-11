package com.zivkaputa.camerademo.ErrorHandling;

import android.content.Context;
import android.widget.Toast;

/**
 * ToastUtils
 *
 * Contains methods for conveniently creating Toast messages within an Activity
 */
public class ToastUtils {

    public static void shortToast( String message, Context context ){

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText( context, message, duration );
        toast.show();

    }

}
