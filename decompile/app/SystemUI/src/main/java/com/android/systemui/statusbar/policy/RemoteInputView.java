package com.android.systemui.statusbar.policy;

import android.app.Notification.Action;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.logging.MetricsLogger;
import com.android.mms.service.MmsService;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.stack.ScrollContainer;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.MmsUtils;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;

public class RemoteInputView extends FrameLayout implements OnClickListener, TextWatcher {
    public static final Object VIEW_TAG = new Object();
    private HwCustRemoteInputView hwCustRemoteInputView = ((HwCustRemoteInputView) HwCustUtils.createObj(HwCustRemoteInputView.class, new Object[]{this}));
    private RemoteInputController mController;
    private RemoteEditText mEditText;
    private Entry mEntry;
    private TextView mInputCount;
    private boolean mIsRCS = false;
    private int mMaxSmsSplit = 11;
    private PendingIntent mPendingIntent;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInput[] mRemoteInputs;
    private boolean mRemoved;
    private Runnable mResetRunnable;
    private ScrollContainer mScrollContainer;
    private View mScrollContainerChild;
    private ImageButton mSendButton;
    private boolean mSent = false;
    private TextView mText;
    private View mTopView;

    public static class RemoteEditText extends EditText {
        private RemoteInputView mRemoteInputView = null;
        boolean mShowImeOnInputConnection;

        public RemoteEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        private void defocusIfNeeded() {
            if ((this.mRemoteInputView == null || !this.mRemoteInputView.mEntry.row.isChangingPosition()) && isFocusable() && isEnabled()) {
                setInnerFocusable(false);
                if (this.mRemoteInputView != null) {
                    this.mRemoteInputView.onDefocus();
                }
                this.mShowImeOnInputConnection = false;
            }
        }

        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            if (!isShown()) {
                defocusIfNeeded();
            }
        }

        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            if (!focused) {
                defocusIfNeeded();
            }
        }

        public void getFocusedRect(Rect r) {
            super.getFocusedRect(r);
            r.top = this.mScrollY;
            r.bottom = this.mScrollY + (this.mBottom - this.mTop);
        }

        public boolean requestRectangleOnScreen(Rect rectangle) {
            return this.mRemoteInputView.requestScrollTo();
        }

        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode != 4 || event.getAction() != 1) {
                return super.onKeyPreIme(keyCode, event);
            }
            defocusIfNeeded();
            InputMethodManager.getInstance().hideSoftInputFromWindow(getWindowToken(), 0);
            return true;
        }

        public boolean onCheckIsTextEditor() {
            boolean flyingOut;
            if (this.mRemoteInputView != null) {
                flyingOut = this.mRemoteInputView.mRemoved;
            } else {
                flyingOut = false;
            }
            if (flyingOut) {
                return false;
            }
            return super.onCheckIsTextEditor();
        }

        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
            if (this.mShowImeOnInputConnection && inputConnection != null) {
                final InputMethodManager imm = InputMethodManager.getInstance();
                if (imm != null) {
                    post(new Runnable() {
                        public void run() {
                            imm.viewClicked(RemoteEditText.this);
                            imm.showSoftInput(RemoteEditText.this, 0);
                        }
                    });
                }
            }
            return inputConnection;
        }

        public void onCommitCompletion(CompletionInfo text) {
            clearComposingText();
            setText(text.getText());
            setSelection(getText().length());
        }

        void setInnerFocusable(boolean focusable) {
            setFocusableInTouchMode(focusable);
            setFocusable(focusable);
            setCursorVisible(focusable);
            if (focusable) {
                requestFocus();
            }
        }
    }

    public RemoteInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(R.id.remote_input_progress);
        this.mSendButton = (ImageButton) findViewById(R.id.remote_input_send);
        this.mSendButton.setOnClickListener(this);
        this.mEditText = (RemoteEditText) findViewById(R.id.remote_input_text);
        this.mEditText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean isSoftImeEvent = event == null ? (actionId == 6 || actionId == 5) ? true : actionId == 4 : false;
                boolean isKeyboardEnterKey = (event == null || !KeyEvent.isConfirmKey(event.getKeyCode())) ? false : event.getAction() == 0;
                if (!isSoftImeEvent && !isKeyboardEnterKey) {
                    return false;
                }
                if (RemoteInputView.this.mEditText.length() > 0) {
                    RemoteInputView.this.sendRemoteInput();
                }
                return true;
            }
        });
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setInnerFocusable(false);
        this.mEditText.mRemoteInputView = this;
        this.mInputCount = (TextView) findViewById(R.id.input_count);
    }

    public void setTopView(View view) {
        this.mTopView = view;
        this.mText = (TextView) this.mTopView.findViewById(16908413);
    }

    private void setFilter() {
        if (this.mEntry.notification.getPackageName().equals("com.android.mms")) {
            int i;
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    RemoteInputView.this.mMaxSmsSplit = MmsService.getSmsToMmsTextThreshhold(RemoteInputView.this.getContext());
                    return super.runInThread();
                }

                public void runInUI() {
                    if (RemoteInputView.this.mMaxSmsSplit <= 1) {
                        Log.d("RemoteInput", "SmsToMmsTextThreshhold not set." + RemoteInputView.this.mMaxSmsSplit);
                        RemoteInputView.this.mMaxSmsSplit = 11;
                    }
                    Log.d("RemoteInput", "Add SmsToMmsTextThreshhold ." + RemoteInputView.this.mMaxSmsSplit + " ;  " + ((RemoteInputView.this.mMaxSmsSplit - 1) * 67));
                    RemoteInputView.this.mEditText.setFilters(new InputFilter[]{new LengthFilter(maxCharLen)});
                    super.runInUI();
                }
            });
            if (!(this.mEntry.notification.getNotification() == null || this.mEntry.notification.getNotification().extras == null)) {
                this.mIsRCS = this.mEntry.notification.getNotification().extras.getBoolean("hw_rcs", false);
            }
            ImageButton imageButton = this.mSendButton;
            if (this.mIsRCS) {
                i = R.drawable.send_reply_rcs;
            } else {
                i = R.drawable.send_reply;
            }
            imageButton.setBackgroundResource(i);
        }
    }

    private void save() {
        HwLog.i("RemoteInput", "save");
        Bundle results = new Bundle();
        results.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent fillInIntent = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, fillInIntent, results);
        fillInIntent.putExtra("hw_save_mms", true);
        try {
            this.mPendingIntent.send(this.mContext, 0, fillInIntent);
        } catch (CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
        }
    }

    private void sendRemoteInput() {
        this.mSent = true;
        Bundle results = new Bundle();
        results.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent fillInIntent = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, fillInIntent, results);
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mInputCount.setVisibility(4);
        this.mEntry.remoteInputText = this.mEditText.getText();
        this.mController.addSpinning(this.mEntry.key);
        this.mController.removeRemoteInput(this.mEntry);
        this.mEditText.mShowImeOnInputConnection = false;
        this.mController.remoteInputSent(this.mEntry);
        MetricsLogger.action(this.mContext, 398, this.mEntry.notification.getPackageName());
        if (this.mEntry.notification.getTag() != null && this.mEntry.notification.getTag().contains("_hwclone")) {
            fillInIntent.addHwFlags(1);
        }
        try {
            this.mPendingIntent.send(this.mContext, 0, fillInIntent);
        } catch (CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
            MetricsLogger.action(this.mContext, 399, this.mEntry.notification.getPackageName());
        }
        removeCallbacks(this.mResetRunnable);
        this.mResetRunnable = new Runnable() {
            public void run() {
                RemoteInputView.this.reset();
            }
        };
        postDelayed(this.mResetRunnable, 500);
    }

    public static RemoteInputView inflate(Context context, ViewGroup root, Entry entry, RemoteInputController controller) {
        RemoteInputView v = (RemoteInputView) LayoutInflater.from(context).inflate(R.layout.remote_input, root, false);
        v.mController = controller;
        v.mEntry = entry;
        v.setTag(VIEW_TAG);
        v.setFilter();
        return v;
    }

    public void onClick(View v) {
        if (v == this.mSendButton) {
            sendRemoteInput();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public void onDefocus() {
        if (this.mText != null) {
            this.mText.setSingleLine(true);
        }
        if (this.mEntry.notification.getPackageName().equals("com.android.mms") && !this.mSent) {
            save();
        }
        this.mController.removeRemoteInput(this.mEntry);
        this.mEntry.remoteInputText = this.mEditText.getText();
        if (!this.mRemoved) {
            setVisibility(8);
        }
        postDelayed(new Runnable() {
            public void run() {
                HwPhoneStatusBar.getInstance().onHideRemoteInput();
            }
        }, 0);
        MetricsLogger.action(this.mContext, 400, this.mEntry.notification.getPackageName());
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEntry.row.isChangingPosition() && getVisibility() == 0 && this.mEditText.isFocusable()) {
            this.mEditText.requestFocus();
        }
    }

    public void setVisibility(int visibility) {
        if (this.mTopView != null) {
            int height;
            View actions = this.mTopView.findViewById(16909210);
            if (visibility == 0) {
                height = getResources().getDimensionPixelSize(R.dimen.remote_input_height);
                if (actions != null) {
                    actions.setVisibility(8);
                }
            } else {
                height = getResources().getDimensionPixelSize(17105218);
                if (actions != null) {
                    actions.setVisibility(0);
                }
            }
            View actionListMarginTarget = this.mTopView.findViewById(16909429);
            if (actionListMarginTarget != null) {
                LayoutParams layoutParams = actionListMarginTarget.getLayoutParams();
                if (layoutParams instanceof MarginLayoutParams) {
                    ((MarginLayoutParams) layoutParams).bottomMargin = height;
                    actionListMarginTarget.setLayoutParams(layoutParams);
                }
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
            lp.height = height;
            setLayoutParams(lp);
            this.mTopView.requestLayout();
        }
        super.setVisibility(visibility);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.mEntry.row.isChangingPosition()) {
            this.mController.removeRemoteInput(this.mEntry);
            this.mController.removeSpinning(this.mEntry.key);
        }
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.mPendingIntent = pendingIntent;
    }

    public void setRemoteInput(RemoteInput[] remoteInputs, RemoteInput remoteInput) {
        this.mRemoteInputs = remoteInputs;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(this.mRemoteInput.getLabel());
    }

    public void focus() {
        MetricsLogger.action(this.mContext, 397, this.mEntry.notification.getPackageName());
        if (this.mText != null) {
            this.mText.setSingleLine(false);
        }
        setVisibility(0);
        this.mController.addRemoteInput(this.mEntry);
        this.mEditText.setInnerFocusable(true);
        this.mEditText.mShowImeOnInputConnection = true;
        if (this.mEntry.notification.getPackageName().equals("com.android.mms") && this.mEntry.row.isHeadsUp()) {
            this.mEditText.setText(BuildConfig.FLAVOR);
        } else {
            this.mEditText.setText(this.mEntry.remoteInputText);
        }
        this.mEditText.setSelection(this.mEditText.getText().length());
        this.mEditText.requestFocus();
        updateSendButton();
        if (this.mEntry.notification.getPackageName().equals("com.android.mms")) {
            HwLog.i("RemoteInput", "send broadcast to mms");
            Intent intent = new Intent("com.android.mms.HEADSUP_NOTIFICATION_REMAIN");
            intent.putExtras(this.mPendingIntent.getIntent());
            getContext().sendBroadcastAsUser(intent, new UserHandle(-2), "huawei.permmisons.mms.HEADSUP_NOTIFICATION_REMAIN_ACTION");
        }
    }

    public void onNotificationUpdateOrReset() {
        boolean sending = false;
        if (this.mProgressBar.getVisibility() == 0) {
            sending = true;
        }
        if (sending) {
            reset();
        }
    }

    private void reset() {
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        this.mController.removeSpinning(this.mEntry.key);
        updateSendButton();
        onDefocus();
    }

    private void updateSendButton() {
        boolean z = false;
        ImageButton imageButton = this.mSendButton;
        if (this.mEditText.getText().length() != 0) {
            z = true;
        }
        imageButton.setEnabled(z);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        updateSendButton();
        updateInputCount();
        if (this.hwCustRemoteInputView != null) {
            this.hwCustRemoteInputView.setOnePageSmsText(s, this.mEditText);
        }
    }

    public void close() {
        this.mEditText.defocusIfNeeded();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            findScrollContainer();
            if (this.mScrollContainer != null) {
                this.mScrollContainer.requestDisallowLongPress();
                this.mScrollContainer.requestDisallowDismiss();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean requestScrollTo() {
        findScrollContainer();
        this.mScrollContainer.lockScrollTo(this.mScrollContainerChild);
        return true;
    }

    private void findScrollContainer() {
        if (this.mScrollContainer == null) {
            this.mScrollContainerChild = null;
            ViewParent p = this;
            while (p != null) {
                if (this.mScrollContainerChild == null && (p instanceof ExpandableView)) {
                    this.mScrollContainerChild = (View) p;
                }
                if (p.getParent() instanceof ScrollContainer) {
                    this.mScrollContainer = (ScrollContainer) p.getParent();
                    if (this.mScrollContainerChild == null) {
                        this.mScrollContainerChild = (View) p;
                        return;
                    }
                    return;
                }
                p = p.getParent();
            }
        }
    }

    public void updateInputCount() {
        final String input = this.mEditText.getText().toString();
        if (!this.mEntry.notification.getPackageName().equals("com.android.mms")) {
            String result = Integer.valueOf(input.length() % 70 != 0 ? 70 - (input.length() % 70) : 0) + "/" + Integer.valueOf(input.length() % 70 != 0 ? (input.length() / 70) + 1 : input.length() / 70);
            if (result.length() > 0) {
                this.mInputCount.setText(result);
                this.mInputCount.setVisibility(0);
            } else {
                this.mInputCount.setText(BuildConfig.FLAVOR);
                this.mInputCount.setVisibility(8);
            }
        } else if (!this.mIsRCS) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                String result = BuildConfig.FLAVOR;

                public boolean runInThread() {
                    this.result = MmsUtils.getMmsCounterText(RemoteInputView.this.getContext(), input);
                    return super.runInThread();
                }

                public void runInUI() {
                    if (this.result.length() > 0) {
                        RemoteInputView.this.mInputCount.setText(this.result);
                        RemoteInputView.this.mInputCount.setVisibility(0);
                    } else {
                        RemoteInputView.this.mInputCount.setText(BuildConfig.FLAVOR);
                        RemoteInputView.this.mInputCount.setVisibility(8);
                    }
                    super.runInUI();
                }
            });
        }
    }

    public boolean isActive() {
        return this.mEditText.isFocused() ? this.mEditText.isEnabled() : false;
    }

    public void stealFocusFrom(RemoteInputView other) {
        other.close();
        setPendingIntent(other.mPendingIntent);
        setRemoteInput(other.mRemoteInputs, other.mRemoteInput);
        focus();
    }

    public boolean updatePendingIntentFromActions(Action[] actions) {
        if (this.mPendingIntent == null || actions == null) {
            return false;
        }
        Intent current = this.mPendingIntent.getIntent();
        if (current == null) {
            return false;
        }
        for (Action a : actions) {
            RemoteInput[] inputs = a.getRemoteInputs();
            if (!(a.actionIntent == null || inputs == null || !current.filterEquals(a.actionIntent.getIntent()))) {
                RemoteInput input = null;
                for (RemoteInput i : inputs) {
                    if (i.getAllowFreeFormInput()) {
                        input = i;
                    }
                }
                if (input != null) {
                    setPendingIntent(a.actionIntent);
                    setRemoteInput(inputs, input);
                    return true;
                }
            }
        }
        return false;
    }

    public PendingIntent getPendingIntent() {
        return this.mPendingIntent;
    }

    public void setRemoved() {
        this.mRemoved = true;
    }
}
