package ru.ifsoft.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import ru.ifsoft.network.adapter.SectionsPagerAdapter;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.common.ActivityBase;
import ru.ifsoft.network.util.Helper;

public class MainActivity extends ActivityBase {

    Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    TabLayout mTabLayout;

    Boolean action = false;

    SectionsPagerAdapter adapter;

    int pageId = 0;
    Fragment fragment;
    int tab_position = 0;

    private View messenger_badge;

    String query = "";

    private Boolean restore = false;

    private ActivityResultLauncher<String> notificationsPermissionLauncher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.e("tester", "MainActivity onCreate");

        // ge intent

        Intent i = getIntent();

        tab_position = i.getIntExtra("pageId", 0);

        // Location. Send Location data (lat & lng) to server

            App.getInstance().setLocation();

        //

        if (savedInstanceState != null) {

            restore = savedInstanceState.getBoolean("restore");
            tab_position = savedInstanceState.getInt("tab_position");

        } else {

            restore = false;
            tab_position = 0;
        }

        //

        notificationsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            App.getInstance().getTooltipsSettings().setShowNotificationsPermissionRequst(false);
            App.getInstance().saveTooltipsSettings();

            if (isGranted) {

                // Permission is granted
                Log.e("Push Permission", "Permission is granted");

            } else {

                // Permission is denied

                Log.e("Push Permission", "denied");

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_notifications_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(MainActivity.this, getString(R.string.label_grant_notifications_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());
        mTabLayout.addTab(mTabLayout.newTab());

        initTabs();
        refreshBadges();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                tab_position = tab.getPosition();

                updateActiveTab(tab.getPosition());

                if (tab_position == 0) {

                    mAppBarLayout.setExpanded(true, true);

                } else {

                    mAppBarLayout.setExpanded(false, true);
                }

                displayFragment(tab_position, "");

                // animateTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                updateInactiveTab(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                // animateTab(tab.getPosition());
            }
        });

        if (!restore) {

            displayFragment(tab_position, "");
        }

        // Create a ConsentRequestParameters object.

        Helper helper = new Helper();
        helper.requestConsent(this, App.getInstance().isMobileAdsInitializeCalled.get());
    }

    private void displayFragment(int position, String title) {

        action = false;

        switch (position) {

            case 0: {

                fragment = new FeedFragment();
                action = true;

                break;
            }

            case 1: {

                fragment = new FriendsFragment();
                action = true;

                break;
            }

            case 2: {

                fragment = new ProfileFragment();
                action = true;

                break;
            }

            case 3: {

                fragment = new NotificationsFragment();
                action = true;

                break;
            }

            default: {

                fragment = new MenuFragment();
                action = true;

                break;
            }
        }

        if (action && fragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        }
    }

    private void animateTab(int tab_position) {

        ImageView tab_icon;

        tab_icon = (ImageView) mTabLayout.getTabAt(tab_position).getCustomView().findViewById(R.id.tab_icon);

        // rotate animation

        // RotateAnimation rotate = new RotateAnimation(0, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // rotate.setDuration(175);
        // rotate.setInterpolator(new LinearInterpolator());


        // Scale animation

        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());

        tab_icon.startAnimation(scale);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("restore", true);
        outState.putInt("tab_position", tab_position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTabs() {

        ImageView tab_icon;
        TextView tab_badge;
        RelativeLayout tab_layout;

        // Feed tab

        mTabLayout.getTabAt(0).setCustomView(R.layout.tab_layout);
        tab_icon = (ImageView) mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setImageResource(R.drawable.ic_feed);
        tab_badge = (TextView) mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.tab_badge);
        tab_badge.setText("");
        tab_badge.setVisibility(View.GONE);
        tab_layout = (RelativeLayout) mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.tab_main_layout);

        tab_layout.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    animateTab(0);
                }

                return false;
            }
        });

        // Groups tab

        mTabLayout.getTabAt(1).setCustomView(R.layout.tab_layout);
        tab_icon = (ImageView)mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setImageResource(R.drawable.ic_friends_tab_2);
        tab_badge = (TextView)mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.tab_badge);
        tab_badge.setText("");
        tab_badge.setVisibility(View.GONE);
        tab_layout = (RelativeLayout) mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.tab_main_layout);

        tab_layout.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    animateTab(1);
                }

                return false;
            }
        });


        // Profile tab

        mTabLayout.getTabAt(2).setCustomView(R.layout.tab_layout);
        tab_icon = (ImageView)mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setImageResource(R.drawable.ic_profile);
        tab_badge = (TextView)mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.tab_badge);
        tab_badge.setText("");
        tab_badge.setVisibility(View.GONE);
        tab_layout = (RelativeLayout) mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.tab_main_layout);

        tab_layout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    animateTab(2);
                }

                return false;
            }
        });

        // Notifications tab

        mTabLayout.getTabAt(3).setCustomView(R.layout.tab_layout);
        tab_icon = (ImageView)mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setImageResource(R.drawable.ic_notifications);
        tab_badge = (TextView)mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.tab_badge);
        tab_badge.setText("");
        tab_badge.setVisibility(View.GONE);
        tab_layout = (RelativeLayout) mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.tab_main_layout);

        tab_layout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    animateTab(3);
                }

                return false;
            }
        });

        // Menu tab

        mTabLayout.getTabAt(4).setCustomView(R.layout.tab_layout);
        tab_icon = (ImageView)mTabLayout.getTabAt(4).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setImageResource(R.drawable.ic_menu);
        tab_badge = (TextView)mTabLayout.getTabAt(4).getCustomView().findViewById(R.id.tab_badge);
        tab_badge.setText("");
        tab_badge.setVisibility(View.GONE);
        tab_layout = (RelativeLayout) mTabLayout.getTabAt(4).getCustomView().findViewById(R.id.tab_main_layout);

        tab_layout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){

                    animateTab(4);
                }

                return false;
            }
        });

        // Get selected tab

        updateActiveTab(tab_position);
    }

    private void updateActiveTab(int tab_position) {

        mTabLayout.setScrollPosition(tab_position, 0f, true);

        ImageView tab_icon;

        tab_icon = mTabLayout.getTabAt(tab_position).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setColorFilter(getResources().getColor(R.color.tabBarIconTintActive));

        switch (tab_position) {

            case 0: {

                tab_icon.setImageResource(R.drawable.ic_feed);

                break;
            }

            case 1: {

                tab_icon.setImageResource(R.drawable.ic_friends_tab_2);

                break;
            }

            case 2: {

                tab_icon.setImageResource(R.drawable.ic_profile);

                break;
            }

            case 3: {

                tab_icon.setImageResource(R.drawable.ic_notifications);

                break;
            }

            default: {

                tab_icon.setImageResource(R.drawable.ic_menu);

                break;
            }
        }
    }

    private void updateInactiveTab(int tab_position) {

        ImageView tab_icon;

        tab_icon = mTabLayout.getTabAt(tab_position).getCustomView().findViewById(R.id.tab_icon);
        tab_icon.setColorFilter(getResources().getColor(R.color.tabBarIconTint));

        switch (tab_position) {

            case 0: {

                tab_icon.setImageResource(R.drawable.ic_feed);

                break;
            }

            case 1: {

                tab_icon.setImageResource(R.drawable.ic_friends_tab_2);

                break;
            }

            case 2: {

                tab_icon.setImageResource(R.drawable.ic_profile);

                break;
            }

            case 3: {

                tab_icon.setImageResource(R.drawable.ic_notifications);

                break;
            }

            default: {

                tab_icon.setImageResource(R.drawable.ic_menu);

                break;
            }
        }
    }

    private void refreshBadges() {

        TextView tab_badge;

        // Friends tab

        tab_badge = mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.tab_badge);

        if (App.getInstance().getNewFriendsCount() > 0) {

            if (App.getInstance().getNewFriendsCount() > 9) {

                tab_badge.setText("9+");

            } else {

                tab_badge.setText(String.format(Locale.getDefault(), "%d", App.getInstance().getNewFriendsCount()));
            }

            tab_badge.setVisibility(View.VISIBLE);

        } else {

            tab_badge.setVisibility(View.GONE);
        }

        // Notifications tab

        tab_badge = mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.tab_badge);

        if (App.getInstance().getNotificationsCount() > 0) {


            if (App.getInstance().getNotificationsCount() > 9) {

                tab_badge.setText("9+");

            } else {

                tab_badge.setText(String.format(Locale.getDefault(), "%d", App.getInstance().getNotificationsCount()));
            }

            tab_badge.setVisibility(View.VISIBLE);

        } else {

            tab_badge.setText("");
            tab_badge.setVisibility(View.GONE);
        }

        // Menu tab

        tab_badge = mTabLayout.getTabAt(4).getCustomView().findViewById(R.id.tab_badge);

        if (App.getInstance().getGuestsCount() > 0 || App.getInstance().getNewFriendsCount() > 0) {

            tab_badge.setVisibility(View.VISIBLE);

        } else {

            tab_badge.setVisibility(View.GONE);
        }

        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_messenger);
        View actionView = MenuItemCompat.getActionView(menuItem);
        messenger_badge = actionView.findViewById(R.id.messenger_badge);

        if (messenger_badge != null) {

            if (App.getInstance().getMessagesCount() == 0) {

                messenger_badge.setVisibility(View.GONE);

            } else {

                messenger_badge.setVisibility(View.VISIBLE);

                String count_txt = App.getInstance().getMessagesCount() + "";

                if (App.getInstance().getMessagesCount() > 9) count_txt = "9+";

                ((TextView) messenger_badge.findViewById(R.id.counter)).setText(count_txt);
            }
        }

        actionView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                onOptionsItemSelected(menuItem);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                return true;
            }

            case R.id.action_messenger: {

                Intent i = new Intent(MainActivity.this, DialogsActivity.class);
                startActivity(i);

                return true;
            }

            case R.id.action_search: {

                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(i, 1001);

                return true;
            }

            case R.id.action_media: {

                Intent i = new Intent(MainActivity.this, MediaActivity.class);
                startActivity(i);

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    public void hideKeyboard() {

        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            View v = getCurrentFocus();

            if ( v instanceof EditText) {

                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {

                    v.clearFocus();

                     hideKeyboard();
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();

        refreshBadges();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            registerReceiver(mMessageReceiver, new IntentFilter(TAG_UPDATE_BADGES), RECEIVER_NOT_EXPORTED);

        } else {

            registerReceiver(mMessageReceiver, new IntentFilter(TAG_UPDATE_BADGES));
        }

        //

        Helper helper = new Helper(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            if (!helper.checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {

                if (App.getInstance().getTooltipsSettings().isAllowShowNotificationsPermissionRequest()) {

                    notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        unregisterReceiver(mMessageReceiver);
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            // String message = intent.getStringExtra("message");

            refreshBadges();
        }
    };
}
