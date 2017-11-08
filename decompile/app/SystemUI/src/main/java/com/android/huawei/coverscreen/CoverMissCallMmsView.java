package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$id;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.util.Typefaces;
import fyusion.vislib.BuildConfig;

public class CoverMissCallMmsView extends RelativeLayout {
    private TextView mCallCount;
    private Handler mHandler;
    private ImageView mMissCallView;
    private TextView mMmsCount;
    private ImageView mNewMmsView;
    HwUpdateCallback mUpdateCallback;

    public CoverMissCallMmsView(Context context) {
        this(context, null);
    }

    public CoverMissCallMmsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverMissCallMmsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mUpdateCallback = new HwUpdateCallback() {
            public void onNewMessageChange(MessageInfo info) {
                if (info == null) {
                    Log.i("CoverMissCallMmsView", "onNewMessageChange info is null - no change happened");
                    return;
                }
                Message message = CoverMissCallMmsView.this.mHandler.obtainMessage(1000);
                message.arg1 = info.mUnReadCount;
                CoverMissCallMmsView.this.mHandler.sendMessage(message);
            }

            public void onCalllogChange(CallLogInfo info) {
                if (info == null) {
                    Log.i("CoverMissCallMmsView", "onCalllogChange info is null - no change happened");
                    return;
                }
                Message message = CoverMissCallMmsView.this.mHandler.obtainMessage(1001);
                message.arg1 = info.mMissedcount;
                CoverMissCallMmsView.this.mHandler.sendMessage(message);
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        CoverMissCallMmsView.this.onMmsChange(msg.arg1);
                        break;
                    case 1001:
                        CoverMissCallMmsView.this.onCallLogChange(msg.arg1);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("CoverMissCallMmsView", " onFinishInflate");
        this.mMissCallView = (ImageView) findViewById(R$id.cover_miss_calllog);
        this.mNewMmsView = (ImageView) findViewById(R$id.cover_new_mms);
        this.mCallCount = (TextView) findViewById(R$id.cover_miss_calllog_count);
        this.mMmsCount = (TextView) findViewById(R$id.cover_new_mms_count);
        if (this.mMissCallView == null || this.mCallCount == null || this.mNewMmsView == null || this.mMmsCount == null) {
            Log.w("CoverMissCallMmsView", "onFinishInflate, mMissCallView=" + this.mMissCallView + ", mCallCount=" + this.mCallCount + ", mNewMmsView=" + this.mNewMmsView + ", mMmsCount=" + this.mMmsCount);
            return;
        }
        Typeface t = Typefaces.get(getContext(), "/system/fonts/Roboto-Regular.ttf");
        if (t != null) {
            this.mCallCount.setTypeface(t);
            this.mMmsCount.setTypeface(t);
        }
        this.mMissCallView.setVisibility(8);
        this.mCallCount.setVisibility(8);
        this.mNewMmsView.setVisibility(8);
        this.mMmsCount.setVisibility(8);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
    }

    public void onCallLogChange(int count) {
        if (this.mMissCallView == null || this.mCallCount == null) {
            Log.w("CoverMissCallMmsView", "onCallLogChange view is null");
        } else if (count <= 0) {
            Log.w("CoverMissCallMmsView", "hide miss call view count = " + count);
            this.mMissCallView.setVisibility(8);
            this.mCallCount.setVisibility(8);
        } else {
            CharSequence numText = BuildConfig.FLAVOR + count;
            if (count > 99) {
                numText = "99+";
            }
            this.mCallCount.setText(numText);
            if (!KeyguardUpdateMonitor.getInstance(getContext()).isSimPinSecure()) {
                this.mMissCallView.setVisibility(0);
                this.mCallCount.setVisibility(0);
            }
        }
    }

    public void onMmsChange(int count) {
        if (this.mNewMmsView == null || this.mMmsCount == null) {
            Log.w("CoverMissCallMmsView", "onCallLogChange view is null");
        } else if (count <= 0) {
            Log.w("CoverMissCallMmsView", "hide miss mms view count = " + count);
            this.mNewMmsView.setVisibility(8);
            this.mMmsCount.setVisibility(8);
        } else {
            CharSequence numText = BuildConfig.FLAVOR + count;
            if (count > 99) {
                numText = "99+";
            }
            this.mMmsCount.setText(numText);
            if (!KeyguardUpdateMonitor.getInstance(getContext()).isSimPinSecure()) {
                this.mNewMmsView.setVisibility(0);
                this.mMmsCount.setVisibility(0);
            }
        }
    }
}
