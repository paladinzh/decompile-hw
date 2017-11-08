package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;
import com.huawei.systemmanager.util.HwLog;

public class EntryHarassment extends SimpleEntrace {
    public static final String NAME = "EntryHarassment";
    private Runnable mLoadDataTask = new Runnable() {
        public void run() {
            Context ctx = EntryHarassment.this.getContext();
            if (ctx == null) {
                HwLog.e(EntryHarassment.NAME, "mLoadDataTask ctx is null!");
                return;
            }
            EntryHarassment.this.mTipView.postSetNumber(DBAdapter.getUnreadCallCount(ctx) + DBAdapter.getUnreadMsgCount(ctx));
        }
    };
    private final TipView mTipView = new TipView();

    public void onResume() {
        refreshData();
    }

    protected void onCreateView(View container) {
        this.mTipView.setTipTextView((TextView) container.findViewById(R.id.tip));
        Utility.setViewEnabled(container, Utility.isOwnerUser(false));
    }

    protected int getIconResId() {
        return R.drawable.ic_block_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_blocklist;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_HARASSMENT;
    }

    public String getEntryName() {
        return NAME;
    }

    public void refreshData() {
        SERIAL_EXECUTOR.execute(this.mLoadDataTask);
    }
}
