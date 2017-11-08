package com.android.contacts.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.utils.ScreenUtils;
import com.google.android.gms.R;

public class NoContentFragment extends Fragment {
    private LinearLayout mAllGroupLyout;
    private int mEmptyType = -1;
    private View mRootView;

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("empty_type", this.mEmptyType);
    }

    public NoContentFragment(int type) {
        this.mEmptyType = type;
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            this.mEmptyType = savedState.getInt("empty_type", -1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.mRootView = inflater.inflate(R.layout.no_content_view, null);
        this.mAllGroupLyout = (LinearLayout) this.mRootView.findViewById(R.id.all_empty_layout);
        if (CommonUtilMethods.calcIfNeedSplitScreen()) {
            Activity act = getActivity();
            if (act instanceof PeopleActivity) {
                ScreenUtils.adjustPaddingTop(act, (LinearLayout) this.mRootView.findViewById(R.id.no_content_main), true);
            }
        }
        setShowEmptyType(this.mEmptyType);
        return this.mRootView;
    }

    private void updateEmptyViewPosition() {
        Activity activity = getActivity();
        if (activity != null && this.mAllGroupLyout != null) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            if (this.mAllGroupLyout.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) this.mAllGroupLyout.getLayoutParams();
                if (isPor) {
                    params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor);
                    this.mAllGroupLyout.setLayoutParams(params);
                    return;
                }
                this.mAllGroupLyout.setPadding(0, 0, 0, CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor));
            }
        }
    }

    public int getShowEmptyType() {
        return this.mEmptyType;
    }

    public void setShowEmptyType(int type) {
        this.mEmptyType = type;
        if (type == 0) {
            if (this.mAllGroupLyout != null) {
                this.mAllGroupLyout.setVisibility(0);
                updateEmptyViewPosition();
            }
        } else if (this.mAllGroupLyout != null) {
            this.mAllGroupLyout.setVisibility(8);
        }
    }
}
