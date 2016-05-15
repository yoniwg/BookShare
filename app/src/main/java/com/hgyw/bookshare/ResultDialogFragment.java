package com.hgyw.bookshare;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentResultListener} interface
 * to handle interaction events.
 */
public class ResultDialogFragment<ResultData> extends DialogFragment {

    private OnFragmentResultListener<ResultData> mListener;

    private void onResult(ResultData resultData) {
        if (mListener != null) {
            mListener.onFragmentResult(resultData);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentResultListener) {
            mListener = (OnFragmentResultListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentResultListener<ResultData> {
        void onFragmentResult(ResultData resultData);
    }
}
