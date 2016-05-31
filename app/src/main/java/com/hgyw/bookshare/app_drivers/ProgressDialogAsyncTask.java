package com.hgyw.bookshare.app_drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccessIoException;

/**
 * Created by haim7 on 30/05/2016.
 */
public abstract class ProgressDialogAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, ProgressDialogAsyncTask.OptionalResult<Result>> {
    private AlertDialog progressDialog;
    protected final android.content.Context context;
    private final String message;

    public ProgressDialogAsyncTask(Context context, String message) {
        this.context = context;
        this.message = message;
    }

    public ProgressDialogAsyncTask(Context context, @StringRes int message) {
        this(context, context.getString(message));
    }

    public ProgressDialogAsyncTask(Context context) {
        this(context, R.string.opening);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog.Builder(context)
                .setTitle(R.string.please_wait)
                .setMessage(message + "...")
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    protected static class OptionalResult<Result> {
        final Result result;
        final DataAccessIoException dataAccessIoException;

        private OptionalResult(Result result) {
            this.result = result;
            this.dataAccessIoException = null;
        }
        private OptionalResult(DataAccessIoException exception) {
            this.result = null;
            this.dataAccessIoException = exception;
        }

    }

    @Override
    protected final OptionalResult<Result> doInBackground(Params... params) {
        try {
            return new OptionalResult<>(doInBackground1(params));
        } catch (DataAccessIoException e) {
            return new OptionalResult<Result>(e);
        }
    }


    /**
    * DataAccessIoException is caught automatically, and message will be displayed to user.
    */
    protected abstract Result doInBackground1(Params... params);
    /**
     * The progress-dialog dismisses before this method is called.
     */
    protected void onPostExecute1(Result result) {}

    @Override
    protected final void onPostExecute(OptionalResult<Result> result) {
        progressDialog.dismiss();
        if (result.dataAccessIoException != null) {
            DataAccessIoException e = result.dataAccessIoException;
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        onPostExecute1(result.result);
    }
}
