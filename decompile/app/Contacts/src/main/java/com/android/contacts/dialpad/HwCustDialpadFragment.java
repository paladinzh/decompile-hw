package com.android.contacts.dialpad;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class HwCustDialpadFragment {
    protected static final String NO_DISPLAY_STRING = "";
    protected static final String TAG = "DialpadFragment";
    Context mContext;
    DialpadFragment mParent;

    public HwCustDialpadFragment(Context context, DialpadFragment parent) {
        this.mContext = context;
        this.mParent = parent;
    }

    public Boolean isDisableCustomService() {
        return Boolean.valueOf(false);
    }

    public String getPredefinedSpeedDialNumbersByMccmnc(String singlePair) {
        return singlePair;
    }

    public void showEmergentView(TextView mEmergentDialText, String text) {
    }

    public void startListenPhoneState() {
    }

    public void stopListenPhoneState() {
    }

    public boolean isRCMCertificate() {
        return false;
    }

    public Boolean isFilterText() {
        return Boolean.valueOf(false);
    }

    public String getShowText(String text) {
        return text;
    }

    public String stringToShowForRCMCert(Resources res) {
        return NO_DISPLAY_STRING;
    }

    public void showStateName(TextView mShowDialpadLocation, String number) {
    }

    public boolean isSupportCallIntercept() {
        return false;
    }

    public boolean getCallInterceptIntent(String number) {
        return false;
    }

    public void checkAndUpdatePhoneType(TextView aTextView, String phoneType, String lookUp) {
    }

    public boolean predefinedHeaderNotNeeded() {
        return false;
    }

    public String changeToTwEmergencyNum(String number, int subId) {
        return number;
    }

    public boolean isCustSdlEyNuber(String aNumber) {
        return false;
    }

    public void repalceAdditionalButtonRowForEncryptCall(LayoutInflater inflater, ViewGroup dialpadView, OnClickListener dialButtonEncrypt1Listener, OnClickListener dialButtonEncrypt2Listener) {
    }

    public void setButtonsLayoutForOneSimWithEncryptCall(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout cardButtonLayout, int which, boolean isLandscape, boolean isInLeftOrRightState) {
    }

    public void setButtonsLayoutForBothSimWithEncryptCall(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout card1ButtonLayout, LinearLayout card2ButtonLayout, boolean isLandscape, boolean isInLeftOrRightState) {
    }

    public void hideEncryptCallButton() {
    }

    public int updateSearchBtnsPaddingStart(int paddingStart) {
        return paddingStart;
    }

    public int updateSearchBtnsPaddingEnd(int paddingEnd) {
        return paddingEnd;
    }

    public void initExtremeSimplicityMode(ViewSwitcher aViewSwitcher, LayoutInflater inflater, LinearLayout addDialBtnSwitcherLayout) {
    }

    public void setSwitcherBtnState(OnClickListener dialBtnEncryptListener, OnTouchListener switcherTouchListener, OnLongClickListener switcherLongClickListener, boolean isLandscape, boolean isInLeftOrRightState, boolean isSim1Enabled, boolean isSim2Enabled) {
    }

    public void setSwitcherBtnLayout(boolean isSim1Enabled, boolean isSim2Enabled, LinearLayout card1ButtonLayout, LinearLayout card2ButtonLayout, boolean isLandscape, boolean isInLeftOrRightState) {
    }

    public void setViewSwitcherPressed() {
    }

    public void showSwitcherNext(Animation InAnimation, Animation OutAnimation, OnClickListener dialBtnEncryptListener, boolean isLandscape, boolean isInLeftOrRightState) {
    }

    public void showSwitcherPrevious(Animation InAnimation, Animation OutAnimation, OnClickListener dialBtnEncryptListener, boolean isLandscape, boolean isInLeftOrRightState) {
    }

    public void hideSwitchButton() {
    }

    public void setEncryptBtnBgNormal() {
    }

    public void setEncryptBtnBgNormal(int slotId) {
    }

    public void getEncryptButtonRecommended(Bundle savedState) {
    }

    public void putEncryptButtonRecommended(Bundle outState) {
    }

    public void setEncryptButtonBgByRecommended() {
    }

    public boolean setEncryptButtonBackgroundChoosed(int resultid) {
        return false;
    }

    public boolean isEncryptBtnUpdateRecommend(String dialString) {
        return false;
    }

    public boolean checkAndInitCall(Context aContext, String number) {
        return false;
    }

    public void checkAndAddHeaderView(View aView, LayoutInflater inflater) {
    }

    public void checkAndShowMarqueeOnResume(Context aContext) {
    }

    public void removeMarqueeMessageOnPause() {
    }

    public void setContentObserver(Context aContext) {
    }

    public void removeContentObserver(Context aContext) {
    }

    public void registerDialerCallBack(DialpadCallBack aDialerCallBack) {
    }

    public void unregisterDialerCallBack() {
    }
}
