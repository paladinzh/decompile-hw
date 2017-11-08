package com.android.contacts.dialpad;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.DialpadHeaderLayout;
import com.google.android.gms.R;
import java.util.Stack;

public class DialpadFragmentHelper extends Handler {
    private Activity mActivity = null;
    private View mCachedCallLogListHeaderView = null;
    private DialpadHeaderLayout mCachedDialpadHeaderLayout = null;
    private Context mContext = null;
    private final Object mDialpadLock = new Object();
    private View mDiapadView = null;
    private HandlerThread mHandlerThread = null;
    private LayoutInflater mInflater = null;
    private Stack<View> mNewViews = null;

    public static DialpadFragmentHelper createDialpadFragmentHelper(Context context) {
        HandlerThread handlerThread = new HandlerThread("DialpadFragmentHelper");
        handlerThread.start();
        return new DialpadFragmentHelper(handlerThread, context);
    }

    private DialpadFragmentHelper(HandlerThread handlerThread, Context context) {
        super(handlerThread.getLooper());
        this.mContext = new ContextThemeWrapper(context.getApplicationContext(), context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        this.mContext.getTheme().applyStyle(R.style.PeopleTheme, true);
        this.mHandlerThread = handlerThread;
    }

    private void initLayoutInflater() {
        synchronized (this.mDialpadLock) {
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(this.mContext);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                initDialpadLayout();
                initDialpadHeaderLayout();
                initCallLogListHeaderView();
                return;
            case 17:
                inflateNewView();
                return;
            case 4369:
                this.mHandlerThread.quitSafely();
                this.mHandlerThread = null;
                return;
            default:
                return;
        }
    }

    public void startInitDialpadFragmentInBackground(Activity activity) {
        this.mActivity = activity;
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            sendEmptyMessage(1);
        }
    }

    private void initCallLogListHeaderView() {
        initLayoutInflater();
        this.mCachedCallLogListHeaderView = this.mInflater.inflate(R.layout.contacts_radio_button_group_divider, null);
    }

    public View getCallLogListHeaderView() {
        if (this.mCachedCallLogListHeaderView == null) {
            initLayoutInflater();
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragmentHelper", "inflater callLog header");
            }
            return this.mInflater.inflate(R.layout.contacts_radio_button_group_divider, null);
        }
        View retView = this.mCachedCallLogListHeaderView;
        this.mCachedCallLogListHeaderView = null;
        return retView;
    }

    private void initDialpadHeaderLayout() {
        this.mCachedDialpadHeaderLayout = (DialpadHeaderLayout) LayoutInflater.from(this.mActivity).inflate(R.layout.dialpad_header_layout, null);
    }

    public DialpadHeaderLayout getDialpadHeaderLayout() {
        if (this.mCachedDialpadHeaderLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(this.mActivity);
            if (HwLog.HWDBG) {
                HwLog.d("DialpadFragmentHelper", "inflater dialpad_header");
            }
            return (DialpadHeaderLayout) inflater.inflate(R.layout.dialpad_header_layout, null);
        }
        DialpadHeaderLayout retDialpadHeaderLayout = this.mCachedDialpadHeaderLayout;
        this.mCachedDialpadHeaderLayout = null;
        return retDialpadHeaderLayout;
    }

    private void initDialpadLayout() {
        initLayoutInflater();
        synchronized (this.mDialpadLock) {
            if (this.mDiapadView == null) {
                if (CommonUtilMethods.isLargeThemeApplied(this.mContext.getResources()) || CommonUtilMethods.isSpecialLanguageForDialpad()) {
                    this.mDiapadView = this.mInflater.inflate(R.layout.dialpad_huawei, null);
                } else {
                    this.mDiapadView = this.mInflater.inflate(R.layout.contacts_dialpad, null);
                }
            }
        }
    }

    public View getDialpadView() {
        View retDialpadView;
        synchronized (this.mDialpadLock) {
            if (this.mDiapadView == null) {
                initLayoutInflater();
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadFragmentHelper", "inflater dialpad");
                }
                if (CommonUtilMethods.isLargeThemeApplied(this.mContext.getResources()) || CommonUtilMethods.isSpecialLanguageForDialpad()) {
                    retDialpadView = this.mInflater.inflate(R.layout.dialpad_huawei, null);
                } else {
                    retDialpadView = this.mInflater.inflate(R.layout.contacts_dialpad, null);
                }
            } else {
                retDialpadView = this.mDiapadView;
                this.mDiapadView = null;
            }
        }
        return retDialpadView;
    }

    public void startInitNewViewInBackground(Activity activity) {
        this.mActivity = activity;
        sendEmptyMessage(17);
    }

    private void inflateNewView() {
        if (this.mNewViews == null) {
            this.mNewViews = new Stack();
        }
        int viewsCount = this.mNewViews.size();
        initLayoutInflater();
        for (int i = 0; i < 6 - viewsCount; i++) {
            this.mNewViews.push(this.mInflater.inflate(R.layout.freq_call_list_row, null));
        }
    }

    public View getNewView() {
        if (this.mNewViews != null && !this.mNewViews.isEmpty()) {
            return (View) this.mNewViews.pop();
        }
        initLayoutInflater();
        if (HwLog.HWDBG) {
            HwLog.d("DialpadFragmentHelper", "inflater search list row");
        }
        return this.mInflater.inflate(R.layout.freq_call_list_row, null);
    }
}
