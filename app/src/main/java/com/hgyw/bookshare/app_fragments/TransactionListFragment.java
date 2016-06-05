package com.hgyw.bookshare.app_fragments;

import android.app.ListFragment;
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
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 23/05/2016.
 */
public class TransactionListFragment extends ListFragment implements TitleFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders_supplier, container, false);
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
                setListAdapter(new ApplyObjectAdapter<Transaction>(getActivity(), R.layout.transaction_list_item, transactions) {
                    @Override
                    protected void applyOnView(View view, int position) {
                        Transaction transaction = getItem(position);
                        ObjectToViewAppliers.apply(view, transaction);
                    }
                });
            }
        }.execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Transaction transaction = (Transaction) l.getItemAtPosition(position);
        Intent intent = IntentsFactory.newEntityIntent(getActivity(), transaction);
        startActivity(intent);
    }

    @Override
    public int getFragmentTitle() {
        return R.string.transactions;
    }
}
