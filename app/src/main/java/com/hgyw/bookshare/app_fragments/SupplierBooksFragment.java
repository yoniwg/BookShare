package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.List;

/**
 * Created by haim7 on 22/05/2016.
 */
public class SupplierBooksFragment extends ListFragment implements TitleFragment, BookSupplierDialogFragment.ResultListener {


    private ArrayAdapter<BookSupplier> adapter;

    private Activity activity;
    private final SupplierAccess sAccess = AccessManagerFactory.getInstance().getSupplierAccess();

    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        new AsyncTask<Void, Void, List<BookSupplier>>() {
            SupplierAccess sAccess = AccessManagerFactory.getInstance().getSupplierAccess();

            @Override
            protected List<BookSupplier> doInBackground(Void... params) {
                return sAccess.retrieveMyBooks();
            }

            @Override
            protected void onPostExecute(List<BookSupplier> bookSuppliers) {
                setListAdapter(adapter = new ListApplyObjectAdapter<BookSupplier>(activity, R.layout.supplier_book_list_item, bookSuppliers){
                    @Override
                    protected Object[] retrieveDataForView(BookSupplier bs) {
                        Book book = sAccess.retrieve(Book.class, bs.getBookId());
                        ImageEntity bookImage = (book.getImageId() == 0) ?
                                null : sAccess.retrieve(ImageEntity.class,book.getImageId());
                        return new Object[] {book, bookImage };
                    }

                    @Override
                    protected void applyDataOnView(View view, BookSupplier bs, Object[] data) {
                        ObjectToViewAppliers.apply(view, bs);
                        ObjectToViewAppliers.apply(view, (Book) data[0], false);
                        ObjectToViewAppliers.apply(view, (ImageEntity) data[1]);
                    }
                });
            }
        }.execute();

        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public int getFragmentTitle() {
        return R.string.my_books;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //get chosen book-supplier
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BookSupplier bookSupplier = adapter.getItem(info.position);
        //add menu items
        MenuItem editMenuItem = menu.add(R.string.edit);
        MenuItem deleteMenuItem = menu.add(R.string.remove_from_my_books);

        //set edit listener
        editMenuItem.setOnMenuItemClickListener(item -> {
            BookSupplierDialogFragment.newInstance(bookSupplier).show(getFragmentManager(), "BookSupplierDialogFragment");
            return true;
        });

        //set delete listener
        deleteMenuItem.setOnMenuItemClickListener(item -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.remove_from_my_books_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        new ProgressDialogAsyncTask<Void, Void, Void>(activity) {
                            @Override protected Void retrieveDataAsync(Void... params) {
                                sAccess.removeBookSupplier(bookSupplier); return null;
                            }
                            @Override protected void doByData(Void aVoid) {
                                adapter.remove(bookSupplier);
                                Toast.makeText(activity,R.string.book_was_removed_to_supplier, Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    })
                    .setNeutralButton(R.string.no, (dialog, which) -> {
                    });
            builder.create().show();
            return true;
        });
    }

    @Override
    public void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier) {
        switch (result) {
            case OK:
                new ProgressDialogAsyncTask<Void, Void, Void>(activity) {
                    @Override protected Void retrieveDataAsync(Void... params) {
                        sAccess.updateBookSupplier(bookSupplier); return null;
                    }
                    @Override protected void doByData(Void aVoid) {
                        adapter.notifyDataSetChanged();
                    }
                }.execute(); break;
            case CANCEL: break;
        }
    }

}
