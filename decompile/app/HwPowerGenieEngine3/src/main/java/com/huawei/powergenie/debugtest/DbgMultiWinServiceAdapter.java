package com.huawei.powergenie.debugtest;

import android.util.Log;
import com.huawei.powergenie.integration.adapter.MultiWinServiceAdapter;
import java.io.PrintWriter;

public class DbgMultiWinServiceAdapter extends DbgBaseAdapter {
    protected void startTest(PrintWriter pw) {
        boolean z;
        super.startTest(pw);
        Log.i("DbgMultiWinServiceAdapter", "MultiWinService Adapter Test!");
        pw.println("\nMultiWinService Adapter Test!");
        int result = MultiWinServiceAdapter.getMWMaintained();
        String str = "getMWMaintained";
        StringBuilder stringBuilder = new StringBuilder();
        if (result != -1) {
            z = true;
        } else {
            z = false;
        }
        printlnResult(str, stringBuilder.append(getResult(z)).append(", MultiWinstate :").append(result).toString());
    }
}
