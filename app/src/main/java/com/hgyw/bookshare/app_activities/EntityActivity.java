package com.hgyw.bookshare.app_activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hgyw.bookshare.app_fragments.BookFragment;
import com.hgyw.bookshare.app_fragments.EntityFragment;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_fragments.SupplierFragment;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;

import java.util.HashMap;
import java.util.Map;

public class EntityActivity extends AppCompatActivity {

    public static final String ENTITY_FRAGMENT_TAG = "entityFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get values from intent and instantiate the fragment accordingly.
        IdReference entityReference = IntentsFactory.idReferenceFrom(getIntent().getData());
        Class<? extends EntityFragment> fragmentClass = IntentsFactory.getEntityFragment(entityReference.getEntityType());
        EntityFragment fragment = EntityFragment.newInstance(fragmentClass, entityReference.getId());
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_entity_container, fragment, ENTITY_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
