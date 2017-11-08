package com.huawei.powergenie.debugtest;

import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.integration.eventhub.EventHubFactory;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbgTestService extends BaseService {
    private List<DbgBaseAdapter> mDbgAdapterList = null;
    private DbgEventHub mDbgEventHub;
    private ICoreContext mICoreContext;

    public DbgTestService(ICoreContext context) {
        this.mICoreContext = context;
    }

    public void start() {
        this.mDbgAdapterList = new ArrayList<DbgBaseAdapter>() {
            {
                add(new DbgNativeAdapter());
                add(new DbgAlarmAdapter(DbgTestService.this.mICoreContext.getContext()));
                add(new DbgCommonAdapter(DbgTestService.this.mICoreContext.getContext()));
                add(new DbgMultiWinServiceAdapter());
                add(new DbgHardwareAdapter(DbgTestService.this.mICoreContext));
                add(new DbgPGManagerAdapter());
                add(new DbgPgedBinderAdapter());
            }
        };
        this.mDbgEventHub = new DbgEventHub();
        EventHubFactory.startAllEventHubs(this.mICoreContext.getContext(), this.mDbgEventHub);
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("\nPOWER GENIUS (dumpsys powergenius integration)");
        List<String> argList = Arrays.asList(args);
        if (args.length == 1) {
            dumpAdapter(pw);
            this.mDbgEventHub.dump(pw, "all");
        } else if (argList.contains("adapter")) {
            dumpAdapter(pw);
        } else if (argList.contains("event")) {
            this.mDbgEventHub.dump(pw, "event");
        } else if (argList.contains("msg")) {
            this.mDbgEventHub.dump(pw, "msg");
        }
    }

    private void dumpAdapter(PrintWriter pw) {
        for (DbgBaseAdapter dbg : this.mDbgAdapterList) {
            dbg.startTest(pw);
        }
    }
}
