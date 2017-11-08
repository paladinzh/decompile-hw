package com.android.settings.sdencryption;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.Utils;
import com.android.settings.sdencryption.view.statemachine.MsStateMachine;
import java.util.Timer;
import java.util.TimerTask;

public class SdEncryptionProgress extends Fragment {
    private boolean isRunning = true;
    private Button mActionButton;
    private View mContentView;
    private Context mContext;
    private TextView mDescriptionText;
    private Handler mHandler = new Handler();
    private String mState;
    private MsStateMachine mStateMachine;
    private TextView mStateText;
    private Timer mTimer;

    private class MyTimerTask extends TimerTask {
        private MyTimerTask() {
        }

        public void run() {
            final String state = SdEncryptionUtils.getCryptState();
            final int percent = SdEncryptionUtils.getCryptPercent();
            SdLog.d("SdEncryptionProgress", "state = " + state + ", percent = " + percent);
            SdEncryptionProgress.this.mHandler.post(new Runnable() {
                public void run() {
                    SdEncryptionProgress.this.updateUI(state, percent);
                }
            });
        }
    }

    private static class ReturnMessage {
        String mCrypt;
        String mExtra;
        String mType;

        public ReturnMessage(String message) {
            SdLog.i("SdEncryptionProgress", "Return message = " + message);
            if (message.contains("Cryptsd failed in Encrypt")) {
                this.mCrypt = "Cryptsd failed in Encrypt";
            } else if (message.contains("Cryptsd failed in Decrypt")) {
                this.mCrypt = "Cryptsd failed in Decrypt";
            }
            if (message.contains("No Space")) {
                int index = message.lastIndexOf("No Space") + 10;
                if (index >= message.length()) {
                    this.mType = "Unknown Reason";
                    return;
                }
                this.mType = "No Space";
                this.mExtra = message.substring(index);
            } else if (message.contains("Volume Not Find")) {
                this.mType = "Volume Not Find";
            } else if (message.contains("Unknown Reason")) {
                this.mType = "Unknown Reason";
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mContext = getContext();
        this.mContentView = inflater.inflate(2130969083, container, false);
        this.mContentView.setScrollBarStyle(33554432);
        Utils.prepareCustomPreferencesList(container, this.mContentView, this.mContentView, true);
        this.mStateText = (TextView) this.mContentView.findViewById(2131887130);
        this.mDescriptionText = (TextView) this.mContentView.findViewById(2131887131);
        this.mActionButton = (Button) this.mContentView.findViewById(2131887132);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mState = bundle.getString("State");
        }
        SdLog.d("SdEncryptionProgress", "Start Process. State = " + this.mState);
        if (this.mState == null) {
            boolean success = bundle.getBoolean("code");
            String msg = bundle.getString("message");
            if (msg == null || msg.isEmpty()) {
                return null;
            }
            this.isRunning = false;
            SdLog.i("SdEncryptionProgress", "Success = " + Boolean.toString(success) + ", msg = " + msg);
            this.mStateMachine = new MsStateMachine(this, this.mContentView, Looper.getMainLooper());
            this.mStateMachine.start();
            this.mStateMachine.finishCircleImageShading();
            handleMsg(success, msg);
            return this.mContentView;
        }
        int percent = SdEncryptionUtils.getCryptPercent();
        SdLog.i("SdEncryptionProgress", "Initial percent =" + percent);
        if (percent == 100) {
            percent = 0;
        }
        String str = this.mState;
        if (str.equals("Encrypt")) {
            if (percent == 0) {
                this.mStateText.setText(2131628797);
            } else {
                this.mStateText.setText(2131628802);
                this.mDescriptionText.setText(2131628804);
                this.mDescriptionText.setVisibility(0);
            }
        } else if (!str.equals("Decrypt")) {
            SdLog.d("SdEncryptionProgress", "received unknow event, just return");
        } else if (percent == 0) {
            this.mStateText.setText(2131628797);
        } else {
            this.mStateText.setText(2131628803);
            this.mDescriptionText.setText(2131628805);
            this.mDescriptionText.setVisibility(0);
        }
        if (this.mStateMachine == null) {
            this.mStateMachine = new MsStateMachine(this, this.mContentView, Looper.getMainLooper());
            this.mStateMachine.start();
        }
        return this.mContentView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        if (this.isRunning && this.mTimer == null) {
            this.mTimer = new Timer();
            this.mTimer.schedule(new MyTimerTask(), 500, 500);
            SdLog.d("SdEncryptionProgress", "Stop Schedule.");
        }
    }

    public void onPause() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer.purge();
            this.mTimer = null;
            SdLog.d("SdEncryptionProgress", "Stop Schedule.");
        }
        super.onPause();
    }

    private void updateUI(String state, int percent) {
        if (percent >= 100 && ("disable".equals(state) || "enable".equals(state))) {
            showFinishView(this.mState);
        } else if (percent > 0) {
            if (this.mDescriptionText.getVisibility() == 8) {
                this.mDescriptionText.setVisibility(0);
            }
            String str = this.mState;
            if (str.equals("Encrypt")) {
                this.mStateText.setText(2131628802);
                this.mDescriptionText.setText(2131628804);
            } else if (str.equals("Decrypt")) {
                this.mStateText.setText(2131628803);
                this.mDescriptionText.setText(2131628805);
            } else {
                SdLog.d("SdEncryptionProgress", "received unknow event, just return");
            }
            if (this.mDescriptionText.getVisibility() != 0) {
                this.mDescriptionText.setVisibility(0);
            }
            if (this.mActionButton.getVisibility() != 8) {
                this.mActionButton.setVisibility(8);
            }
            Message msg = Message.obtain();
            msg.what = 22;
            msg.arg1 = percent;
            this.mStateMachine.sendMessage(msg);
        }
    }

    private void showFinishView(String state) {
        this.isRunning = false;
        SdLog.d("SdEncryptionProgress", "Progress Finished.");
        Message msg = Message.obtain();
        msg.what = 23;
        msg.arg1 = 100;
        this.mStateMachine.sendMessage(msg);
        if (this.mDescriptionText.getVisibility() != 8) {
            this.mDescriptionText.setVisibility(8);
        }
        if (state.equals("Encrypt")) {
            this.mStateText.setText(2131628806);
        } else if (state.equals("Decrypt")) {
            this.mStateText.setText(2131628807);
        } else {
            SdLog.d("SdEncryptionProgress", "received unknow event, just return");
        }
        this.mActionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SdEncryptionProgress.this.getActivity().finish();
            }
        });
        if (this.mActionButton.getVisibility() != 0) {
            this.mActionButton.setText(2131628808);
            this.mActionButton.setVisibility(0);
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer.purge();
            this.mTimer = null;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void onDestroyView() {
        if (this.mStateMachine != null) {
            this.mStateMachine.quit();
        }
        super.onDestroyView();
    }

    public void handleMsg(boolean success, String message) {
        ActionBar actionBar = getActivity().getActionBar();
        if (success) {
            showFinishView(message);
            return;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer.purge();
            this.mTimer = null;
        }
        Message msg = Message.obtain();
        msg.what = 25;
        this.mStateMachine.sendMessage(msg);
        ReturnMessage failedMessage = new ReturnMessage(message);
        if ("No Space".equals(failedMessage.mType)) {
            this.mStateText.setText(2131628798);
            String spaceString = getSpaceWithProperUnit(failedMessage.mExtra);
            if ("Cryptsd failed in Encrypt".equals(failedMessage.mCrypt)) {
                actionBar.setTitle(2131628778);
                this.mDescriptionText.setText(String.format(this.mContext.getResources().getString(2131628799, new Object[]{spaceString}), new Object[0]));
            } else {
                actionBar.setTitle(2131628780);
                this.mDescriptionText.setText(String.format(this.mContext.getResources().getString(2131628800, new Object[]{spaceString}), new Object[0]));
            }
            this.mActionButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.huawei.hidisk", "com.huawei.hidisk.filemanager.FileManager"));
                    try {
                        SdEncryptionProgress.this.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        SdLog.e("SdEncryptionProgress", "No Hidisk.");
                    }
                }
            });
            if (this.mActionButton.getVisibility() != 0) {
                this.mActionButton.setText(2131628801);
                this.mActionButton.setVisibility(0);
                return;
            }
            return;
        }
        if (this.mStateText.getVisibility() != 0) {
            this.mStateText.setVisibility(0);
        }
        if ("Cryptsd failed in Encrypt".equals(failedMessage.mCrypt)) {
            this.mStateText.setText(2131628814);
            actionBar.setTitle(2131628778);
        } else {
            this.mStateText.setText(2131628815);
            actionBar.setTitle(2131628780);
        }
        if (this.mDescriptionText.getVisibility() != 8) {
            this.mDescriptionText.setVisibility(8);
        }
        this.mActionButton.setText(2131628808);
        if (this.mActionButton.getVisibility() != 0) {
            this.mActionButton.setVisibility(0);
        }
        this.mActionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SdEncryptionProgress.this.getActivity().finish();
            }
        });
    }

    private String getSpaceWithProperUnit(String spaceString) {
        if (spaceString.endsWith("KB")) {
            try {
                return Formatter.formatFileSize(getActivity(), Long.valueOf(Long.valueOf(spaceString.substring(0, spaceString.length() - 2)).longValue() * 1024).longValue());
            } catch (NumberFormatException e) {
                SdLog.e("SdEncryptionProgress", "Wrong format of space string : " + spaceString);
                return spaceString;
            }
        }
        SdLog.e("SdEncryptionProgress", "Wrong format of space string : " + spaceString);
        return spaceString;
    }
}
