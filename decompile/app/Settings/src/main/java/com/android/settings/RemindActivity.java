package com.android.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;

public class RemindActivity extends AlertActivity {
    private View mView;
    private TextView messageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertParams p = this.mAlertParams;
        p.mView = createConnectionDialogView();
        p.mPositiveButtonText = getString(2131629077);
        setupAlert();
    }

    private View createConnectionDialogView() {
        this.mView = getLayoutInflater().inflate(2130969050, null);
        this.messageView = (TextView) this.mView.findViewById(2131886296);
        this.messageView.setText(getString(2131629076));
        return this.mView;
    }
}
