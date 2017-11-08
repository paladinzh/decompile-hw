package com.android.dialer.voicemail;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;

public class ActiveVVM4CMView {
    private static final String TAG = ActiveVVM4CMView.class.getSimpleName();
    private Button mButton;
    private Activity mContext;
    private LinearLayout mEmptyView;
    private boolean mIsEmpty;
    private boolean mIsSourceAvalible;
    private boolean mIsUser;
    private SharedPreferences mPref;
    private Fragment mTargetFragment;
    private TextView mTextView;
    private String mTodoActivedId;

    public ActiveVVM4CMView(Activity activity, Fragment fragment) {
        this.mContext = activity;
        this.mTargetFragment = fragment;
        this.mPref = SharePreferenceUtil.getDefaultSp_de(activity);
    }

    public void initViews(View views, OnClickListener listener) {
        this.mTextView = (TextView) views.findViewById(R.id.vm_description);
        this.mButton = (Button) views.findViewById(R.id.vm_free_activation);
        this.mButton.setOnClickListener(listener);
        this.mEmptyView = (LinearLayout) views.findViewById(R.id.ll_empty_text_parent_view);
    }

    public void onCallFetched(Cursor cursor) {
        boolean z = true;
        if (!(cursor == null || cursor.getCount() == 0)) {
            z = false;
        }
        this.mIsEmpty = z;
        HwLog.d(TAG, "onCallFetched,mIsEmpty : " + this.mIsEmpty);
    }

    public void onVoicemailStatusFetched(boolean sourcesAvailable, String todoActivedId) {
        HwLog.d(TAG, "onVoicemailStatusFetched,mIsSourceAvalible: " + sourcesAvailable);
        this.mIsSourceAvalible = sourcesAvailable;
        this.mTodoActivedId = todoActivedId;
    }

    public void onAllDataFetched(int mCurrentCallTypeFilter, boolean dialpadVisible) {
        if (mCurrentCallTypeFilter != 4) {
            HwLog.d(TAG, "onAllDataFetched, mCurrentCallTypeFilter : " + mCurrentCallTypeFilter);
            setVisible(8);
            boolean showGuide = false;
            if (this.mTargetFragment instanceof CallLogFragment) {
                showGuide = ((CallLogFragment) this.mTargetFragment).isShowSearchGuide();
            }
            if (!this.mIsEmpty || r1) {
                setEmptyVisible(8);
                return;
            }
            setEmptyVisible(0);
            setEmptyViewLocation(dialpadVisible);
        } else if (!this.mIsEmpty) {
            setVisible(8);
            setEmptyVisible(8);
            ActiveDialog dialog = ActiveDialog.get(this.mTargetFragment.getFragmentManager());
            HwLog.d(TAG, "onAllDataFetched,dialog : " + dialog);
            if (this.mIsUser && this.mTodoActivedId != null && dialog == null) {
                this.mIsUser = false;
                if (!ActiveDialog.isNotNow(this.mPref, this.mTodoActivedId)) {
                    ActiveDialog.show(this.mTodoActivedId, this.mTargetFragment.getFragmentManager(), this.mTargetFragment);
                }
            }
        } else if (this.mIsSourceAvalible || CommonConstants.IS_VVM_FILTER_ON) {
            setVisible(8);
            setEmptyVisible(0);
            setEmptyViewLocation(dialpadVisible);
            HwLog.d(TAG, "onAllDataFetched, show Empty view");
        } else {
            setVisible(0);
            setEmptyVisible(8);
            HwLog.d(TAG, "onAllDataFetched, show Activation view");
        }
    }

    private void setEmptyViewLocation(boolean dialpadVisible) {
        boolean isPor = true;
        if (this.mContext.getResources().getConfiguration().orientation != 1) {
            isPor = false;
        }
        if (isPor) {
            this.mEmptyView.setPadding(0, getMarginTopPixel(this.mContext, dialpadVisible, isPor), 0, 0);
            return;
        }
        this.mEmptyView.setGravity(17);
        this.mEmptyView.setPadding(0, 0, 0, CommonUtilMethods.getActionBarAndStatusHeight(this.mContext, isPor));
    }

    private void setEmptyVisible(int visibility) {
        if (this.mEmptyView == null) {
            HwLog.d(TAG, "onAllDataFetched mEmptyView == null,return");
        } else {
            this.mEmptyView.setVisibility(visibility);
        }
    }

    private void setVisible(int visible) {
        if (this.mTextView == null || this.mButton == null) {
            HwLog.d(TAG, "setVisible (mTextView == null || mButton == null),return");
            return;
        }
        this.mTextView.setVisibility(visible);
        this.mButton.setVisibility(visible);
    }

    public void setVmDescriptionLocation(Activity activity, boolean dialpadVisible, boolean isPor) {
        if (this.mTextView != null && activity != null && isPor) {
            MarginLayoutParams params = (MarginLayoutParams) this.mTextView.getLayoutParams();
            params.topMargin = getMarginTopPixel(activity, dialpadVisible, isPor);
            this.mTextView.setLayoutParams(params);
        }
    }

    private int getMarginTopPixel(Activity activity, boolean dialpadVisible, boolean isPor) {
        if (dialpadVisible) {
            return activity.getResources().getDimensionPixelSize(R.dimen.call_log_empty_icon_dialpad_visible_margin_top);
        }
        return CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
    }

    public boolean onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        if (aResultCode == -1) {
            switch (aRequestCode) {
                case 1:
                    if (this.mTargetFragment instanceof CallLogFragment) {
                        ((CallLogFragment) this.mTargetFragment).refreshCallLogWithCurrentFilter();
                    }
                    return true;
            }
        }
        return false;
    }

    public void setFromUser(boolean isUser) {
        this.mIsUser = isUser;
    }
}
