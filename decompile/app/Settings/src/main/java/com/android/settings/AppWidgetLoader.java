package com.android.settings;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppWidgetLoader<Item extends LabelledItem> {
    private AppWidgetManager mAppWidgetManager;
    private Context mContext;
    ItemConstructor<Item> mItemConstructor;

    interface LabelledItem {
        CharSequence getLabel();
    }

    public interface ItemConstructor<Item> {
        Item createItem(Context context, AppWidgetProviderInfo appWidgetProviderInfo, Bundle bundle);
    }

    public AppWidgetLoader(Context context, AppWidgetManager appWidgetManager, ItemConstructor<Item> itemConstructor) {
        this.mContext = context;
        this.mAppWidgetManager = appWidgetManager;
        this.mItemConstructor = itemConstructor;
    }

    void putCustomAppWidgets(List<Item> items, Intent intent) {
        List customExtras = null;
        List customInfo = intent.getParcelableArrayListExtra("customInfo");
        if (customInfo == null || customInfo.size() == 0) {
            Log.i("AppWidgetAdapter", "EXTRA_CUSTOM_INFO not present.");
        } else {
            int i;
            Parcelable p;
            int customInfoSize = customInfo.size();
            for (i = 0; i < customInfoSize; i++) {
                p = (Parcelable) customInfo.get(i);
                if (p == null || !(p instanceof AppWidgetProviderInfo)) {
                    customInfo = null;
                    Log.e("AppWidgetAdapter", "error using EXTRA_CUSTOM_INFO index=" + i);
                    break;
                }
            }
            customExtras = intent.getParcelableArrayListExtra("customExtras");
            if (customExtras == null) {
                customInfo = null;
                Log.e("AppWidgetAdapter", "EXTRA_CUSTOM_INFO without EXTRA_CUSTOM_EXTRAS");
            } else {
                int customExtrasSize = customExtras.size();
                if (customInfoSize != customExtrasSize) {
                    customInfo = null;
                    customExtras = null;
                    Log.e("AppWidgetAdapter", "list size mismatch: EXTRA_CUSTOM_INFO: " + customInfoSize + " EXTRA_CUSTOM_EXTRAS: " + customExtrasSize);
                } else {
                    for (i = 0; i < customExtrasSize; i++) {
                        p = (Parcelable) customExtras.get(i);
                        if (p == null || !(p instanceof Bundle)) {
                            customInfo = null;
                            customExtras = null;
                            Log.e("AppWidgetAdapter", "error using EXTRA_CUSTOM_EXTRAS index=" + i);
                            break;
                        }
                    }
                }
            }
        }
        putAppWidgetItems(customInfo, customExtras, items, 0, true);
    }

    void putAppWidgetItems(List<AppWidgetProviderInfo> appWidgets, List<Bundle> customExtras, List<Item> items, int categoryFilter, boolean ignoreFilter) {
        if (appWidgets != null) {
            int size = appWidgets.size();
            for (int i = 0; i < size; i++) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) appWidgets.get(i);
                if (ignoreFilter || (info.widgetCategory & categoryFilter) != 0) {
                    Bundle bundle;
                    ItemConstructor itemConstructor = this.mItemConstructor;
                    Context context = this.mContext;
                    if (customExtras != null) {
                        bundle = (Bundle) customExtras.get(i);
                    } else {
                        bundle = null;
                    }
                    items.add((LabelledItem) itemConstructor.createItem(context, info, bundle));
                }
            }
        }
    }

    protected List<Item> getItems(Intent intent) {
        boolean sortCustomAppWidgets = intent.getBooleanExtra("customSort", true);
        List<Item> items = new ArrayList();
        putInstalledAppWidgets(items, intent.getIntExtra("categoryFilter", 1));
        if (sortCustomAppWidgets) {
            putCustomAppWidgets(items, intent);
        }
        Collections.sort(items, new Comparator<Item>() {
            Collator mCollator = Collator.getInstance();

            public int compare(Item lhs, Item rhs) {
                return this.mCollator.compare(lhs.getLabel(), rhs.getLabel());
            }
        });
        if (!sortCustomAppWidgets) {
            List<Item> customItems = new ArrayList();
            putCustomAppWidgets(customItems, intent);
            items.addAll(customItems);
        }
        return items;
    }

    void putInstalledAppWidgets(List<Item> items, int categoryFilter) {
        putAppWidgetItems(this.mAppWidgetManager.getInstalledProviders(categoryFilter), null, items, categoryFilter, false);
    }
}
