package com.hgyw.bookshare.app_drivers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.view.View;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by haim7 on 31/05/2016.
 */
public abstract class ListApplyObjectAdapter<T> extends ApplyObjectAdapter<T> {

    private class SimpleBuffer<K, V>{

        private final int maxBufferSize;

        private final Map<K,V> buffer = new HashMap<>();
        private final Queue<K> bufferQueue = new LinkedList<>();

        public SimpleBuffer(int maxBufferSize ){
            this.maxBufferSize = maxBufferSize;
        }

        public void promoteKey(K key) {
            bufferQueue.remove(key);
            if (bufferQueue.size() > maxBufferSize) {
                bufferQueue.remove();
            }
            bufferQueue.add(key);
        }

        public boolean containsKey(K key) {
            return buffer.containsKey(key);
        }

        public V get(K key) {
            return buffer.get(key);
        }

        public void insert(K key, V value) {
            if (buffer.containsKey(key)) return;
            if (buffer.size() > maxBufferSize){
                buffer.remove(bufferQueue.peek());
            }
            buffer.put(key, value);
        }
    }


    final Map<View,AsyncTask<Void, Void, Object[]>> asyncTasks = new HashMap<>();
    final SimpleBuffer<T,Object[]> buffer = new SimpleBuffer<>(20);

    protected ListApplyObjectAdapter(Context context, @LayoutRes int itemLayoutId, List<T> itemsList) {
        super(context, itemLayoutId, itemsList);
    }

    @Override
    protected final void applyOnView(View view, int position) {
        T item = getItem(position);
        buffer.promoteKey(item);
        view.setVisibility(View.INVISIBLE);
        AsyncTask<Void, Void, Object[]> asyncTask;
        asyncTask = asyncTasks.get(view);
        if (asyncTask != null) asyncTask.cancel(false);
        if (buffer.containsKey(item)){
            ListApplyObjectAdapter.this.applyDataOnView(view, item, buffer.get(item));
            view.setVisibility(View.VISIBLE);
            return;
        }
        asyncTask = new AsyncTask<Void, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(Void... params) {
                return ListApplyObjectAdapter.this.retrieveDataForView(item);
            }

            @Override
            protected void onPostExecute(Object[] items) {
                buffer.insert(item, items);
                if (isCancelled()) return;
                ListApplyObjectAdapter.this.applyDataOnView(view, item, items);
                view.setVisibility(View.VISIBLE);
            }
        };
        asyncTasks.put(view, asyncTask);
        asyncTask.execute();
    }

    @WorkerThread
    protected abstract Object[] retrieveDataForView(T item);

    @MainThread
    protected abstract void applyDataOnView(View view, T item, Object[] data);
}