package com.hgyw.bookshare.app_drivers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccessIoException;

/**
 * Created by haim7 on 30/05/2016.
 */
public abstract class ProgressDialogAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, OptionalResult<Result, DataAccessIoException>> {
    protected final android.content.Context context;
    private final int message;
    protected ProgressDialog progressDialog ;

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    private DialogInterface.OnCancelListener onCancelListener = null;

    public ProgressDialogAsyncTask(Context context, @StringRes int message) {
        this.context = context;
        this.message = message;
    }

    public ProgressDialogAsyncTask(Context context) {
        this(context, R.string.prossecing);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(context.getString(message) + "...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(onCancelListener != null);
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.show();
    }

    @Override
    protected final OptionalResult<Result, DataAccessIoException> doInBackground(Params... params) {
        try {
            return OptionalResult.ofResult(retrieveDataAsync(params));
        } catch (DataAccessIoException e) {
            return OptionalResult.ofException(e);
        }
    }


    /**
    * DataAccessIoException is caught automatically, and message will be displayed to user.
    */
    protected abstract Result retrieveDataAsync(Params... params);

    /**
     * The progress-dialog dismisses at this method is called.
     */
    protected void doByData(Result result) {}

    @Override
    protected final void onPostExecute(OptionalResult<Result,DataAccessIoException> result) {
        if (!result.hasResult()) {
            onDataAccessIoException(result.getException());
            return;
        }
        doByData(result.getResult());
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    protected void onDataAccessIoException(DataAccessIoException e) {
        String message = "Connection Error: \n" + e.getMessage();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();    }
}
