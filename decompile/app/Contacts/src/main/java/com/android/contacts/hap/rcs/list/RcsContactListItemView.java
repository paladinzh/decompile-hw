package com.android.contacts.hap.rcs.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ContactListItemView.PhotoPosition;
import com.google.android.gms.R;

public class RcsContactListItemView {
    private CheckBox mCheckBox;
    private Context mContext;
    private ImageView mRCSView;
    private int mRCSViewWidth = 45;
    private int mRcsWithSimCardGap = 35;

    public RcsContactListItemView(Context context) {
        this.mContext = context;
    }

    public ImageView getRCSView(ViewGroup view) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRCSView == null) {
            this.mRCSView = new ImageView(this.mContext, null, 16843439);
            this.mRCSView.setBackground(null);
            view.addView(this.mRCSView);
        }
        return this.mRCSView;
    }

    public ImageView getRCSView() {
        return this.mRCSView;
    }

    public int[] layoutRCSFeatureIcon(int rightBound, int leftBound, int textTopBound, int accIndicatorWidth, int mAccIndicatorGapFromEnd, boolean isCheckBoxVisible, boolean isLandSpace, int mCheckWidth, int mNameTextViewHeight, boolean mIsSimAccountIndDisplayEnabled, ImageView mSimAccountIndicator, PhotoPosition mPhotoPosition, int mGapBetweenAccIcons, int iPaddingLeft) {
        View rcsView = this.mRCSView;
        int accIndicatorWidthRCS = this.mRCSViewWidth;
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            int accRightIndPos;
            int accLeftIndPos;
            if (!(this.mContext.getResources().getBoolean(R.bool.show_account_icons) || rcsView == null || this.mRCSView.getVisibility() != 0)) {
                if (mPhotoPosition == PhotoPosition.LEFT) {
                    accRightIndPos = rightBound - mAccIndicatorGapFromEnd;
                    if (isCheckBoxVisible) {
                        accRightIndPos -= mCheckWidth;
                    }
                    accLeftIndPos = accRightIndPos - accIndicatorWidthRCS;
                    if (isLandSpace && isCheckBoxVisible) {
                        accLeftIndPos -= mCheckWidth;
                    }
                    if (!isCheckBoxVisible) {
                        rightBound -= accIndicatorWidthRCS + mGapBetweenAccIcons;
                    }
                } else {
                    accLeftIndPos = leftBound + mAccIndicatorGapFromEnd;
                    if (isVisible(this.mCheckBox)) {
                        accLeftIndPos += mCheckWidth;
                    }
                    accRightIndPos = accLeftIndPos + accIndicatorWidthRCS;
                    if (!isCheckBoxVisible) {
                        leftBound += accIndicatorWidthRCS + mGapBetweenAccIcons;
                    }
                    if (isLandSpace && isCheckBoxVisible) {
                        accRightIndPos += mCheckWidth;
                    }
                }
                rcsView.layout(accLeftIndPos, textTopBound, accRightIndPos, textTopBound + mNameTextViewHeight);
                rcsView.bringToFront();
            }
            if (mIsSimAccountIndDisplayEnabled && mSimAccountIndicator != null && mSimAccountIndicator.getVisibility() == 0) {
                accIndicatorWidth = mSimAccountIndicator.getMeasuredWidth();
                if (mPhotoPosition == PhotoPosition.LEFT) {
                    accRightIndPos = rightBound - mAccIndicatorGapFromEnd;
                    if (isCheckBoxVisible) {
                        accRightIndPos -= mCheckWidth;
                    }
                    accLeftIndPos = accRightIndPos - accIndicatorWidth;
                    if (isLandSpace && isCheckBoxVisible) {
                        accLeftIndPos -= mCheckWidth;
                    }
                    if (!isCheckBoxVisible) {
                        rightBound -= accIndicatorWidth + mGapBetweenAccIcons;
                    }
                } else {
                    accLeftIndPos = leftBound + mAccIndicatorGapFromEnd;
                    if (isVisible(this.mCheckBox)) {
                        accLeftIndPos += mCheckWidth;
                    }
                    accRightIndPos = accLeftIndPos + accIndicatorWidth;
                    if (!isVisible(this.mCheckBox)) {
                        leftBound += accIndicatorWidth + mGapBetweenAccIcons;
                    }
                    if (isLandSpace && isCheckBoxVisible) {
                        accRightIndPos += mCheckWidth;
                    }
                    if (isVisible(rcsView)) {
                        accRightIndPos += accIndicatorWidthRCS - this.mRcsWithSimCardGap;
                        if (!isCheckBoxVisible) {
                            leftBound += accIndicatorWidthRCS;
                        }
                    }
                }
                mSimAccountIndicator.layout(accLeftIndPos, textTopBound, accRightIndPos, textTopBound + mNameTextViewHeight);
                mSimAccountIndicator.bringToFront();
            }
        }
        return new int[]{rightBound, leftBound, accIndicatorWidth};
    }

    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == 0;
    }

    public void setCheckBox(CheckBox checkBox) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mCheckBox = checkBox;
        }
    }

    public int getRcsViewWidth() {
        return this.mRCSViewWidth;
    }
}
