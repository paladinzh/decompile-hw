package com.huawei.systemmanager.power.model;

import com.huawei.systemmanager.comm.component.AppItem;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class UnifiedPowerAppItem extends AppItem {
    public static final AlpComparator<UnifiedPowerAppItem> PROTECT_APP_COMPARATOR = new AlpComparator<UnifiedPowerAppItem>() {
        public String getStringKey(UnifiedPowerAppItem t) {
            return t.getName();
        }

        public int compare(UnifiedPowerAppItem lhs, UnifiedPowerAppItem rhs) {
            int i = -1;
            boolean isLeftProtected = lhs.isProtect();
            if ((isLeftProtected ^ rhs.isProtect()) != 0) {
                if (!isLeftProtected) {
                    i = 1;
                }
                return i;
            }
            boolean isLeftPowerCost = lhs.isHighPower();
            if ((isLeftPowerCost ^ rhs.isHighPower()) == 0) {
                return super.compare(lhs, rhs);
            }
            if (!isLeftPowerCost) {
                i = 1;
            }
            return i;
        }
    };
    protected boolean mHighPower;
    protected boolean mProtected;

    protected UnifiedPowerAppItem() {
    }

    protected UnifiedPowerAppItem(HsmPkgInfo info) {
        super(info);
    }

    public UnifiedPowerAppItem(HsmPkgInfo info, boolean protect, boolean highPower) {
        super(info);
        this.mProtected = protect;
        this.mHighPower = highPower;
    }

    public void setProtect(boolean isProtect) {
        this.mProtected = isProtect;
    }

    public boolean isProtect() {
        return this.mProtected;
    }

    public boolean isHighPower() {
        return this.mHighPower;
    }
}
