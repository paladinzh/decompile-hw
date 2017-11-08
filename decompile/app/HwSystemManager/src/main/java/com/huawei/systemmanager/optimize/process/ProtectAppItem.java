package com.huawei.systemmanager.optimize.process;

import com.huawei.systemmanager.comm.component.AppItem;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class ProtectAppItem extends AppItem {
    public static final AlpComparator<ProtectAppItem> PROTECT_APP_COMPARATOR = new AlpComparator<ProtectAppItem>() {
        public String getStringKey(ProtectAppItem t) {
            return t.getName();
        }

        public int compare(ProtectAppItem lhs, ProtectAppItem rhs) {
            int i = -1;
            boolean isLeftProtected = lhs.isProtect();
            boolean isRightProtected = rhs.isProtect();
            if (AbroadUtils.isAbroad()) {
                if ((isLeftProtected ^ isRightProtected) != 0) {
                    if (isLeftProtected) {
                        i = 1;
                    }
                    return i;
                }
            } else if ((isLeftProtected ^ isRightProtected) != 0) {
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
    public static final AlpComparator<ProtectAppItem> WHITE_LIST_COMPARATOR = new AlpComparator<ProtectAppItem>() {
        public String getStringKey(ProtectAppItem t) {
            return t.getName();
        }

        public int compare(ProtectAppItem lhs, ProtectAppItem rhs) {
            boolean isLeftProtected = lhs.isProtect();
            if ((isLeftProtected ^ rhs.isProtect()) == 0) {
                return super.compare(lhs, rhs);
            }
            return isLeftProtected ? -1 : 1;
        }
    };
    protected boolean mHighPower;
    protected boolean mProtected;

    protected ProtectAppItem() {
    }

    protected ProtectAppItem(HsmPkgInfo info) {
        super(info);
    }

    public ProtectAppItem(HsmPkgInfo info, boolean protect, boolean highPower) {
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
