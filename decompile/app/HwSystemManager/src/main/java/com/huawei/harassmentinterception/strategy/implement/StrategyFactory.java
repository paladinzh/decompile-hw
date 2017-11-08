package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;

public interface StrategyFactory {
    AbsStrategy create(Context context, ContentValues contentValues, int i, int i2);
}
