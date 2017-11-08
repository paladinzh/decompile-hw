package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.app.AlertController.AlertParams.OnPrepareListViewListener;

public final class NotificationIntervalPickerActivity extends AlertActivity implements OnItemSelectedListener, OnClickListener, OnPrepareListViewListener {
    private int mClickedPos = -1;
    private ContentResolver mContentResolver;
    private OnClickListener mRingtoneClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NotificationIntervalPickerActivity.this.mClickedPos = which;
            System.putInt(NotificationIntervalPickerActivity.this.mContentResolver, "persistent_notification", which);
            NotificationIntervalPickerActivity.this.finish();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(2131629235);
        if (savedInstanceState != null) {
            this.mClickedPos = savedInstanceState.getInt("clicked_pos", -1);
        }
        this.mContentResolver = getContentResolver();
        AlertParams params = this.mAlertParams;
        params.mItems = getResources().getStringArray(2131362018);
        params.mOnClickListener = this.mRingtoneClickListener;
        params.mIsSingleChoice = true;
        params.mOnItemSelectedListener = this;
        params.mNegativeButtonText = getString(17039360);
        params.mNegativeButtonListener = this;
        params.mOnPrepareListViewListener = this;
        params.mTitle = getString(2131629236);
        setupAlert();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("clicked_pos", this.mClickedPos);
    }

    public void onPrepareListView(ListView aListView) {
        if (this.mClickedPos == -1) {
            this.mClickedPos = System.getInt(this.mContentResolver, "persistent_notification", 0);
        }
        this.mAlertParams.mCheckedItem = this.mClickedPos;
    }

    public void onClick(DialogInterface dialog, int which) {
        setResult(0);
        finish();
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        this.mClickedPos = position;
        System.putInt(this.mContentResolver, "persistent_notification", position);
        finish();
    }

    public void onNothingSelected(AdapterView parent) {
    }
}
