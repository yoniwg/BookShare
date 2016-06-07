package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.DateRangeBar;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 23/05/2016.
 */
public class TransactionListFragment extends ListFragment implements TitleFragment {

    private Activity activity;
    ApplyObjectAdapter<Transaction> adapter;

    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content_with_date_range, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DateRangeBar dateRangeBar = (DateRangeBar) view.findViewById(R.id.dateRangeBar);
        dateRangeBar.setDateRangeListener(this::updateListAdapter);
        updateListAdapter(dateRangeBar);
    }

    private void updateListAdapter(DateRangeBar dateRangeBar) {
        Date dateFrom = dateRangeBar.getDateFrom();
        Date dateTo = dateRangeBar.getDateTo();
        new AsyncTask<Void, Void, List<Transaction>>() {
            @Override
            protected List<Transaction> doInBackground(Void... params) {
                CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
                return cAccess.retrieveTransactions(dateFrom, dateTo);
            }

            @Override
            protected void onPostExecute(List<Transaction> transactions) {
                adapter = new ListApplyObjectAdapter<Transaction>(activity, R.layout.transaction_list_item, transactions) {
                    GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                    @Override
                    protected Object[] retrieveDataForView(Transaction item) {
                        Object[] data = new Object[2];
                        data[0] = access.calcTotalPriceOfTransaction(item);
                        data[1] = access.getSuppliersOfTransaction(item);
                        return data;
                    }

                    @Override
                    protected void applyDataOnView(View view, Transaction item, Object[] data) {
                        ObjectToViewAppliers.apply(view, item, (BigDecimal) data[0], (List<User>) data[1]);
                    }
                };
                setListAdapter(adapter);
            }
        }.execute();
        setEmptyText(getString(R.string.no_items_list_view));

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Transaction transaction = (Transaction) l.getItemAtPosition(position);
        Intent intent = IntentsFactory.newEntityIntent(activity, transaction);
        startActivity(intent);
    }

    @Override
    public int getFragmentTitle() {
        return R.string.transactions;
    }
}
