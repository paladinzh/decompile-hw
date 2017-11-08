package com.huawei.systemmanager.comm.grule.rules;

import android.content.Context;

public interface IRule<T> {
    boolean match(Context context, T t);
}
