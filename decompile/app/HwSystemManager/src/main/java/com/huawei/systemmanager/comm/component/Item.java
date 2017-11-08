package com.huawei.systemmanager.comm.component;

import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Item {

    public interface CheckItem extends Item {
        boolean isCheckable();

        boolean isChecked();

        void setChecked(boolean z);

        void toggle();
    }

    public static class SimpleItem implements CheckItem {
        private AtomicBoolean mChecked = new AtomicBoolean(false);

        public String getName() {
            return "";
        }

        public String getDescription(Context ctx) {
            return "";
        }

        public boolean isCheckable() {
            return true;
        }

        public void setChecked(boolean checked) {
            this.mChecked.getAndSet(checked);
        }

        public boolean isChecked() {
            return this.mChecked.get();
        }

        public void toggle() {
            this.mChecked.set(!this.mChecked.get());
        }
    }

    String getDescription(Context context);

    String getName();
}
