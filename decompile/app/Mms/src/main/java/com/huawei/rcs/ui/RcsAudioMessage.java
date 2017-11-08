package com.huawei.rcs.ui;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.messaging.util.OsUtil;
import com.android.mms.attachment.ui.mediapicker.RcsSoundModelAnimationView;
import com.android.mms.attachment.ui.mediapicker.RecorderManager;
import com.android.mms.attachment.ui.mediapicker.RecorderManager.Callback;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.AudioManagerUtils;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.List;

public class RcsAudioMessage implements Callback {
    public static final boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private static int mCurrentView = 3;
    private int RECORD_TOO_SHORT_DIALOG_TIME = 1000;
    private float downY;
    private AnimationDialog mAnimationDialog;
    public OnTouchListener mAudioViewTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case 0:
                    RcsAudioMessage.this.mPickAudioButton.setPressed(true);
                    RcsAudioMessage.this.mTouchControlHandler.sendEmptyMessageDelayed(1, 200);
                    RcsAudioMessage.this.downY = motionEvent.getY();
                    break;
                case 1:
                    if (RcsAudioMessage.this.mRecorderManager.getState() != 0) {
                        if (RcsAudioMessage.this.mRecorderManager.getState() == 1) {
                            RcsAudioMessage.this.moveY = RcsAudioMessage.this.downY - motionEvent.getY();
                            if (RcsAudioMessage.this.moveY < 400.0f) {
                                RcsAudioMessage.this.mPickAudioButton.setPressed(false);
                                RcsAudioMessage.this.mTouchControlHandler.sendEmptyMessage(2);
                                break;
                            }
                            RcsAudioMessage.this.mAnimationDialog.dismiss();
                            RcsAudioMessage.this.mSoundModelAnimationView.stopAndClearAnimations();
                            RcsAudioMessage.this.mPickAudioButton.setPressed(false);
                            RcsAudioMessage.this.mPickAudioButton.setText(R.string.talk_prompt_text);
                            RcsAudioMessage.this.mRecorderManager.stopRecording();
                            RcsAudioMessage.this.mRecorderManager.delete();
                            AudioManagerUtils.abandonAudioFocus(RcsAudioMessage.this.mContext);
                            break;
                        }
                    }
                    RcsAudioMessage.this.mPickAudioButton.setPressed(false);
                    RcsAudioMessage.this.mTouchControlHandler.removeMessages(1);
                    if (RcsAudioMessage.this.mAnimationDialog != null) {
                        RcsAudioMessage.this.mAnimationDialog.dismiss();
                    }
                    RcsAudioMessage.this.mPickAudioButton.setText(R.string.talk_prompt_text);
                    break;
                    break;
                case 2:
                    RcsAudioMessage.this.moveY = Math.abs(RcsAudioMessage.this.downY - motionEvent.getY());
                    if (RcsAudioMessage.this.moveY >= 400.0f) {
                        RcsAudioMessage.this.mSoundModelAnimationView.setVisibility(8);
                        RcsAudioMessage.this.mRecordCountDown.setVisibility(0);
                        RcsAudioMessage.this.mRecordCountDown.setText("");
                        RcsAudioMessage.this.mRecordCountDown.setBackground(ResEx.self().getStateListDrawable(RcsAudioMessage.this.mContext, R.drawable.ic_mms_rcs_cancel));
                        RcsAudioMessage.this.mRecordText.setText(R.string.slide_down_text);
                        RcsAudioMessage.this.mPickAudioButton.setText(R.string.rcs_release_to_cancel);
                        break;
                    }
                    if (590 <= RcsAudioMessage.this.recordTime) {
                        RcsAudioMessage.this.mSoundModelAnimationView.setVisibility(8);
                        RcsAudioMessage.this.mRecordCountDown.setVisibility(0);
                    } else {
                        RcsAudioMessage.this.mSoundModelAnimationView.setVisibility(0);
                        RcsAudioMessage.this.mRecordCountDown.setVisibility(8);
                    }
                    RcsAudioMessage.this.mRecordText.setText(R.string.slide_up_text);
                    RcsAudioMessage.this.mPickAudioButton.setText(R.string.realease_send_prompt_text);
                    break;
                case 3:
                    RcsAudioMessage.this.mPickAudioButton.setPressed(false);
                    RcsAudioMessage.this.stopRecording();
                    break;
            }
            return true;
        }
    };
    private Context mContext;
    private Conversation mConversation;
    private Fragment mFragment;
    private Button mPickAudioButton;
    private TextView mRecordCountDown;
    private TextView mRecordText;
    private TextView mRecordTime;
    private RecorderManager mRecorderManager = null;
    private RcsSoundModelAnimationView mSoundModelAnimationView;
    private Handler mTouchControlHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (RcsAudioMessage.this.mRecorderManager.getState() != 0) {
                        return;
                    }
                    if (!OsUtil.hasRecordAudioPermission()) {
                        RcsAudioMessage.this.requestRecordAudioPermission();
                        return;
                    } else if (AudioManagerUtils.isTelephonyCalling(RcsAudioMessage.this.mContext)) {
                        Toast.makeText(RcsAudioMessage.this.mContext, R.string.record_audio_in_calling_toast, 1).show();
                        return;
                    } else {
                        RcsAudioMessage.this.mAnimationDialog.show();
                        RcsAudioMessage.this.mRecordCountDown.setVisibility(8);
                        RcsAudioMessage.this.mSoundModelAnimationView.setVisibility(0);
                        RcsAudioMessage.this.mSoundModelAnimationView.startRecordingAnimation();
                        RcsAudioMessage.this.mPickAudioButton.setText(R.string.realease_send_prompt_text);
                        RcsAudioMessage.this.mRecorderManager.startRecording(3, ".amr", RcsAudioMessage.this.mContext, -1);
                        AudioManagerUtils.requestAudioManagerFocus(RcsAudioMessage.this.mContext, 2);
                        return;
                    }
                case 2:
                    int stop_type = 0;
                    if (msg.obj != null) {
                        stop_type = ((Integer) msg.obj).intValue();
                    }
                    if (RcsAudioMessage.this.mRecorderManager.getState() == 1) {
                        RcsAudioMessage.this.mPickAudioButton.setText(R.string.talk_prompt_text);
                        RcsAudioMessage.this.mRecorderManager.stopRecording(RcsAudioMessage.this.mContext);
                        if (RcsAudioMessage.this.mRecorderManager.sampleLengthMilliSecond() <= 400) {
                            int textInt = R.string.record_too_short;
                            if (stop_type == 1) {
                                textInt = R.string.exceed_message_size_limitation;
                            } else if (stop_type == 2) {
                                textInt = R.string.storage_warning_title;
                            }
                            RcsAudioMessage.this.showRecordTooShortDialog(textInt);
                            RcsAudioMessage.this.addSignatureWhenTooShort();
                            return;
                        }
                        RcsAudioMessage.this.mAnimationDialog.dismiss();
                        Uri uri = RcsAudioMessage.this.mRecorderManager.getRecordUri();
                        if (RcsAudioMessage.this.mFragment instanceof ComposeMessageFragment) {
                            ComposeMessageFragment mComposeMessageFragment = (ComposeMessageFragment) RcsAudioMessage.this.mFragment;
                            mComposeMessageFragment.getWorkingMessage().syncWorkingRecipients();
                            mComposeMessageFragment.onPreMessageSent();
                            List<Uri> uriList = new ArrayList();
                            uriList.add(uri);
                            List<String> rcsList = new ArrayList();
                            for (String number : RcsAudioMessage.this.mConversation.getRecipients().getNumbers()) {
                                rcsList.add(number);
                            }
                            RcsTransaction.multiSend(RcsAudioMessage.this.mContext, Long.valueOf(RcsAudioMessage.this.mConversation.ensureThreadId()), uriList, rcsList, 120);
                            mComposeMessageFragment.setSentMessage(true);
                            mComposeMessageFragment.onMessageSent();
                        } else if (RcsAudioMessage.this.mFragment instanceof RcsGroupChatComposeMessageFragment) {
                            RcsGroupChatComposeMessageFragment fragment = (RcsGroupChatComposeMessageFragment) RcsAudioMessage.this.mFragment;
                            long threadId = fragment.getmThreadID();
                            String groupID = fragment.getGroupID();
                            fragment.onPreGroupChatMessageSent();
                            RcsTransaction.rcsSendGroupAnyFile(RcsAudioMessage.this.mContext, uri, threadId, groupID);
                        }
                        AudioManagerUtils.abandonAudioFocus(RcsAudioMessage.this.mContext);
                        return;
                    }
                    return;
                case 3:
                    if (RcsAudioMessage.this.mRecorderManager.getState() == 1) {
                        RcsAudioMessage.this.mPickAudioButton.setText(R.string.talk_prompt_text);
                        RcsAudioMessage.this.mRecorderManager.stopRecording();
                        RcsAudioMessage.this.mRecorderManager.delete();
                        AudioManagerUtils.abandonAudioFocus(RcsAudioMessage.this.mContext);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private float moveY;
    private long recordTime;

    public RcsAudioMessage(Fragment fragment) {
        this.mContext = fragment.getContext();
        this.mFragment = fragment;
        this.mRecorderManager = new RecorderManager(this.mContext);
        this.mRecorderManager.registerRecordCallback(this);
        mCurrentView = 3;
        this.mAnimationDialog = new AnimationDialog(this.mContext, R.style.dialog);
        View view = View.inflate(this.mContext, R.layout.rcs_recordanimation, null);
        this.mAnimationDialog.setContentView(view);
        this.mSoundModelAnimationView = (RcsSoundModelAnimationView) view.findViewById(R.id.mic_sound_view);
        this.mRecordTime = (TextView) view.findViewById(R.id.timer_view);
        this.mRecordText = (TextView) view.findViewById(R.id.slide_to_text);
        this.mRecordCountDown = (TextView) view.findViewById(R.id.record_countdown);
    }

    public static void setCurrentView(int viewType) {
        if (isRcsOn) {
            mCurrentView = viewType;
        }
    }

    public int getCurrentView() {
        if (isRcsOn) {
            return mCurrentView;
        }
        return 0;
    }

    public void setPickAudioButton(Button pickaudiobutton) {
        if (isRcsOn) {
            this.mPickAudioButton = pickaudiobutton;
            this.mPickAudioButton.setOnTouchListener(this.mAudioViewTouchListener);
        }
    }

    public boolean switchCurrentView() {
        if (!isRcsOn || mCurrentView == 3) {
            return false;
        }
        if (mCurrentView == 1) {
            mCurrentView = 2;
        } else if (mCurrentView == 2) {
            mCurrentView = 1;
            switchSendViewIfHasContent();
        }
        return true;
    }

    private void switchSendViewIfHasContent() {
        if (this.mFragment instanceof ComposeMessageFragment) {
            if (this.mFragment.editorHasText()) {
                mCurrentView = 3;
            }
        } else if ((this.mFragment instanceof RcsGroupChatComposeMessageFragment) && this.mFragment.editorHasText()) {
            mCurrentView = 3;
        }
    }

    public int getViewBackGroud(int res) {
        if (!isRcsOn) {
            return res;
        }
        int resId = res;
        if (mCurrentView == 1) {
            resId = R.drawable.ic_rcs_voice_icon;
        } else if (mCurrentView == 2) {
            resId = R.drawable.rcs_audio_key_board;
        }
        return resId;
    }

    public boolean getClickStatus(boolean currentstatus) {
        if (!isRcsOn || mCurrentView == 3) {
            return currentstatus;
        }
        return true;
    }

    public void requestRecordAudioPermission() {
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(this.mFragment.getActivity(), new String[]{"android.permission.RECORD_AUDIO"}, 3);
    }

    private void stopRecording() {
        if (this.mRecorderManager != null) {
            if (this.mRecorderManager.getState() == 0) {
                this.mTouchControlHandler.removeMessages(1);
            } else if (this.mRecorderManager.getState() == 1) {
                this.mTouchControlHandler.sendEmptyMessage(2);
            }
        }
    }

    public void setConversation(Conversation conv) {
        this.mConversation = conv;
    }

    public void onError(int error) {
        if (this.mRecorderManager != null && this.mRecorderManager.getState() == 1) {
            this.mRecorderManager.stopRecording();
        }
    }

    public void onMemoryFull(int storageFullType) {
        if (this.mRecorderManager != null) {
            if (this.mRecorderManager.getState() == 0) {
                this.mTouchControlHandler.removeMessages(1);
            } else if (this.mRecorderManager.getState() == 1) {
                Message message = this.mTouchControlHandler.obtainMessage(2);
                message.obj = Integer.valueOf(storageFullType);
                this.mTouchControlHandler.sendMessage(message);
            }
        }
    }

    public void onTimerChange(long time) {
        this.recordTime = time;
        if (600 <= time) {
            if (this.mPickAudioButton != null) {
                this.mPickAudioButton.setPressed(false);
            }
            this.mTouchControlHandler.sendEmptyMessage(2);
        } else if (590 <= time) {
            this.mSoundModelAnimationView.setVisibility(8);
            this.mRecordCountDown.setVisibility(0);
            if (this.mRecordText.getText().equals(this.mContext.getResources().getString(R.string.slide_down_text))) {
                this.mRecordCountDown.setText("");
                this.mRecordCountDown.setBackground(ResEx.self().getStateListDrawable(this.mContext, R.drawable.ic_mms_rcs_cancel));
            } else {
                this.mRecordCountDown.setText(String.valueOf(600 - time));
                this.mRecordCountDown.setBackgroundResource(0);
            }
        }
        if (this.mRecordTime != null) {
            this.mRecordTime.setText(String.format("%02d:%02d", new Object[]{Long.valueOf((time % 3600) / 60), Long.valueOf((time % 3600) % 60)}));
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mRecorderManager != null) {
                this.mRecorderManager.unregisterRecordCallback(this);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void showRecordTooShortDialog(int textInt) {
        this.mSoundModelAnimationView.setVisibility(8);
        this.mRecordCountDown.setVisibility(0);
        this.mRecordCountDown.setText("");
        this.mRecordCountDown.setBackground(ResEx.self().getStateListDrawable(this.mContext, R.drawable.ic_mms_rcs_tooshot));
        this.mRecordText.setText(this.mContext.getString(textInt));
        new Thread() {
            public void run() {
                try {
                    AnonymousClass3.sleep((long) RcsAudioMessage.this.RECORD_TOO_SHORT_DIALOG_TIME);
                } catch (InterruptedException e) {
                    MLog.d("RcsAudioMessage", "CountDown sleep error");
                } finally {
                    RcsAudioMessage.this.mAnimationDialog.dismiss();
                }
            }
        }.start();
    }

    private void addSignatureWhenTooShort() {
        if (this.mFragment instanceof ComposeMessageFragment) {
            this.mFragment.clearMsgAndAppendSignature();
        } else if (this.mFragment instanceof RcsGroupChatComposeMessageFragment) {
            this.mFragment.onPreGroupChatMessageSent();
        }
    }
}
