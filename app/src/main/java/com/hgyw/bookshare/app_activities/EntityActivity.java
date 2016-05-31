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

    private EntityFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get values from intent and instantiate the fragment accordingly.
        // TODO what if fragment is saved?
        IdReference entityReference = IntentsFactory.idReferenceFrom(getIntent().getData());
        Class<? extends EntityFragment> fragmentClass = IntentsFactory.getEntityFragment(entityReference.getEntityType());
        fragment = EntityFragment.newInstance(fragmentClass, entityReference.getId());
        setTitle(fragment.getFragmentTitle());
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_entity_container, fragment)
                .commit();
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
        return ListenerSupplierHelper.tryGetListenerFromObjects(listenerClass, fragment);
    }

}
