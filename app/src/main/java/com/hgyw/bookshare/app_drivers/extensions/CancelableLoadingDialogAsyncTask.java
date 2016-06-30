package com.hgyw.bookshare.app_drivers.extensions;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.StringRes;

import com.hgyw.bookshare.R;

/**
 * <p>Async task that shows dialog as class {@link ProgressDialogAsyncTask}, but with cancel button on the dialog. </p>
 * <p>In addition, this call provide </p>
 */
public abstract class CancelableLoadingDialogAsyncTask<Result> extends ProgressDialogAsyncTask<Result>  {

    private ProgressDialog progressDialog;

    /**
     * Call the {@code CancelableLoadingDialogAsyncTask(Context context, int message)} with default message.
     */
    public CancelableLoadingDialogAsyncTask(Context context) {
        this(context, R.string.loading);
    }

    /**
     * @param context the context
     * @param message String-resource for message within the dialog like 'Loading' (without '...')
     */
    public CancelableLoadingDialogAsyncTask(Context context, @StringRes int message) {
        super(context, message);
        super.setCancelable(true);
    }

    /**
     * This method is called by {@link ProgressDialogAsyncTask#onCancelled()} if the task is canceled.
     */
    protected abstract void onCancel();

    @Override
    public void setCancelable(boolean cancelable) {
        throw new UnsupportedOperationException("This class always cancelable.");
    }
}
