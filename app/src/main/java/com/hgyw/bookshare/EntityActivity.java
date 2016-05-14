package com.hgyw.bookshare;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.Supplier;

import java.util.HashMap;
import java.util.Map;

public class EntityActivity extends AppCompatActivity {

    private static final Map<Class<? extends Entity>, Class<? extends EntityFragment>> entityFragmentMap = new HashMap<>();
    static {
        entityFragmentMap.put(Book.class, BookFragment.class);
        entityFragmentMap.put(Supplier.class, SupplierFragment.class);
    }

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
        Bundle intentBundle = getIntent() == null ? null : getIntent().getExtras();
        Class entityType = intentBundle == null ? null : (Class) intentBundle.getSerializable(IntentsFactory.ARG_ENTITY_TYPE);
        Class<? extends EntityFragment> fragmentClass = entityFragmentMap.get(entityType);
        if (fragmentClass == null) {
            throw new IllegalArgumentException("No EntityFragment for " + entityType);
        }
        try {
            EntityFragment fragment = fragmentClass.newInstance();
            fragment.setArguments(intentBundle);
            this.setTitle(fragment.getTitleResource());
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_entity_container, fragment)
                    .commit();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            String message = "Cannot instantiate EntityFragment of " + fragmentClass.getSimpleName();
            throw new IllegalArgumentException(message, e);
        }
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
