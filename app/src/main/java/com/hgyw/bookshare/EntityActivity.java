package com.hgyw.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.hgyw.bookshare.entities.Entity;

public class EntityActivity extends AppCompatActivity {

    public static final String ARG_ENTITY_ID = "id";
    public static final String ARG_ENTITY_TYPE = "entityType";

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


        // get values from intent bundle
        Bundle intentBundle = getIntent().getExtras();
        long entityId = intentBundle.getLong(ARG_ENTITY_ID, 0);
        Class entityType = (Class) intentBundle.getSerializable(ARG_ENTITY_TYPE);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_entity_container, EntityFragment.newInstance(entityType, entityId))
                .commit();
    }

    public static void startNewActivity(Activity invokingActivity, Class<? extends Entity> entityType, long entityId) {
        Intent intent = new Intent(invokingActivity, EntityActivity.class);
        intent.putExtra(EntityActivity.ARG_ENTITY_TYPE, entityType);
        intent.putExtra(EntityActivity.ARG_ENTITY_ID, entityId);
        invokingActivity.startActivity(intent);
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
