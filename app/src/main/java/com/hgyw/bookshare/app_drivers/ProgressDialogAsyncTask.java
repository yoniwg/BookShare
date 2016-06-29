package com.hgyw.bookshare.app_drivers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccessIoException;

/**
 * <p>Async task that shows progress-dialog when the task is executing.</p>
 * <p> This Async-task catch {@link DataAccessIoException}. after retrieving data the
 * {@link ProgressDialogAsyncTask#doByData}() will be called if retrieving data succeed.</p>
 * <p> If {@code DataAccessIoException} is being thrown then the task cancel, and
 * {@link ProgressDialogAsyncTask#onCancelled} called (if task have not been canceled).</p>
 * <p></p>The {@code ProgressDialogAsyncTask#onCancelled} called on cancel, and invoke the onDataAccessIoException()
 * method if DataAccessIoException was thrown, and the onCancel method.</p>
 */
public abstract class ProgressDialogAsyncTask<Result> extends AsyncTask<Void, Void, Result> {
    protected final android.content.Context context;
    private final int message;
    protected ProgressDialog progressDialog ;
    private boolean cancelable = false;

    private Optional<DataAccessIoException> exception = Optional.empty();


    public ProgressDialogAsyncTask(Context context, @StringRes int message) {
        this.context = context;
        this.message = message;
    }

    /**
     * Call the {@code ProgressDialogAsyncTask(Context context, int message)} with default message.
     */
    public ProgressDialogAsyncTask(Context context) {
        this(context, R.string.prossecing);
    }

    /**
     * Shows the dialog
     */
    @Override
    protected final void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(context.getString(message) + "...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        if (cancelable) {
            progressDialog.setOnCancelListener(d -> {});
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    context.getString(R.string.cancel),
                    (dialog, which) -> {
                        ProgressDialogAsyncTask.this.cancel(false);
                        // the current progressDialog  automatically dismissed by this button click
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setTitle(R.string.please_wait);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage(context.getString(R.string.canceling) + "...");
                        progressDialog.show();
                    }
            );
        }
        progressDialog.show();
    }

    /**
     * This method is final. use {@link ProgressDialogAsyncTask#retrieveDataAsync} to retrieve the data.<br>
     * If DataAccessIoException thrown, it catch the exception,  and set the getException() to it,
     * and cancel this async task.
     */
    @Override
    protected final Result doInBackground(Void... params) {
        try {
            return retrieveDataAsync();
        } catch (DataAccessIoException e) {
            exception = Optional.of(e);
            cancel(false);
            return null;
        }
    }

    /**
     * Call the {@link ProgressDialogAsyncTask#doByData}() if retrieving data succeed, or the
     * {@link ProgressDialogAsyncTask#onDataAccessIoException}() if {@code DataAccessIoException} was thrown.
     */
    @Override
    protected final void onPostExecute(Result result) {
        doByData(result);
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    /*
     * call to onDataAccessIoException() if needed, and to onCancel().
     * the dialog is closed after these calls.
     */
    @Override
    protected final void onCancelled() {
        if (exception.isPresent()) onDataAccessIoException(exception.get());
        onCancel();
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    /**
     * call the {@link ProgressDialogAsyncTask#onCancel()}
     * @param result
     */
    @Override
    protected final void onCancelled(Result result) {
        onCancelled();
    }

    /**
     * Retrieve data async. <br>
     * If method ends successfully then {@link ProgressDialogAsyncTask#doByData(Object)} will be called. <br>
     * DataAccessIoException is caught automatically, and the task canceled.
    */
    @WorkerThread
    protected abstract Result retrieveDataAsync();

    /**
     * Do by data async on main thread.
     * The progress-dialog dismisses after this method is called.
     */
    @MainThread
    protected void doByData(Result result) {}

    /**
     * Method called ny onCancelled when if getException() contains an exception.
     * It shows error in {@link Toast}.
     * The sub-classes can override this method.
     */
    protected void onDataAccessIoException(DataAccessIoException e) {
        String message = "Connection Error: \n" + e.getMessage();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is called by {@link ProgressDialogAsyncTask#onCancelled()} if the task is canceled.
     */
    protected void onCancel() {}

    /**
     * if cancelable==true then the dialog will have cancel button, that will cancel this async task.
     */
    void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    /**
     * Get optional of DataAccessIoException contains the exception thrown while retrieving data (if thrown).
     */
    public Optional<DataAccessIoException> getException() {
        return exception;
    }
}
