package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        BookSupplier bs = (BookSupplier) l.getItemAtPosition(position);
        BookSupplierDialogFragment.newInstance(bs).show(getFragmentManager(), "BookSupplierDialogFragment");
    }

    @Override
    public void onBookSupplierResult(ResultCode result, BookSupplier bookSupplier) {
        final SupplierAccess sAccess = (result == ResultCode.CANCEL) ? null : AccessManagerFactory.getInstance().getSupplierAccess();
        switch (result) {
            case OK:
                new ProgressDialogAsyncTask<Void, Void, Void>(activity) {
                    @Override protected Void doInBackground1(Void... params) {
                        sAccess.updateBookSupplier(bookSupplier); return null;
                    }
                    @Override protected void onPostExecute1(Void aVoid) {
                        Utility.replaceById(adapter, bookSupplier);
                    }
                }.execute(); break;
            case DELETE:
                new ProgressDialogAsyncTask<Void, Void, Void>(activity) {
                    @Override protected Void doInBackground1(Void... params) {
                        sAccess.removeBookSupplier(bookSupplier); return null;
                    }
                    @Override protected void onPostExecute1(Void aVoid) {
                        adapter.remove(bookSupplier);
                    }
                }.execute(); break;
            case CANCEL: break;
        }
    }

}
