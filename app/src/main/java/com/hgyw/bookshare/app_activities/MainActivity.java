package com.hgyw.bookshare.app_activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.app_fragments.BooksListFragment;
import com.hgyw.bookshare.app_fragments.CartFragment;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.app_fragments.LoginDialogFragment;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String MAIN_FRAGMENT_TAG = "mainFragmentTag";
    private DrawerLayout drawer;
    final private AccessManager accessManager = AccessManagerFactory.getInstance();
    private Class fragmentClass;
    private static final Map<Class<? extends Fragment>, Integer> fragmentNavMap = new HashMap<>();
    static {
        fragmentNavMap.put(BooksListFragment.class, R.id.nav_books);
        fragmentNavMap.put(CartFragment.class, R.id.nav_cart);
        fragmentNavMap.put(CartFragment.class, R.id.nav_cart);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        onNewIntent(getIntent()); // updates the fragmentClass field

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Integer navItemId = fragmentNavMap.get(fragmentClass);
        navigationView.setCheckedItem(navItemId == null ? 0 : navItemId);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateDrawerOnLogin();
    }

    public void updateDrawerOnLogin(){

        User user = accessManager.getGeneralAccess().retrieveUserDetails();
        String userName = Utility.userNameToString(user);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        TextView navUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_user_name);
        ImageView navUserImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drwer_user_image);

        if (userName.trim().isEmpty()){
            navUserName.setText(R.string.anonymous);
        }else {
            navUserName.setText(userName);
        }

        long userImageId = user.getImageId();
        Utility.setImageById(navUserImage, userImageId, R.drawable.image_user);


        switch (accessManager.getCurrentUserType()) {
            case GUEST:
                navUserName.setText(R.string.guest);
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_cart).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_my_orders).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_user_details).setVisible(false);
                break;
            case CUSTOMER:
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_cart).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_my_orders).setVisible(true);
                break;
            case SUPPLIER:
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_cart).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_my_orders).setVisible(false);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        fragmentClass = newIntent == null ? null : (Class) newIntent.getSerializableExtra(IntentsFactory.ARG_FRAGMENT_CLASS);
        if (fragmentClass == null) {
            startActivity(IntentsFactory.homeIntent(this));
            return;
        }
        boolean refreshLogin = newIntent.getBooleanExtra(IntentsFactory.ARG_REFRESH_LOGIN, false);
        if (refreshLogin) updateDrawerOnLogin();
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(newIntent.getExtras());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, MAIN_FRAGMENT_TAG)
                    .commit();
            setIntent(newIntent);
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //ft.addToBackStack(null);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onBackPressed() {
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_logout:
                //show yes/no alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.logout_message)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            accessManager.signOut();
                            startActivity(IntentsFactory.homeIntent(this, true));
                            Toast.makeText(this,R.string.toast_loged_out, Toast.LENGTH_SHORT).show();
                        })
                        .setNeutralButton(R.string.no, (dialog, which) -> {
                        });
                builder.create().show();

                break;
            case R.id.nav_login:
                LoginDialogFragment.newInstance().show(getFragmentManager(), "LoginDialogFragment");

                break;
            case R.id.nav_books: {
                Intent intent = IntentsFactory.newBookListIntent(this, null);
                startActivity(intent);
                break;
            }
            case R.id.nav_cart: {
                Intent intent = IntentsFactory.newCartIntent(this);
                startActivity(intent);
                break;
            }
            case R.id.nav_my_orders:
                Intent intent = IntentsFactory.newOldOrderIntent(this);
                startActivity(intent);
                break;
            case R.id.nav_user_details:
                startActivity(IntentsFactory.userDetailsIntent(this));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
