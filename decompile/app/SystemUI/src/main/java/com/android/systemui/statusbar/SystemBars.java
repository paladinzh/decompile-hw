package com.android.systemui.statusbar;

import android.content.res.Configuration;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.ServiceMonitor.Callbacks;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemBars extends SystemUI implements Callbacks {
    private ServiceMonitor mServiceMonitor;
    private BaseStatusBar mStatusBar;

    public void start() {
        this.mServiceMonitor = new ServiceMonitor("SystemBars", false, this.mContext, "bar_service_component", this);
        this.mServiceMonitor.start();
    }

    public void onNoService() {
        createStatusBarFromConfig();
    }

    public long onServiceStartAttempt() {
        if (this.mStatusBar == null) {
            return 0;
        }
        this.mStatusBar.destroy();
        this.mStatusBar = null;
        return 500;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (this.mStatusBar != null) {
            this.mStatusBar.onConfigurationChanged(newConfig);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mStatusBar != null) {
            this.mStatusBar.dump(fd, pw, args);
        }
    }

    private void createStatusBarFromConfig() {
        RuntimeException andLog;
        String clsName = this.mContext.getString(R.string.config_statusBarComponent);
        if (clsName == null || clsName.length() == 0) {
            throw andLog("No status bar component configured", null);
        }
        try {
            try {
                this.mStatusBar = (BaseStatusBar) this.mContext.getClassLoader().loadClass(clsName).newInstance();
                this.mStatusBar.mContext = this.mContext;
                this.mStatusBar.mComponents = this.mComponents;
                this.mStatusBar.start();
            } catch (Throwable t) {
                andLog = andLog("Error creating status bar component: " + clsName, t);
            }
        } catch (Throwable t2) {
            andLog = andLog("Error loading status bar component: " + clsName, t2);
        }
    }

    private RuntimeException andLog(String msg, Throwable t) {
        Log.w("SystemBars", msg, t);
        throw new RuntimeException(msg, t);
    }
}
