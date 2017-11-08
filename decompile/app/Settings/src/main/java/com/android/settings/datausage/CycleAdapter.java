package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStatsHistory.Entry;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import com.android.settings.Utils;
import com.android.settingslib.net.ChartData;
import libcore.util.Objects;

public class CycleAdapter extends ArrayAdapter<CycleItem> {
    private final OnItemSelectedListener mListener;
    private final SpinnerInterface mSpinner;

    public static class CycleItem implements Comparable<CycleItem> {
        public long end;
        public CharSequence label;
        public long start;

        public CycleItem(Context context, long start, long end) {
            this.label = Utils.formatDateRange(context, start, end);
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return this.label.toString();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof CycleItem)) {
                return false;
            }
            CycleItem another = (CycleItem) o;
            if (this.start == another.start && this.end == another.end) {
                z = true;
            }
            return z;
        }

        public int compareTo(CycleItem another) {
            return Long.compare(this.start, another.start);
        }
    }

    public interface SpinnerInterface {
        Object getSelectedItem();

        void setAdapter(CycleAdapter cycleAdapter);

        void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener);

        void setSelection(int i);
    }

    public CycleAdapter(Context context, SpinnerInterface spinner, OnItemSelectedListener listener, boolean isHeader) {
        int i;
        if (isHeader) {
            i = 2130968779;
        } else {
            i = 2130968722;
        }
        super(context, i);
        setDropDownViewResource(17367049);
        this.mSpinner = spinner;
        this.mListener = listener;
        this.mSpinner.setAdapter(this);
        this.mSpinner.setOnItemSelectedListener(this.mListener);
    }

    public int findNearestPosition(CycleItem target) {
        if (target != null) {
            for (int i = getCount() - 1; i >= 0; i--) {
                if (((CycleItem) getItem(i)).compareTo(target) >= 0) {
                    return i;
                }
            }
        }
        return 0;
    }

    public boolean updateCycleList(NetworkPolicy policy, ChartData chartData) {
        long cycleEnd;
        long cycleStart;
        boolean includeCycle;
        CycleItem previousItem = (CycleItem) this.mSpinner.getSelectedItem();
        clear();
        Context context = getContext();
        Entry entry = null;
        long historyStart = Long.MAX_VALUE;
        long historyEnd = Long.MIN_VALUE;
        if (chartData != null) {
            historyStart = chartData.network.getStart();
            historyEnd = chartData.network.getEnd();
        }
        long now = System.currentTimeMillis();
        if (historyStart == Long.MAX_VALUE) {
            historyStart = now;
        }
        if (historyEnd == Long.MIN_VALUE) {
            historyEnd = now + 1;
        }
        boolean hasCycles = false;
        if (policy != null) {
            cycleEnd = NetworkPolicyManager.computeNextCycleBoundary(historyEnd, policy);
            while (cycleEnd > historyStart) {
                cycleStart = NetworkPolicyManager.computeLastCycleBoundary(cycleEnd, policy);
                if (chartData != null) {
                    entry = chartData.network.getValues(cycleStart, cycleEnd, entry);
                    includeCycle = entry.rxBytes + entry.txBytes > 0;
                } else {
                    includeCycle = true;
                }
                if (includeCycle) {
                    add(new CycleItem(context, cycleStart, cycleEnd));
                    hasCycles = true;
                }
                cycleEnd = cycleStart;
            }
        }
        if (!hasCycles) {
            cycleEnd = historyEnd;
            while (cycleEnd > historyStart) {
                cycleStart = cycleEnd - 2419200000L;
                if (chartData != null) {
                    entry = chartData.network.getValues(cycleStart, cycleEnd, entry);
                    includeCycle = entry.rxBytes + entry.txBytes > 0;
                } else {
                    includeCycle = true;
                }
                if (includeCycle) {
                    add(new CycleItem(context, cycleStart, cycleEnd));
                }
                cycleEnd = cycleStart;
            }
        }
        if (getCount() > 0) {
            int position = findNearestPosition(previousItem);
            this.mSpinner.setSelection(position);
            if (!Objects.equal((CycleItem) getItem(position), previousItem)) {
                this.mListener.onItemSelected(null, null, position, 0);
                return false;
            }
        }
        return true;
    }
}
