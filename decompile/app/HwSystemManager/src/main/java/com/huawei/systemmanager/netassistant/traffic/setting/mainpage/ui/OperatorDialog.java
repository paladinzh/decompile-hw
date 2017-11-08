package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.ICodeName;
import java.util.List;

public class OperatorDialog extends AlertDialog {
    protected OperatorDialog(Context context) {
        super(context);
    }

    public void showDialog(List<ICodeName> list, int titleRes, OnClickListener listener, int select) {
        String[] nItems = new String[list.size()];
        for (int i = 0; i < nItems.length; i++) {
            nItems[i] = ((ICodeName) list.get(i)).getName();
        }
        Builder ab = new Builder(getContext());
        ab.setTitle(titleRes);
        ab.setSingleChoiceItems(nItems, select, listener);
        ab.setNegativeButton(R.string.common_cancel, null);
        ab.create().show();
    }
}
