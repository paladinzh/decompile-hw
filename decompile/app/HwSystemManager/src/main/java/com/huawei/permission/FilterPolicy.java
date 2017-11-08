package com.huawei.permission;

import android.content.Context;
import com.huawei.permission.filter.ProfileTestEnvFilter;
import java.util.ArrayList;

public class FilterPolicy {
    private ArrayList<Filter> mFilters = new ArrayList();

    public static class Filter {
        public FilterResult filterOperation(int uid, int pid, int type) {
            return new FilterResult(false, 1);
        }
    }

    public static class FilterResult {
        private boolean mBreak;
        private int mOpration;

        public FilterResult(boolean brk, int op) {
            this.mBreak = brk;
            this.mOpration = op;
        }

        public boolean shouldBreak() {
            return this.mBreak;
        }

        public int getOperationWhenBreak() {
            return this.mOpration;
        }
    }

    public FilterPolicy(Context context) {
        this.mFilters.add(new ProfileTestEnvFilter(context));
    }

    public FilterResult filterOperation(int uid, int pid, int type) {
        for (Filter f : this.mFilters) {
            FilterResult fr = f.filterOperation(uid, pid, type);
            if (fr.shouldBreak()) {
                return fr;
            }
        }
        return new FilterResult(false, 1);
    }
}
