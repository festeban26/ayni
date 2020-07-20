package com.festeban26.ayni.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.facebook.model.FacebookUser;
import com.festeban26.ayni.fragments.LandingPageFragment;
import com.festeban26.ayni.interfaces.StartActivityListener;
import com.festeban26.ayni.utils.RequestCodes;
import com.festeban26.ayni.utils.ResultCodes;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, StartActivityListener {

    ImageView mUserProfilePictureInNavHeader;
    TextView mUsernameInNavHeader;
    TextView mToolbarTitle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RequestCodes.BOOK_TRIP) {
            if (resultCode == ResultCodes.SUCCESS) {
                startActivity(MyTrips.class);
            }
        } else if (requestCode == RequestCodes.POST_TRIP) {
            if (resultCode == ResultCodes.SUCCESS) {
                startActivity(MyTrips.class);
            }
        } else if (requestCode == RequestCodes.SIGN_IN) {
            if (resultCode == ResultCodes.SUCCESS) {
                updateUiForCurrentUser();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        // Hides app title from app toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.NavigationView_ActivityMain_Drawer);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        mUserProfilePictureInNavHeader = headerView.findViewById(R.id.ImageView_NavHeaderMain_UserProfilePicture);
        mUsernameInNavHeader = headerView.findViewById(R.id.TextView_NavHeaderMain_Username);

        LandingPageFragment landingPageFragment = new LandingPageFragment();
        openFragment(landingPageFragment);

        // When click on toolbar title, open default fragment
        mToolbarTitle = findViewById(R.id.TextView_ToolbarLayout_Title);

        mToolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new LandingPageFragment());
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView_HomeActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return MainActivity.this.onNavigationItemSelected(menuItem);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUiForCurrentUser();
    }

    private void updateUiForCurrentUser() {

        NavigationView navigationDrawer = findViewById(R.id.NavigationView_ActivityMain_Drawer);
        Menu menu = navigationDrawer.getMenu();
        MenuItem menuItem_loginLogout = menu.findItem(R.id.Navigation_Drawer_OpenCloseSession);

        // If user is logged in
        if (AppAuth.getInstance().isUserLoggedIn(getApplicationContext())) {

            FacebookUser currentFacebookUser = AppAuth.getInstance().getCurrentFacebookUser(getApplicationContext());

            // Update Navigation drawer data
            String welcomeMessage = "Welcome " + currentFacebookUser.getFirstName() + "!";
            mUsernameInNavHeader.setText(welcomeMessage);
            String imageUrl = currentFacebookUser.getUserImageUrl();

            if (!imageUrl.equals("default")) {
                Glide.with(MainActivity.this)
                        .load(imageUrl)
                        .into(mUserProfilePictureInNavHeader);
            }

            // Update menus
            menuItem_loginLogout.setTitle("Logout");

        }
        // If user is not logged in
        else {
            mUsernameInNavHeader.setText("Welcome to Ayni!");
            mUserProfilePictureInNavHeader.setImageDrawable(null);
            // TODO set a default background image
            // mUserProfilePictureInNavHeader.setImageResource(R.mipmap.ic_launcher);
            menuItem_loginLogout.setTitle("Login");
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.NavigationItem_MainDrawer_SearchRide || id == R.id.Nav_BottomNavigation_FindARide) {
            startActivity(SearchActivity.class);
            return true;
        } else if (id == R.id.NavigationItem_MainDrawer_PostATrip || id == R.id.Nav_BottomNavigation_PostATrip) {
            startActivity(PostActivity.class);
            return true;
        } else if (id == R.id.Navigation_Drawer_OpenCloseSession) {
            // If user is logged in
            if (AppAuth.getInstance().isUserLoggedIn(getApplicationContext())) {
                signOut();
                return true;
            } else {
                drawer.closeDrawer(GravityCompat.START);
                startSignInActivity(false);
                return true;
            }

        } else if (id == R.id.Navigation_Drawer_MyTrips || id == R.id.Nav_BottomNavigation_MyTrips) {
            startActivity(MyTrips.class);
            return true;
        }

        // TODO
        /*
        else if (id == R.id.Navigation_Drawer_Messages) {
            // If user is logged in
            if (AppAuth.getInstance().isUserLoggedIn(getApplicationContext())) {
                openFragment(new MessagesFragment());
                return true;
            }
            else {
                drawer.closeDrawer(GravityCompat.START);
                startSignInActivity(true);
                return false;
            }
        }
        else if (id == R.id.Navigation_Drawer_AboutUs) {
            // TODO
        }*/

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSignInActivity(boolean isSignInDueToContentRestriction) {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        Bundle extras = new Bundle();
        if (isSignInDueToContentRestriction)
            extras.putBoolean("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", true);
        else
            extras.putBoolean("SIGN_IN_DUE_TO_CONTENT_RESTRICTION", false);
        intent.putExtras(extras);
        startActivityForResult(intent, RequestCodes.SIGN_IN);
    }

    private void signOut() {
        AppAuth.getInstance().logout(getApplicationContext());
        updateUiForCurrentUser();
        openFragment(new LandingPageFragment());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method replace R.id.frameLayout_MainActivity with the fragment to open.
     * If the view to be replaced already contains a fragment of the same class, the view will
     * not be replaced.
     *
     * @param fragmentToOpen The fragment to open
     */
    private void openFragment(Fragment fragmentToOpen) {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout_MainActivity);
        if (currentFragment == null ||
                !Objects.equals(currentFragment.getClass().getCanonicalName(), fragmentToOpen.getClass().getCanonicalName())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout_MainActivity, fragmentToOpen)
                    // It is important to disable this line of code. It makes the main UI thread
                    // to skip frames since it has to animate the transition between a surface view
                    // and the next fragment content.
                    //.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit();
        }
    }

    @Override
    public void startActivity(Class activityClass) {

        int requestCode = 0;
        String activityClassCanonicalName = activityClass.getCanonicalName();

        if (activityClassCanonicalName != null) {
            if (activityClassCanonicalName.equalsIgnoreCase(SearchActivity.class.getCanonicalName()))
                requestCode = RequestCodes.BOOK_TRIP;
            else if (activityClassCanonicalName.equalsIgnoreCase(PostActivity.class.getCanonicalName()))
                requestCode = RequestCodes.POST_TRIP;
        }

        Intent intent = new Intent(MainActivity.this, activityClass);

        if (requestCode != 0)
            startActivityForResult(intent, requestCode);
        else
            startActivity(intent);

    }
}