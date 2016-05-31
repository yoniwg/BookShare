package com.hgyw.bookshare.app_drivers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.view.View;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haim7 on 31/05/2016.
 */
public abstract class ListApplyObjectAdapter<T> extends ApplyObjectAdapter<T> {

    protected ListApplyObjectAdapter(Context context, @LayoutRes int itemLayoutId, List<T> itemsList) {
        super(context, itemLayoutId, itemsList);
    }

    final Map<View,AsyncTask<Void, Void, Object[]>> asyncTasks = new HashMap<>();

    @Override
    protected final void applyOnView(View view, int position) {
        view.setVisibility(View.INVISIBLE);
        AsyncTask<Void, Void, Object[]> asyncTask;
        asyncTask = asyncTasks.get(view);
        if (asyncTask != null) asyncTask.cancel(false);
        T item = getItem(position);
        asyncTask = new AsyncTask<Void, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(Void... params) {
                return ListApplyObjectAdapter.this.retrieveData(item);
            }

            @Override
            protected void onPostExecute(Object[] items) {
                if (isCancelled()) return;
                ListApplyObjectAdapter.this.applyDataOnView(view, item, items);
                view.setVisibility(View.VISIBLE);
            }
        };
        asyncTasks.put(view, asyncTask);
        asyncTask.execute();
    }

    @WorkerThread
    protected abstract Object[] retrieveData(T item);

    @MainThread
    protected abstract void applyDataOnView(View view, T item, Object[] items);
}