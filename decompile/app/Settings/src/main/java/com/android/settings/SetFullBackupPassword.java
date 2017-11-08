package com.android.settings;

import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class SetFullBackupPassword extends SettingsDrawerActivity {
    IBackupManager mBackupManager;
    OnClickListener mButtonListener = new OnClickListener() {
        public void onClick(View v) {
            if (v == SetFullBackupPassword.this.mSet) {
                String curPw = SetFullBackupPassword.this.mCurrentPw.getText().toString();
                String newPw = SetFullBackupPassword.this.mNewPw.getText().toString();
                if (!newPw.equals(SetFullBackupPassword.this.mConfirmNewPw.getText().toString())) {
                    Toast.makeText(SetFullBackupPassword.this, SetFullBackupPassword.this.getResources().getString(2131627351), 1).show();
                } else if (SetFullBackupPassword.this.setBackupPassword(curPw, newPw)) {
                    Toast.makeText(SetFullBackupPassword.this, SetFullBackupPassword.this.getResources().getString(2131627348), 1).show();
                    SetFullBackupPassword.this.finish();
                } else {
                    Toast.makeText(SetFullBackupPassword.this, SetFullBackupPassword.this.getResources().getString(2131627347), 1).show();
                }
            } else if (v == SetFullBackupPassword.this.mCancel) {
                SetFullBackupPassword.this.finish();
            } else {
                Log.w("SetFullBackupPassword", "Click on unknown view");
            }
        }
    };
    Button mCancel;
    TextView mConfirmNewPw;
    TextView mCurrentPw;
    TextView mNewPw;
    Button mSet;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
        setContentView(2130969098);
        this.mCurrentPw = (TextView) findViewById(2131887142);
        this.mNewPw = (TextView) findViewById(2131887144);
        this.mConfirmNewPw = (TextView) findViewById(2131887146);
        this.mCancel = (Button) findViewById(2131887147);
        this.mSet = (Button) findViewById(2131887148);
        this.mCancel.setOnClickListener(this.mButtonListener);
        this.mSet.setOnClickListener(this.mButtonListener);
    }

    private boolean setBackupPassword(String currentPw, String newPw) {
        try {
            return this.mBackupManager.setBackupPassword(currentPw, newPw);
        } catch (RemoteException e) {
            Log.e("SetFullBackupPassword", "Unable to communicate with backup manager");
            return false;
        }
    }
}
