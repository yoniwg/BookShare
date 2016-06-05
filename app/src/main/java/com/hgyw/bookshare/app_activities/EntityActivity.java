package com.hgyw.bookshare.app_activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hgyw.bookshare.app_drivers.ListenerSupplier;
import com.hgyw.bookshare.app_drivers.ListenerSupplierHelper;
import com.hgyw.bookshare.app_fragments.EntityFragment;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.IdReference;

public class EntityActivity extends AppCompatActivity implements ListenerSupplier{


    private static final String ENTITY_FRAGMENT_TAG = "entityFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Get values from intent and instantiate the fragment accordingly.
            IdReference entityReference = IntentsFactory.idReferenceFrom(getIntent().getData());
            Class<? extends EntityFragment> fragmentClass = IntentsFactory.getEntityFragment(entityReference.getEntityType());
            EntityFragment fragment = EntityFragment.newInstance(fragmentClass, entityReference.getId());
            setTitle(fragment.getFragmentTitle());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_entity_container, fragment, ENTITY_FRAGMENT_TAG)
                    .commit();
        }
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
