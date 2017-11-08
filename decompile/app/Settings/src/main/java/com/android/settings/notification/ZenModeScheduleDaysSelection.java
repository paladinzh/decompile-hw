package com.android.settings.notification;

import android.util.SparseBooleanArray;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;
import java.util.Arrays;

public class ZenModeScheduleDaysSelection extends ScrollView {
    public static final int[] CHINA_DAYS = new int[]{2, 3, 4, 5, 6, 7, 1};
    public static final int[] DAYS = new int[]{1, 2, 3, 4, 5, 6, 7};
    private final SparseBooleanArray mDays;

    /* renamed from: com.android.settings.notification.ZenModeScheduleDaysSelection$1 */
    class AnonymousClass1 implements OnCheckedChangeListener {
        final /* synthetic */ ZenModeScheduleDaysSelection this$0;
        final /* synthetic */ int val$day;

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            this.this$0.mDays.put(this.val$day, isChecked);
            this.this$0.onChanged(this.this$0.getDays());
        }
    }

    private int[] getDays() {
        int i;
        SparseBooleanArray rt = new SparseBooleanArray(this.mDays.size());
        for (i = 0; i < this.mDays.size(); i++) {
            int day = this.mDays.keyAt(i);
            if (this.mDays.valueAt(i)) {
                rt.put(day, true);
            }
        }
        int[] rta = new int[rt.size()];
        for (i = 0; i < rta.length; i++) {
            rta[i] = rt.keyAt(i);
        }
        Arrays.sort(rta);
        return rta;
    }

    protected void onChanged(int[] days) {
    }
}
