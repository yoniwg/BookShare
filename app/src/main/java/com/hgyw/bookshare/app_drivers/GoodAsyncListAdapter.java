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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

import com.annimon.stream.function.Function;
import com.hgyw.bookshare.BuildConfig;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccessIoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by haim7 on 13/06/2016.
 */
public abstract class GoodAsyncListAdapter<T> extends BaseAdapter implements Filterable{

    private static final boolean DEBUG = false && BuildConfig.DEBUG;

    Filter filter;

    /**
     * Delegate to converter function from {@code <T>} to {@code String}
     * uses for the filter
     */
    Function<T, String> converterFunction;

    /**
     * Variable of the filter prefix in order to use in {@link #continueLoading()} method.
     */
    private CharSequence filterPrefix;

    private final Context context;
    private final LayoutInflater inflater;
    private final int itemLayoutId;
    private ListLoadingCallbacks loadingCallbacks;

    private int loadingAmount = 5;
    private static final int distanceFromLastForContinueLoading = 1;

    private List<T> retrievingItems;
    private List<T> items = new ArrayList<>();
    private List<T> originalItems = new ArrayList<>();
    private final Map<T ,Object[]> data = new HashMap<>();

    private final List<AsyncTask> tasks = new ArrayList<>();
    private int loadingEndPosition;
    private boolean isAllItemsLoaded = false;

    protected GoodAsyncListAdapter(Context context, @LayoutRes int itemLayoutId, ListLoadingCallbacks loadingCallbacks) {
        this.context = context;
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
        return data.get(items.get(position));
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
        int loadingEndPosition = Math.min(loadingStartPosition + loadingAmount, retrievingItems.size());
        if (loadingEndPosition <= this.loadingEndPosition) return;
        this.loadingEndPosition = loadingEndPosition;

        AsyncTask asyncTask = new AsyncTask<Void, Object[], List[]>() {
            @Override
            protected List[] doInBackground(Void... params) {
                List<T> newItems = new ArrayList<>(retrievingItems.subList(loadingStartPosition, loadingEndPosition));
                List<Object[]> newData = new ArrayList<>(newItems.size());
                if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Retrieve items of " + loadingStartPosition + " up to " + loadingEndPosition);
                for (T item : newItems) newData.add(retrieveData(item));
                return new List[] {newItems, newData};
            }

            @Override
            protected void onPostExecute(List[] lists) {
                synchronized (GoodAsyncListAdapter.this) {
                    if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Add items of " + loadingStartPosition + " up to " + loadingEndPosition);
                    List<T> itemsList = ((List<T>) lists[0]);
                    List<Object[]> dataList = ((List<Object[]>) lists[1]);
                    items.addAll(itemsList);
                    originalItems.addAll(itemsList);
                    for (int i = 0; i < itemsList.size(); i++) {
                        data.put(itemsList.get(i), dataList.get(i));
                    }
                    if (loadingEndPosition == retrievingItems.size()) {
                        isAllItemsLoaded = true;
                        if (loadingCallbacks != null) loadingCallbacks.onAllItemsLoaded();
                    }
                    if (filterPrefix != null){
                        filter.filter(filterPrefix);
                    }
                    if (loadingCallbacks != null) loadingCallbacks.onItemsLoaded(loadingStartPosition, loadingEndPosition);
                    notifyDataSetChanged();

                }
            }
        }.execute();
        tasks.add(asyncTask);
    }

    public void update(T item) {
        update(item, item);
    }

    public synchronized void update(T item, T newItem) {
        int position = retrievingItems.indexOf(item);

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
                    originalItems.set(position, newItem);
                    GoodAsyncListAdapter.this.data.put(newItem, data);
                    notifyDataSetChanged();
                }
            }
        }.execute();
        tasks.add(asyncTask);
    }

    public synchronized void remove(T item) {
        int position = retrievingItems.indexOf(item);
        items.remove(position);
        originalItems.remove(position);
        data.remove(item);
        notifyDataSetChanged();
    }

    public synchronized void refreshRetrieveList() {
        cancelTasks();
        if (DEBUG) System.out.println("GoodAsyncListAdapter: " + "Clear");
        items.clear();
        originalItems.clear();
        data.clear();
        isAllItemsLoaded = false;
        notifyDataSetChanged();
        loadingEndPosition = 0;
        if (loadingCallbacks != null) loadingCallbacks.onStartLoading();
        AsyncTask asyncTask = new AsyncTask<Void, Void, List<T>>() {
            DataAccessIoException exception;
            @Override
            protected List<T> doInBackground(Void... params) {
                try {
                    return retrieveList();
                } catch (DataAccessIoException e) {
                    exception = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<T> result) {
                if (result == null) {
                    if (loadingCallbacks != null) loadingCallbacks.onConnectionError(GoodAsyncListAdapter.this, exception);
                    return;
                }
                retrievingItems = result;
                if (!retrievingItems.isEmpty()) {
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

        void onConnectionError(GoodAsyncListAdapter<?> asyncTask, DataAccessIoException exception);
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
                //if (listFragment.isDetached()) return;
                listFragment.setListShown(false);
                listView.addFooterView(footerProgressView, null, false);
            }

            @Override
            public void onItemsLoaded(int startPosition, int endPosition) {
                //if (listFragment.isDetached()) return;
                if (startPosition == 0) listFragment.setListShown(true);
            }

            @Override
            public void onAllItemsLoaded() {
                //if (listFragment.isDetached()) return;
                listFragment.setListShown(true);
                listView.removeFooterView(footerProgressView);
            }

            @Override
            public void onConnectionError(GoodAsyncListAdapter<?> asyncTask, DataAccessIoException exception) {
                //if (listFragment.isDetached()) return;
                listFragment.setListShown(true);
                listFragment.setEmptyText(context.getString(R.string.connection_error));
                listView.removeFooterView(footerProgressView);
            }

        };
    }

    public void setFilterConverterFunction(Function<T, String> converterFunction){
        this.converterFunction = converterFunction;
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new StringFilter();
        }
        return filter;
    }

    /**
     * Simple filter using {@link #converterFunction} to convert Item to string.
     * If converterFunction is null it uses toString method.
     */
    private class StringFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            GoodAsyncListAdapter.this.filterPrefix = prefix;
            FilterResults results = new FilterResults();
            if (prefix == null || prefix.length() == 0) {
                List<T> list = new ArrayList<>(originalItems);
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<T> values  = new ArrayList<T>(originalItems);

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<T>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    String valueText;
                    if (converterFunction != null) {
                        valueText = converterFunction.apply(value).toLowerCase();
                    }else{
                        valueText = value.toString().toLowerCase();
                    }

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            items = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else if (!isAllItemsLoaded) {
                continueLoading();
            } else{
                notifyDataSetInvalidated();
            }

            //Dump stored filter prefix in case of no filter.
            if (results.count == originalItems.size()){
                filterPrefix = null;
            }
        }
    }
}
