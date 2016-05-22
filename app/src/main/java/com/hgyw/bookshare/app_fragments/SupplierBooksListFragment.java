package com.hgyw.bookshare.app_fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.List;

/**
 * Created by haim7 on 22/05/2016.
 */
public class SupplierBooksListFragment extends ListFragment implements TitleFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SupplierAccess sAccess = AccessManagerFactory.getInstance().getSupplierAccess();
        List<BookSupplier> bookSupplierList = sAccess.retrieveMyBooks();
        setListAdapter(new ApplyObjectAdapter<BookSupplier>(getActivity(), R.layout.supplier_book_list_item, bookSupplierList){
            protected void applyOnView(View view, int position) {
                BookSupplier bs = getItem(position);
                ObjectToViewAppliers.apply(view, bs);
                Book book = sAccess.retrieve(Book.class, bs.getBookId());
                ObjectToViewAppliers.apply(view, bs);
            }
        });
        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public int getFragmentTitle() {
        return R.string.my_books;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_action_ok, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {

            return true;
        } else return super.onOptionsItemSelected(item);
    }
}
