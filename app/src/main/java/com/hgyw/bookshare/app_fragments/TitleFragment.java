package com.hgyw.bookshare.app_fragments;

import android.support.annotation.StringRes;

/**
 * Interface to force providing certain title to a fragment
 */
public interface TitleFragment {
    @StringRes int getFragmentTitle();
}
