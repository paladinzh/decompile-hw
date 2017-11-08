package com.android.settings;

import android.content.Context;
import java.util.Observable;

public abstract class AbstractStateSaver extends Observable {
    public abstract long query(Context context, String str, long j);

    public abstract boolean update(Context context, String str, long j);
}
