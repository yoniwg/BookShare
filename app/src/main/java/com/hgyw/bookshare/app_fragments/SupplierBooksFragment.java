package com.hgyw.bookshare.app_fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ApplyTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Created by haim7 on 22/05/2016.
 */
public class SupplierBooksFragment extends ListFragment implements TitleFragment, BookSupplierDialogFragment.ResultListener {


    private ArrayAdapter<BookSupplier> adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SupplierAccess sAccess = AccessManagerFactory.getInstance().getSupplierAccess();
        List<BookSupplier> bookSupplierList = sAccess.retrieveMyBooks();
        setListAdapter(adapter = new ApplyObjectAdapter<BookSupplier>(getActivity(), R.layout.supplier_book_list_item, bookSupplierList){
            protected void applyOnView(View view, int position) {
                BookSupplier bs = getItem(position);
                ObjectToViewAppliers.apply(view, bs);
                Book book = sAccess.retrieve(Book.class, bs.getBookId());
                ObjectToViewAppliers.apply(view, book);
                //ApplyTask.toBiConsumer(book -> sAccess.retrieve(Book.class, bs.getBookId()), ObjectToViewAppliers::apply, view).executeAsync(bs);

            }
        });
        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public int getFragmentTitle() {
        return R.string.my_books;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        BookSupplier bs = (BookSupplier) l.getItemAtPosition(position);
        BookSupplierDialogFragment.newInstance(bs).show(getFragmentManager(), "BookSupplierDialogFragment");
    }

    @Override
    public void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier) {
        SupplierAccess sAccess = result == ResultCode.CANCEL ? null : AccessManagerFactory.getInstance().getSupplierAccess();
        switch (result) {
            case OK:
                sAccess.updateBookSupplier(bookSupplier);
                Utility.replaceById(adapter, bookSupplier);
                break;
            case DELETE:
                sAccess = AccessManagerFactory.getInstance().getSupplierAccess();
                sAccess.removeBookSupplier(bookSupplier);
                adapter.remove(bookSupplier);
                break;
            case CANCEL: break;
        }
    }

}
