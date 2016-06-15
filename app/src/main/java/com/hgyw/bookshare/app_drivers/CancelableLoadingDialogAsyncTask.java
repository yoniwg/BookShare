package com.hgyw.bookshare.app_drivers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccessIoException;

/**
 * Created by haim7 on 30/05/2016.
 */
public abstract class CancelableLoadingDialogAsyncTask<Params,Progress, Result> extends AsyncTask<Params,Progress,OptionalResult<Result,DataAccessIoException>>  {

    private final Context context;
    private final int dialogMessage;
    private ProgressDialog progressDialog;

    public CancelableLoadingDialogAsyncTask(Context context) {
        this(context, R.string.loading);
    }

    public CancelableLoadingDialogAsyncTask(Context context, @StringRes int updating_book_supplying) {
        this.context = context;
        this.dialogMessage = updating_book_supplying;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(context.getString(dialogMessage) + "...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.cancel),
                (dialog, which) -> { cancel(false); onCancel(); }
        );
        progressDialog.show();
    }

    @Override
    protected final OptionalResult<Result,DataAccessIoException> doInBackground(Params... params) {
        try {
            return OptionalResult.ofResult(retrieveDataAsync(params));
        } catch (DataAccessIoException e) {
            return OptionalResult.ofException(e);
        }
    }

    @Override
    protected final void onPostExecute(OptionalResult<Result,DataAccessIoException> result) {
        System.out.println("dialog is showing:" + progressDialog.isShowing() + " Window is active: " + progressDialog.getWindow().isActive());        boolean dialogIsShowing = progressDialog.isShowing();
        if (result.hasResult()) {
            if (dialogIsShowing) progressDialog.setCancelable(false);
            doByData(result.getResult());
            System.out.println("dialog is showing:" + progressDialog.isShowing() + " Window is active: " + progressDialog.getWindow().isActive());
            if (dialogIsShowing) progressDialog.dismiss();
        } else {
            if (dialogIsShowing) progressDialog.dismiss();
            onDataAccessIoException(result.getException());
        }
    }

    @MainThread
    protected void onDataAccessIoException(DataAccessIoException e) {
        String message = context.getString(R.string.connection_error) + ": \n" + e.getMessage();
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        onCancel();
    }

    @WorkerThread
    protected abstract Result retrieveDataAsync(Params... params);

    @MainThread
    protected abstract void doByData(Result o);

    /**
     * Invoked by network error, or in cancel by user immediately (in opposite of
     * {@link #onCancelled} method that's invoked at the end of {@link #doInBackground} method
     * execution). <br/>
     */
    protected abstract void onCancel();
}
