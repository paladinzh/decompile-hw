package com.huawei.systemmanager.antivirus.ui;

import android.os.Bundle;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class NetQinActivity extends HsmActivity {
    private static final String NET_QIN_LINK = "http://nqms-hw-en.nq.com/nqhw/nqhwindex.html";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_qin_activity);
        ((TextView) findViewById(R.id.net_qin_link)).setText(NET_QIN_LINK);
    }
}
