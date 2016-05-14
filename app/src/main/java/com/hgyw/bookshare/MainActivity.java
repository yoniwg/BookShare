package com.hgyw.bookshare;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;

import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private AccessManager accessManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        accessManager = AccessManagerFactory.getInstance();

        boolean isGuest = accessManager.getCurrentUserType() == UserType.GUEST;
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(isGuest);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(!isGuest);

        if (getIntent() != null) {
            //onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        Class fragmentClass = (Class) newIntent.getSerializableExtra(IntentsFactory.ARG_FRAGMENT_CLASS);
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(newIntent.getExtras());

            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
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
            case R.id.action_search:
                BookQueryDialogFragment.newInstance(new BookQuery())
                        .show(getFragmentManager(), "bookQueryDialog");
                return true;
            case R.id.action_buy:
                List<Order> oList = accessManager.getCustomerAccess().getCart().retrieveCartContent();
                Toast.makeText(this, "You are going to buy:" + oList.toString(),Toast.LENGTH_SHORT).show();
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
                accessManager.signOut();
                ((TextView) this.findViewById(R.id.drawer_user_name)).setText(R.string.guest);
                ((ImageView) this.findViewById(R.id.drwer_user_image)).setImageResource(R.drawable.image_people);
                ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_login).setVisible(true);
                ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_logout).setVisible(false);
                break;
            case R.id.nav_login:
                CredentialsDialogFragment.newInstance().show(getFragmentManager(), "CredentialsDialogFragment");

                break;
            case R.id.nav_gallery: {
                Intent intent = IntentsFactory.newBookListIntent(this, null);
                startActivity(intent);
                break;
            }
            case R.id.nav_slideshow: {
                Intent intent = IntentsFactory.newCartIntent(this);
                startActivity(intent);
                break;
            }
            case R.id.nav_my_orders:

                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
