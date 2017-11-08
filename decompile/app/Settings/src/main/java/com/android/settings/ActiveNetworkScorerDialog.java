package com.android.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppManager;
import android.net.NetworkScorerAppManager.NetworkScorerAppData;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;

public final class ActiveNetworkScorerDialog extends AlertActivity implements OnClickListener {
    private String mNewPackageName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNewPackageName = getIntent().getStringExtra("packageName");
        if (!buildDialog()) {
            finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                if (((NetworkScoreManager) getSystemService("network_score")).setActiveScorer(this.mNewPackageName)) {
                    setResult(-1);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private boolean buildDialog() {
        if (UserHandle.myUserId() != 0) {
            Log.i("ActiveNetScorerDlg", "Can only set scorer for owner/system user.");
            return false;
        }
        NetworkScorerAppData newScorer = NetworkScorerAppManager.getScorer(this, this.mNewPackageName);
        if (newScorer == null) {
            Log.e("ActiveNetScorerDlg", "New package " + this.mNewPackageName + " is not a valid scorer.");
            return false;
        }
        NetworkScorerAppData oldScorer = NetworkScorerAppManager.getActiveScorer(this);
        if (oldScorer == null || !TextUtils.equals(oldScorer.mPackageName, this.mNewPackageName)) {
            CharSequence newName = newScorer.mScorerName;
            AlertParams p = this.mAlertParams;
            p.mTitle = getString(2131625459);
            if (oldScorer != null) {
                p.mMessage = getString(2131625460, new Object[]{newName, oldScorer.mScorerName});
            } else {
                p.mMessage = getString(2131625461, new Object[]{newName});
            }
            p.mPositiveButtonText = getString(2131624348);
            p.mNegativeButtonText = getString(2131624349);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonListener = this;
            setupAlert();
            return true;
        }
        Log.i("ActiveNetScorerDlg", "New package " + this.mNewPackageName + " is already the active scorer.");
        setResult(-1);
        return false;
    }
}
