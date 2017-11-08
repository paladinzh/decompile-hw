package com.android.mms.attachment.ui.mediapicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.ui.mediapicker.RecorderManager.Callback;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.RichMessageEditor;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.util.AudioManagerUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;

public class AudioMediaChooser extends MediaChooser implements Callback, OnClickListener {
    private TextView mCancelPrompt = null;
    private ComposeMessageFragment mComposeMessageFragment;
    private MediaPickerPanel mMediaPickerPanel;
    private View mMissingPermissionView = null;
    private Button mPermissionButton;
    private RcsGroupChatComposeMessageFragment mRcsGroupChatComposeMessageFragment;
    private RcsGroupChatRichMessageEditor mRcsGroupChatRichMessageEditor;
    private TextView mRecordDuration = null;
    private RelativeLayout mRecordPanel;
    private RecorderManager mRecorderManager = null;
    private long mRequestTimeMillis = 0;
    private RichMessageEditor mRichMessageEditor;
    private SoundModelAnimationView mSoundModelAnimationView = null;
    private TextView mTalkPrompt;
    private Handler mTouchControlHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z;
            switch (msg.what) {
                case 1:
                    if (AudioMediaChooser.this.mRecorderManager.getState() == 0) {
                        if (OsUtil.hasRecordAudioPermission()) {
                            if (!AudioManagerUtils.isTelephonyCalling(AudioMediaChooser.this.getContext())) {
                                long sizelimit;
                                if (!(AudioMediaChooser.this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) || AudioMediaChooser.this.mRcsGroupChatRichMessageEditor == null) {
                                    sizelimit = AudioMediaChooser.this.mRichMessageEditor.computeAddRecordSizeLimit() - 5120;
                                } else {
                                    sizelimit = AudioMediaChooser.this.mRcsGroupChatRichMessageEditor.computeAddRecordSizeLimit();
                                }
                                AudioMediaChooser.this.mSoundModelAnimationView.startRecordingAnimation();
                                AudioMediaChooser.this.mRecorderManager.startRecording(3, ".amr", MmsApp.getApplication().getApplicationContext(), sizelimit);
                                AudioMediaChooser.this.mTalkPrompt.setText("");
                                AudioMediaChooser.this.mCancelPrompt.setText("");
                                AudioManagerUtils.requestAudioManagerFocus(AudioMediaChooser.this.getContext(), 2);
                                StatisticalHelper.incrementReportCount(AudioMediaChooser.this.getContext(), 2260);
                                break;
                            }
                            Toast.makeText(AudioMediaChooser.this.getContext(), R.string.record_audio_in_calling_toast, 1).show();
                            break;
                        }
                        AudioMediaChooser.this.requestRecordAudioPermission();
                        break;
                    }
                    break;
                case 2:
                    int stop_type = 0;
                    if (msg.obj != null) {
                        stop_type = ((Integer) msg.obj).intValue();
                    }
                    if (AudioMediaChooser.this.mRecorderManager.getState() == 1) {
                        AudioMediaChooser.this.mRecorderManager.stopRecording();
                        if (AudioMediaChooser.this.mSoundModelAnimationView != null) {
                            AudioMediaChooser.this.mSoundModelAnimationView.stopAndClearAnimations();
                        }
                        AudioMediaChooser.this.mTalkPrompt.setText(R.string.talk_prompt_text);
                        Uri uri = AudioMediaChooser.this.mRecorderManager.getRecordUri();
                        if (uri != null) {
                            MessageUtils.addFileToIndex(AudioMediaChooser.this.getContext(), uri.getPath());
                        }
                        if (AudioMediaChooser.this.mRecorderManager.sampleLength() > 1) {
                            AudioMediaChooser.this.mMediaPicker.dispatchItemsSelected(AudioMediaChooser.this.createAttachmentData(3, uri), true);
                            AudioMediaChooser.this.mCancelPrompt.setText("");
                            AudioMediaChooser.this.mRecordDuration.setText("");
                            if (stop_type == 1) {
                                Toast.makeText(AudioMediaChooser.this.getContext(), R.string.exceed_message_size_limitation, 0).show();
                            } else if (stop_type == 2) {
                                Toast.makeText(AudioMediaChooser.this.getContext(), R.string.storage_warning_title, 0).show();
                            }
                            AudioManagerUtils.abandonAudioFocus(AudioMediaChooser.this.getContext());
                            break;
                        }
                        AudioMediaChooser.this.mCancelPrompt.setText("");
                        AudioMediaChooser.this.mRecordDuration.setText("");
                        int textInt = R.string.record_too_short;
                        if (stop_type == 1) {
                            textInt = R.string.exceed_message_size_limitation;
                        } else if (stop_type == 2) {
                            textInt = R.string.storage_warning_title;
                        }
                        Toast mToast = Toast.makeText(AudioMediaChooser.this.getContext(), textInt, 1);
                        AudioManagerUtils.abandonAudioFocus(AudioMediaChooser.this.getContext());
                        mToast.show();
                        if (textInt == R.string.record_too_short) {
                            StatisticalHelper.incrementReportCount(AudioMediaChooser.this.getContext(), 2248);
                            break;
                        }
                    }
                    break;
                case 3:
                    if (AudioMediaChooser.this.mRecorderManager.getState() == 1) {
                        AudioMediaChooser.this.mRecorderManager.stopRecording();
                        AudioMediaChooser.this.mRecorderManager.delete();
                        AudioMediaChooser.this.mSoundModelAnimationView.stopAndClearAnimations();
                        AudioMediaChooser.this.mTalkPrompt.setText(R.string.talk_prompt_text);
                        AudioMediaChooser.this.mCancelPrompt.setText("");
                        AudioMediaChooser.this.mRecordDuration.setText("");
                        AudioManagerUtils.abandonAudioFocus(AudioMediaChooser.this.getContext());
                        break;
                    }
                    break;
            }
            AudioMediaChooser.this.mMediaPickerPanel.updateViewPager(AudioMediaChooser.this.mRecorderManager.getState() != 1);
            MediaPickerPanel -get1 = AudioMediaChooser.this.mMediaPickerPanel;
            if (AudioMediaChooser.this.mRecorderManager.getState() != 1) {
                z = true;
            } else {
                z = false;
            }
            -get1.setPanelTouchEnabled(z);
        }
    };

    public AudioMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    public int getSupportedMediaTypes() {
        return 4;
    }

    public View destroyView() {
        if (this.mRecorderManager != null) {
            this.mRecorderManager.unregisterRecordCallback(this);
            this.mRecorderManager.clearCallbacks();
        }
        if (this.mSoundModelAnimationView != null) {
            this.mSoundModelAnimationView.stopAndClearAnimations();
            this.mSoundModelAnimationView.destoryAnimationView();
            this.mSoundModelAnimationView = null;
        }
        return super.destroyView();
    }

    public int getIconResource() {
        return this.mSelected ? R.drawable.ic_sms_add_capture_voice_checked : R.drawable.ic_public_voice;
    }

    protected int getIconTextResource() {
        return R.string.attach_record_sound;
    }

    public boolean canSwipeDown() {
        return false;
    }

    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    protected void onRestoreChooserState() {
        setSelected(false);
    }

    protected View createView(ViewGroup container) {
        View view = getLayoutInflater().inflate(R.layout.mediapicker_audio_chooser, container, false);
        this.mMediaPickerPanel = this.mMediaPicker.getMediaPickerPanel();
        this.mRecorderManager = RecorderManager.getInstance(this.mMediaPicker.getActivity());
        this.mRecorderManager.registerRecordCallback(this);
        this.mMissingPermissionView = view.findViewById(R.id.missing_permission_view);
        this.mPermissionButton = (Button) view.findViewById(R.id.request_perimission_btn);
        this.mPermissionButton.setOnClickListener(this);
        this.mRecordPanel = (RelativeLayout) view.findViewById(R.id.record_panel);
        this.mSoundModelAnimationView = (SoundModelAnimationView) view.findViewById(R.id.mic_sound_view);
        this.mRecordDuration = (TextView) view.findViewById(R.id.tv_tips);
        this.mCancelPrompt = (TextView) view.findViewById(R.id.cancel_prompt_view);
        this.mTalkPrompt = (TextView) view.findViewById(R.id.talk_prompt_view);
        if (HwMessageUtils.isSplitOn() && (this.mMediaPicker.getActivity() instanceof ConversationList)) {
            if (((ConversationList) this.mMediaPicker.getActivity()).getRightFragment() instanceof RcsGroupChatComposeMessageFragment) {
                this.mRcsGroupChatComposeMessageFragment = (RcsGroupChatComposeMessageFragment) ((ConversationList) this.mMediaPicker.getActivity()).getRightFragment();
                this.mRcsGroupChatRichMessageEditor = this.mRcsGroupChatComposeMessageFragment.getRichEditor();
            } else {
                this.mComposeMessageFragment = (ComposeMessageFragment) ((ConversationList) this.mMediaPicker.getActivity()).getRightFragment();
                if (this.mComposeMessageFragment != null) {
                    this.mRichMessageEditor = this.mComposeMessageFragment.getRichEditor();
                }
            }
        } else if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
            this.mRcsGroupChatComposeMessageFragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
            this.mRcsGroupChatRichMessageEditor = this.mRcsGroupChatComposeMessageFragment.getRichEditor();
        } else {
            this.mComposeMessageFragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
            this.mRichMessageEditor = this.mComposeMessageFragment.getRichEditor();
        }
        this.mCancelPrompt.setText("");
        this.mRecordPanel.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case 1:
                        AudioMediaChooser.this.stopRecording();
                        break;
                }
                return true;
            }
        });
        this.mSoundModelAnimationView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case 0:
                        AudioMediaChooser.this.mTouchControlHandler.sendEmptyMessageDelayed(1, 200);
                        break;
                    case 1:
                    case 3:
                        AudioMediaChooser.this.stopRecording();
                        break;
                }
                return true;
            }
        });
        updateForPermissionState(hasPermission("android.permission.RECORD_AUDIO"));
        return view;
    }

    private AttachmentSelectData createAttachmentData(int attachmentType, Uri uriItem) {
        AttachmentSelectData attachmentItem = new AttachmentSelectData(attachmentType);
        attachmentItem.setAttachmentUri(uriItem);
        return attachmentItem;
    }

    protected int getActionBarTitleResId() {
        return 1;
    }

    private void requestRecordAudioPermission() {
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(this.mMediaPicker.getActivity(), new String[]{"android.permission.RECORD_AUDIO"}, 3);
    }

    protected void updateActionBar(AbstractEmuiActionBar actionBar) {
        super.updateActionBar(actionBar);
        if (actionBar != null && this.mMediaPicker.isFullScreen()) {
            actionBar.setTitle(getContext().getResources().getString(R.string.attach_record_sound));
        }
    }

    public void onResume() {
        updateForPermissionState(hasPermission("android.permission.RECORD_AUDIO"));
    }

    protected void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults == null || grantResults.length == 0) {
            MLog.d("AudioMediaChooser", "onRequestPermissionsResult grantResults is invaild.");
            return;
        }
        long currentTimeMillis = SystemClock.elapsedRealtime();
        int permissionGranted = grantResults[0];
        if (requestCode == 3) {
            if (permissionGranted == 0) {
                updateForPermissionState(true);
                StatisticalHelper.incrementReportCount(getContext(), 2259);
            } else if (permissionGranted == -1 && currentTimeMillis - this.mRequestTimeMillis < 500) {
                gotoPackageSettings(this.mMediaPicker.getActivity(), 3);
            }
        }
    }

    private boolean hasPermission(String perm) {
        return this.mMediaPicker.getContext().checkSelfPermission(perm) == 0;
    }

    public static void gotoPackageSettings(Activity act, int requestCode) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + act.getPackageName()));
        intent.setFlags(268435456);
        act.startActivityForResult(intent, requestCode);
    }

    private void updateForPermissionState(boolean granted) {
        int i = 8;
        if (this.mRecordPanel != null) {
            int i2;
            RelativeLayout relativeLayout = this.mRecordPanel;
            if (granted) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            relativeLayout.setVisibility(i2);
            if (this.mMissingPermissionView != null) {
                View view = this.mMissingPermissionView;
                if (!granted) {
                    i = 0;
                }
                view.setVisibility(i);
            }
        }
    }

    public void onError(int error) {
        if (error == 5) {
            this.mTouchControlHandler.sendMessage(this.mTouchControlHandler.obtainMessage(3));
            Toast.makeText(getContext(), R.string.storage_warning_title, 1).show();
        }
        if (this.mRecorderManager.getState() == 1) {
            this.mRecorderManager.stopRecording();
        }
        if (this.mSoundModelAnimationView != null) {
            this.mSoundModelAnimationView.stopAndClearAnimations();
        }
        if (this.mCancelPrompt != null) {
            this.mCancelPrompt.setText("");
        }
        this.mRecordDuration.setText("");
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
        if (this.mRecordDuration != null) {
            this.mRecordDuration.setText(String.format("%02d:%02d", new Object[]{Long.valueOf((time % 3600) / 60), Long.valueOf((time % 3600) % 60)}));
        }
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

    public void onPause() {
        stopRecording();
        super.onPause();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.request_perimission_btn) {
            requestRecordAudioPermission();
        }
    }
}
