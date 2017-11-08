package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.module.IHsmModule;
import java.util.concurrent.Executor;

public abstract class AbsEntrance {
    protected static final Executor SERIAL_EXECUTOR = new HsmSingleExecutor();
    protected Context mCtx;

    public static abstract class SimpleEntrace extends AbsEntrance {
        protected ViewGroup mContainer;

        public boolean isEnable(Context ctx) {
            IHsmModule module = getModule();
            return module == null ? false : module.entryEnabled(ctx);
        }

        public Intent getEntryIntent(Context ctx) {
            IHsmModule module = getModule();
            if (module == null) {
                return null;
            }
            return module.getMainEntry(ctx);
        }

        public View createView(LayoutInflater inflater, int position, ViewGroup parent) {
            View view = inflater.inflate(R.layout.main_screen_entry_normal_item, parent, false);
            int iconResId = getIconResId();
            if (iconResId != 0) {
                ((ImageView) view.findViewById(R.id.icon)).setImageResource(iconResId);
            }
            int titleResId = getTitleStringId();
            if (titleResId != 0) {
                ((TextView) view.findViewById(R.id.title)).setText(titleResId);
                Context ctx = GlobalContext.getContext();
                if (ctx != null) {
                    view.setContentDescription(ctx.getString(titleResId));
                }
            }
            this.mContainer = (ViewGroup) view;
            view.setTag(R.id.convertview_tag_item, this);
            onCreateView(this.mContainer);
            return view;
        }

        protected void onCreateView(View container) {
        }

        protected IHsmModule getModule() {
            return null;
        }

        protected int getIconResId() {
            return 0;
        }

        protected int getTitleStringId() {
            return 0;
        }
    }

    public abstract View createView(LayoutInflater layoutInflater, int i, ViewGroup viewGroup);

    public abstract String getEntryName();

    public abstract boolean isEnable(Context context);

    public void onCreate(Context ctx) {
        this.mCtx = ctx;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestory() {
    }

    public void refreshData() {
    }

    public Intent getEntryIntent(Context ctx) {
        return null;
    }

    protected Context getContext() {
        return this.mCtx;
    }
}
