package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.app.ProgressDialog;
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

    private final Activity activity;
    private final int dialogMessage;
    private ProgressDialog progressDialog;

    public CancelableLoadingDialogAsyncTask(Activity activity) {
        this(activity, R.string.loading);
    }

    public CancelableLoadingDialogAsyncTask(Activity activity, @StringRes int updating_book_supplying) {
        this.activity = activity;
        this.dialogMessage = updating_book_supplying;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(activity.getString(dialogMessage) + "...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog ->  onCancel());
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
        System.out.println("dialogIsShowing: " + progressDialog.isShowing());
        boolean dialogIsShowing = progressDialog.isShowing();
        if (result.hasResult()) {
            if (dialogIsShowing) progressDialog.setCancelable(false);
            doByData(result.getResult());
            System.out.println("dialogIsShowing: " + progressDialog.isShowing());
            if (dialogIsShowing) progressDialog.dismiss();
        } else {
            if (dialogIsShowing) progressDialog.dismiss();
            onDataAccessIoException(result.getException());
        }
    }

    @MainThread
    protected void onDataAccessIoException(DataAccessIoException e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        onCancelled();
    }

    @WorkerThread
    protected abstract Result retrieveDataAsync(Params... params);

    @MainThread
    protected abstract void doByData(Result o);

    protected abstract void onCancel();
}
