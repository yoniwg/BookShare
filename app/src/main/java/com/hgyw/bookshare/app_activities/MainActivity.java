package com.hgyw.bookshare.app_activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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

import com.hgyw.bookshare.app_drivers.*;
import com.hgyw.bookshare.app_fragments.BooksFragment;
import com.hgyw.bookshare.app_fragments.CartFragment;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_fragments.LoginDialogFragment;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_fragments.TitleFragment;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListenerSupplier {

    public static final String MAIN_FRAGMENT_TAG = "mainFragmentTag";
    private DrawerLayout drawer;
    private AccessManager accessManager;
    private Class fragmentClass;
    private static final Map<Class<? extends Fragment>, Integer> fragmentNavMap = new HashMap<>();
    static {
        fragmentNavMap.put(BooksFragment.class, R.id.nav_books);
        fragmentNavMap.put(CartFragment.class, R.id.nav_cart);
        // TODO more...
    }

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessManager = AccessManagerFactory.getInstance();
        Credentials savedCredentials = Utility.loadCredentials(this);
        try {
            if (!savedCredentials.getPassword().isEmpty()) {
                accessManager.signIn(savedCredentials);
            }
        } catch (WrongLoginException ignored) {}

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
                navigationView.getMenu().setGroupVisible(R.id.nav_general_options, true);
                navigationView.getMenu().setGroupVisible(R.id.nav_logged_out_management, true);
                break;
            case CUSTOMER:
                navigationView.getMenu().setGroupVisible(R.id.nav_customer_options, true);
                navigationView.getMenu().setGroupVisible(R.id.nav_logged_in_management, true);
                break;
            case SUPPLIER:
                navigationView.getMenu().setGroupVisible(R.id.nav_supplier_options, true);
                navigationView.getMenu().setGroupVisible(R.id.nav_logged_in_management, true);
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
        if (refreshLogin) {
            updateDrawerOnLogin();
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(newIntent.getExtras());
            if (fragment instanceof TitleFragment) {
                setTitle(((TitleFragment) fragment).getFragmentTitle());
            } else {
                setTitle(fragmentClass.getSimpleName());
            }
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
        //if (item.isChecked()) return false;

        switch (item.getItemId()) {
            case R.id.nav_logout:
                //show yes/no alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.logout_message)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            accessManager.signOut();
                            // delete credentials password
                            Credentials savedCredentials = Utility.loadCredentials(this);
                            Utility.saveCredentials(this, new Credentials(savedCredentials.getUsername(), ""));
                            // go home activity
                            startActivity(IntentsFactory.homeIntent(this, true));
                            Toast.makeText(this,R.string.toast_loged_out, Toast.LENGTH_SHORT).show();
                        })
                        .setNeutralButton(R.string.no, (dialog, which) -> {
                        });
                builder.show();

                break;
            case R.id.nav_login:
                LoginDialogFragment.newInstance(Utility.loadCredentials(this))
                        .show(getFragmentManager(), "LoginDialogFragment");

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
            case R.id.nav_transactions:
                startActivity(IntentsFactory.transactionsIntent(this));
                break;
            case R.id.nav_customer_orders:
                Intent intent = IntentsFactory.newOldOrderIntent(this);
                startActivity(intent);
                break;
            case R.id.nav_supplier_orders:
                if (accessManager.getCurrentUserType() != UserType.SUPPLIER) {
                    Toast.makeText(this, "You are not supplier.", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(IntentsFactory.supplierOrdersIntent(this));
                }
                break;
            case R.id.nav_suppliers_books:
                startActivity(IntentsFactory.supplierBooksIntent(this));
                break;
            case R.id.nav_user_details:
                startActivity(IntentsFactory.userDetailsIntent(this));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public <T> T tryGetListener(Class<T> listenerClass) {
        return ListenerSupplierHelper.tryGetListenerFromObjects(listenerClass, fragment);
    }
}
