package com.huawei.permissionmanager.ui.history;

import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import com.huawei.permissionmanager.ui.history.PermissionHistoryFragment.DataLoader;
import com.huawei.systemmanager.R;
import java.util.List;

public class PermissionForbiddenHistoryFragment extends PermissionHistoryFragment {
    public Loader<List<HistoryItem>> onCreateLoader(int id, Bundle args) {
        return new DataLoader(getApplicationContext(), 10);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        setEmptyTextAndImage(R.string.permission_noforbidden_history, R.drawable.ic_no_apps);
        super.onViewCreated(view, savedInstanceState);
    }
}
