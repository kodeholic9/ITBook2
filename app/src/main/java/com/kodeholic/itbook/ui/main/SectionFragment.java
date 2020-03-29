package com.kodeholic.itbook.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.IBase;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class SectionFragment extends Fragment implements SectionsPagerAdapter.TabListener {

    protected Context mContext;
    protected Handler mHandler;
    protected SectionsPagerAdapter.TabInfo mSectionInfo;
    protected IBase mBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "onCreate()");

        mContext = getContext();
        mHandler = new Handler(Looper.getMainLooper());
        if (getActivity() instanceof IBase) {
            mBase = (IBase)getActivity();
        }
    }

    public void setSectionInfo(SectionsPagerAdapter.TabInfo sectionInfo) {
        mSectionInfo = sectionInfo;
        mSectionInfo.listener = this;
    }

    protected void showLoading(String f) {
        Log.d(this.getClass().getSimpleName(), "showLoading() - f: " + f + ", mBase: " + mBase);
        if (mBase != null) {
            mBase.showLoading();
        }
    }

    protected void hideLoading(String f) {
        Log.d(this.getClass().getSimpleName(), "hideLoading() - f: " + f + ", mBase: " + mBase);
        if (mBase != null) {
            mBase.hideLoading();
        }
    }
}