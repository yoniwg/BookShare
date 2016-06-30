package com.hgyw.bookshare.app_activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.utilities.IntentsFactory;
import com.hgyw.bookshare.app_drivers.utilities.ListenerSupplier;
import com.hgyw.bookshare.app_drivers.utilities.ListenerSupplierHelper;
import com.hgyw.bookshare.app_fragments.EntityFragment;
import com.hgyw.bookshare.entities.IdReference;

/**
 * A generic class for any entity activity.
 * The class gets the properly fragment by Entity object which was passed
 * by the Intent.
 */
public class EntityActivity extends AppCompatActivity implements ListenerSupplier{


    private static final String ENTITY_FRAGMENT_TAG = "entityFragment";
    private static final String KEY_SAVE_FRAGMENT = "saveFragment";
    private EntityFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);

        setContentView(R.layout.activity_entity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null) {
            fragment = (EntityFragment) getFragmentManager().getFragment(savedInstanceState, KEY_SAVE_FRAGMENT);
        }
        if (fragment == null){
            // Get values from intent and instantiate the fragment accordingly.
            IdReference entityReference = IntentsFactory.idReferenceFrom(getIntent().getData());
            Class<? extends EntityFragment> fragmentClass = IntentsFactory.getEntityFragment(entityReference.getEntityType());
            fragment = EntityFragment.newInstance(fragmentClass, entityReference.getId());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_entity_container, fragment, ENTITY_FRAGMENT_TAG)
                    .commit();
        }

        setTitle(fragment.getFragmentTitle());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, KEY_SAVE_FRAGMENT, fragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public <T> T tryGetListener(Class<T> listenerClass) {
        Object fragment = getFragmentManager().findFragmentByTag(ENTITY_FRAGMENT_TAG);
        return ListenerSupplierHelper.tryGetListenerFromObjects(listenerClass, fragment);
    }

}
