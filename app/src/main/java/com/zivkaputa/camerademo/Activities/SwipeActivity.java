package com.zivkaputa.camerademo.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.zivkaputa.camerademo.Activities.SwipeFragments.CameraFragment;
import com.zivkaputa.camerademo.Activities.SwipeFragments.FriendListFragment;
import com.zivkaputa.camerademo.Activities.SwipeFragments.MessageListFragment;
import com.zivkaputa.camerademo.R;

/**
 * Activity that allows user to swipe between camera, friends, and messages view
 */
public class SwipeActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_swipe_layout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        viewPager.setCurrentItem( 1 );


    }


    /**
     * Overridden onBackPressed method - called when user presses Android back button
     */
    @Override
    public void onBackPressed() {
        // Do nothing - The user should not go back to the welcome page
    }


    /**
     * MyPagerAdapter
     *
     * Class used to fill the swipe view with the correct fragment
     */
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MessageListFragment();
                case 1:
                    return new CameraFragment();
                case 2:
                    return new FriendListFragment();
                default:
                    return null;


            }
        }


    }
}
