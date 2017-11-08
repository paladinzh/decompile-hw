package com.android.dialer.greeting.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.dialer.greeting.presenter.RecordPresenter;
import com.android.dialer.greeting.ui.RecordLayout.RecordStatusListener;
import com.google.android.gms.R;

public class AudioRecordDialog extends DialogFragment {
    private static final String TAG = AudioRecordDialog.class.getSimpleName();
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (AudioRecordDialog.this.mPresenter.isRecording()) {
                AudioRecordDialog.this.mPresenter.stopRecording();
            } else {
                AudioRecordDialog.this.mPresenter.startRecording();
            }
        }
    };
    private Button mPositiveButton;
    private RecordPresenter mPresenter;
    private RecordStatusListener mRecordListener = new RecordStatusListener() {
        public void onRecordStop(String fileName, int duration) {
            AudioRecordDialog.this.dismiss();
            GreetingSaveDialog.show(AudioRecordDialog.this.getFragmentManager(), AudioRecordDialog.this.getGreetingValues(duration), fileName);
        }

        public void onRecordStarted() {
            if (AudioRecordDialog.this.mPositiveButton != null) {
                AudioRecordDialog.this.mPositiveButton.setText(17039370);
            }
        }

        public void onRecordAbort() {
            AudioRecordDialog.this.dismiss();
        }
    };
    private RecordLayout mRecordView;

    public static void show(String phoneAccountId, FragmentManager fm) {
        AudioRecordDialog dialog = new AudioRecordDialog();
        Bundle bundle = new Bundle();
        bundle.putString("phone_account_id", phoneAccountId);
        dialog.setArguments(bundle);
        dialog.show(fm, TAG);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPresenter = RecordPresenter.getInstance(getActivity(), savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        builder.setTitle(getString(R.string.action_new_greeting));
        this.mRecordView = (RecordLayout) LayoutInflater.from(getActivity()).inflate(R.layout.audio_record_dialog, null);
        this.mRecordView.setStatusListener(this.mRecordListener);
        this.mPresenter.setRecordView(this.mRecordView);
        builder.setView(this.mRecordView);
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(getString(R.string.start_recording), null);
        AlertDialog dialog = builder.create();
        dialog.show();
        this.mPositiveButton = dialog.getButton(-1);
        this.mPositiveButton.setOnClickListener(this.mClickListener);
        setButtonStatusText();
        return dialog;
    }

    private void setButtonStatusText() {
        if (this.mPositiveButton != null) {
            int i;
            Button button = this.mPositiveButton;
            if (this.mPresenter.isRecording()) {
                i = 17039370;
            } else {
                i = R.string.start_recording;
            }
            button.setText(i);
        }
    }

    private ContentValues getGreetingValues(int duration) {
        String phoneAccountId = getArguments().getString("phone_account_id");
        ContentValues values = new ContentValues();
        values.put("phone_account_id", phoneAccountId);
        values.put("duration", Integer.valueOf(duration));
        values.put("has_content", Integer.valueOf(1));
        values.put("mime_type", "audio/amr");
        values.put("dirty", Integer.valueOf(1));
        return values;
    }

    public void onPause() {
        this.mPresenter.onPause();
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mPresenter.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        this.mPresenter.onDestroy();
        super.onDestroy();
    }
}
