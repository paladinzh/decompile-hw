package com.android.settings;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.ActivityPicker.PickAdapter.Item;
import com.android.settings.AppWidgetLoader.ItemConstructor;
import java.util.List;

public class AppWidgetPickActivity extends ActivityPicker implements ItemConstructor<Item> {
    private int mAppWidgetId;
    private AppWidgetLoader<Item> mAppWidgetLoader;
    private AppWidgetManager mAppWidgetManager;
    List<Item> mItems;
    private PackageManager mPackageManager;

    public void onCreate(Bundle icicle) {
        this.mPackageManager = getPackageManager();
        this.mAppWidgetManager = AppWidgetManager.getInstance(this);
        this.mAppWidgetLoader = new AppWidgetLoader(this, this.mAppWidgetManager, this);
        super.onCreate(icicle);
        setResultData(0, null);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("appWidgetId")) {
            finish();
        } else {
            this.mAppWidgetId = intent.getIntExtra("appWidgetId", 0);
        }
    }

    protected List<Item> getItems() {
        this.mItems = this.mAppWidgetLoader.getItems(getIntent());
        return this.mItems;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Item createItem(Context context, AppWidgetProviderInfo info, Bundle extras) {
        CharSequence label = info.label;
        Drawable drawable = null;
        if (info.icon != 0) {
            try {
                int density = context.getResources().getDisplayMetrics().densityDpi;
                switch (density) {
                    case 160:
                        break;
                    case 213:
                        break;
                    case 240:
                        break;
                    case 320:
                        break;
                    case 480:
                        break;
                }
            } catch (NameNotFoundException e) {
                Log.w("AppWidgetPickActivity", "Can't load icon drawable 0x" + Integer.toHexString(info.icon) + " for provider: " + info.provider);
            } catch (Exception e2) {
                Log.w("AppWidgetPickActivity", "Can't load icon drawable 0x" + Integer.toHexString(info.icon) + " for provider: " + info.provider);
            }
        }
        Item item = new Item(context, label, drawable);
        item.packageName = info.provider.getPackageName();
        item.className = info.provider.getClassName();
        item.extras = extras;
        return item;
        if (drawable == null) {
            Log.w("AppWidgetPickActivity", "Can't load icon drawable 0x" + Integer.toHexString(info.icon) + " for provider: " + info.provider);
        }
        Item item2 = new Item(context, label, drawable);
        item2.packageName = info.provider.getPackageName();
        item2.className = info.provider.getClassName();
        item2.extras = extras;
        return item2;
    }

    public void onClick(DialogInterface dialog, int which) {
        Intent intent = getIntentForPosition(which);
        if (((Item) this.mItems.get(which)).extras != null) {
            setResultData(-1, intent);
        } else {
            int result;
            Bundle options = null;
            try {
                if (intent.getExtras() != null) {
                    options = intent.getExtras().getBundle("appWidgetOptions");
                }
                this.mAppWidgetManager.bindAppWidgetId(this.mAppWidgetId, intent.getComponent(), options);
                result = -1;
            } catch (IllegalArgumentException e) {
                Log.w("AppWidgetPickActivity", "onClick IllegalArgumentException:", e);
                result = 0;
            }
            setResultData(result, null);
        }
        finish();
    }

    void setResultData(int code, Intent intent) {
        Intent result = intent != null ? intent : new Intent();
        result.putExtra("appWidgetId", this.mAppWidgetId);
        setResult(code, result);
    }
}
