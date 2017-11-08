package com.android.contacts;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.hap.camcard.bcr.CCardScanHandler;

public class QuickPressCamcardFragment extends Fragment {
    private Activity mActivity;
    private CCardScanHandler mCcardHandlr;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
        if (this.mActivity != null) {
            if (savedInstanceState != null) {
                this.mCcardHandlr = CCardScanHandler.onRestoreInstance(savedInstanceState);
            } else {
                this.mCcardHandlr = new CCardScanHandler();
                this.mCcardHandlr.recognizeCapture(this.mActivity, this);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mCcardHandlr != null) {
            this.mCcardHandlr.onSaveInstance(outState);
        }
        super.onSaveInstanceState(outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mCcardHandlr != null) {
            this.mCcardHandlr.handlePhotoActivityResult(requestCode, resultCode, data, this.mActivity, this);
        }
        if (this.mActivity != null && resultCode == 0 && requestCode != 301) {
            this.mActivity.finish();
        }
    }
}
