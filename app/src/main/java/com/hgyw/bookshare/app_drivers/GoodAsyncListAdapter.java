package com.hgyw.bookshare.app_drivers;

import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hgyw.bookshare.BuildConfig;
import com.hgyw.bookshare.R;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by haim7 on 13/06/2016.
 */
public abstract class GoodAsyncListAdapter<T> extends BaseAdapter {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final LayoutInflater inflater;
    private final int itemLayoutId;
    private ListLoadingCallbacks loadingCallbacks;

    private int loadingAmount = 5;
    private static final int distanceFromLastForContinueLoading = 1;

    private List<T> retievingItems;
    private List<T> items = new ArrayList<>();
    private final List<Object[]> datas = new ArrayList<>();

    private final List<AsyncTask> tasks = new ArrayList<>();
    private int loadingEndPosition;

    protected GoodAsyncListAdapter(Context context, @LayoutRes int itemLayoutId, ListLoadingCallbacks loadingCallbacks) {
        this.inflater = LayoutInflater.from(context);
        this.itemLayoutId = itemLayoutId;
        setLoadingCallbacks(loadingCallbacks);
        refreshRetrieveList();
    }

    public GoodAsyncListAdapter(Context context, @LayoutRes int itemLayoutId, ListFragment listFragment) {
        this(context, itemLayoutId, listFragmentCallbacks(context, listFragment));
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Object[] getData(int position) {
        return datas.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // continue loading data on last view positions
        int distanceFromLast = (getCount() - 1) - position;
        if (distanceFromLast <= distanceFromLastForContinueLoading) {
            continueLoading();
            if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Call continueLoading() from position " + position);
        }

        // set view
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(itemLayoutId, parent, false);
        }
        applyDataOnView(getItem(position), getData(position), view);
        return view;
    }

    private synchronized void continueLoading() {
        int loadingStartPosition = this.loadingEndPosition;
        int loadingEndPosition = Math.min(loadingStartPosition + loadingAmount, retievingItems.size());
        if (loadingEndPosition <= this.loadingEndPosition) return;
        this.loadingEndPosition = loadingEndPosition;

        AsyncTask asyncTask = new AsyncTask<Void, Object[], List[]>() {
            @Override
            protected List[] doInBackground(Void... params) {
                List<T> newItems = new ArrayList<>(retievingItems.subList(loadingStartPosition, loadingEndPosition));
                List<Object[]> newDatas = new ArrayList<>(newItems.size());
                if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Retrieve items of " + loadingStartPosition + " up to " + loadingEndPosition);
                for (T item : newItems) newDatas.add(retrieveData(item));
                return new List[] {newItems, newDatas};
            }

            @Override
            protected void onPostExecute(List[] lists) {
                synchronized (GoodAsyncListAdapter.this) {
                    if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Add items of " + loadingStartPosition + " up to " + loadingEndPosition);
                    items.addAll(lists[0]);
                    datas.addAll(lists[1]);
                    notifyDataSetChanged();
                    if (loadingCallbacks != null) loadingCallbacks.onItemsLoaded(loadingStartPosition, loadingEndPosition);
                    if (loadingEndPosition == retievingItems.size()) {
                        if (loadingCallbacks != null) loadingCallbacks.onAllItemsLoaded();
                    }
                }
            }
        }.execute();
        tasks.add(asyncTask);
    }

    public void update(T item) {
        update(item, item);
    }

    public synchronized void update(T item, T newItem) {
        int position = retievingItems.indexOf(item);

        AsyncTask asyncTask = new AsyncTask<Void, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(Void... params) {
                Object[] data = retrieveData(getItem(position));
                return data;
            }

            @Override
            protected void onPostExecute(Object[] data) {
                synchronized (GoodAsyncListAdapter.this) {
                    items.set(position, newItem);
                    datas.set(position, data);
                    notifyDataSetChanged();
                }
            }
        }.execute();
        tasks.add(asyncTask);
    }

    public synchronized void remove(T item) {
        int position = retievingItems.indexOf(item);
        items.remove(position);
        datas.remove(position);
        notifyDataSetChanged();
    }

    public synchronized void refreshRetrieveList() { // TODO: doesn't work properly on second call
        cancelTasks();
        if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Clear");
        items.clear();
        datas.clear();
        notifyDataSetChanged();
        loadingEndPosition = 0;
        if (loadingCallbacks != null) loadingCallbacks.onStartLoading();
        AsyncTask asyncTask = new AsyncTask<Void, Void, List<T>>() {
            @Override
            protected List<T> doInBackground(Void... params) {
                return retrieveList();
            }

            @Override
            protected void onPostExecute(List<T> result) {
                retievingItems = result;
                if (!retievingItems.isEmpty()) {
                    continueLoading();
                } else {
                    if (loadingCallbacks != null) loadingCallbacks.onAllItemsLoaded();
                }
            }
        }.execute();
        tasks.add(asyncTask);
    }

    private void cancelTasks() {
        for (ListIterator<AsyncTask> it = tasks.listIterator(); it.hasNext();) {
            if (it.next().getStatus() == AsyncTask.Status.FINISHED) it.remove();
        }
        for (AsyncTask task : tasks) task.cancel(false);
    }

    /**
     * Call this at the end of fragment to avoid actions on nothing
     */
    public synchronized void cancel() {
        cancelTasks();
    }


    //////////////////////////
    // The abstract methods
    //////////////////////////

    /** The method get(int) of list it also called in WorkerThread only.
     *
     * @return
     */
    @WorkerThread
    public abstract List<T> retrieveList();

    @WorkerThread
    public abstract Object[] retrieveData(T retrievedItem);

    @MainThread
    public abstract void applyDataOnView(T retrievedItem, Object[] data, View view);


    /////////////
    // Setters
    /////////////

    private void setLoadingCallbacks(ListLoadingCallbacks loadingCallbacks) {
        this.loadingCallbacks = loadingCallbacks;
        if (loadingCallbacks != null) loadingCallbacks.onAttach(this);
    }

    public void setLoadingAmount(int loadingAmount) {
        if (loadingAmount <= 0) throw new IllegalArgumentException("The loading amount should be large than 0. Value accepted: " + loadingAmount);
        this.loadingAmount = loadingAmount;
    }

    //////////////////////////////////
    // ListLoadingCallbacks
    //////////////////////////////////

    public interface ListLoadingCallbacks {
        void onAttach(GoodAsyncListAdapter adapter);

        void onStartLoading();

        void onItemsLoaded(int startPosition, int endPosition);

        void onAllItemsLoaded();
    }


    /**
     * This set the adapter for list fragment. don't do it manually.
     * @param context
     * @param listFragment
     * @return
     */
    private static ListLoadingCallbacks listFragmentCallbacks(Context context, ListFragment listFragment) {
        return new ListLoadingCallbacks() {
            ListView listView = listFragment.getListView();
            View footerProgressView = LayoutInflater.from(context).inflate(R.layout.progress_item, listView, false);

            @Override
            public void onAttach(GoodAsyncListAdapter adapter) {
                if (listFragment.isDetached()) return;
                listFragment.setListAdapter(adapter);
                listView.setVerticalScrollBarEnabled(false);
            }

            @Override
            public void onStartLoading() {
                if (listFragment.isDetached()) return;
                listFragment.setListShown(false);
                listView.addFooterView(footerProgressView);
            }

            @Override
            public void onItemsLoaded(int startPosition, int endPosition) {
                if (listFragment.isDetached()) return;
                if (startPosition == 0) listFragment.setListShown(true);
            }

            @Override
            public void onAllItemsLoaded() {
                if (listFragment.isDetached()) return;
                listFragment.setListShown(true);
                listView.removeFooterView(footerProgressView);
            }

        };
    }

}
