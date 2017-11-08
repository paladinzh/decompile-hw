package com.huawei.systemmanager.applock.utils.compatibility;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.huawei.permissionmanager.utils.ShareCfg;

public class EditTextUtil {
    public static void disableCopyAndPaste(EditText editText) {
        editText.setLongClickable(false);
        editText.setImeOptions(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        editText.setCustomSelectionActionModeCallback(new Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
    }
}
