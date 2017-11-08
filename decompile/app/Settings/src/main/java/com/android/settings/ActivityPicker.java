package com.android.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityPicker extends AlertActivity implements OnClickListener, OnCancelListener {
    private PickAdapter mAdapter;
    private Intent mBaseIntent;

    private static class EmptyDrawable extends Drawable {
        private final int mHeight;
        private final int mWidth;

        EmptyDrawable(int width, int height) {
            this.mWidth = width;
            this.mHeight = height;
        }

        public int getIntrinsicWidth() {
            return this.mWidth;
        }

        public int getIntrinsicHeight() {
            return this.mHeight;
        }

        public int getMinimumWidth() {
            return this.mWidth;
        }

        public int getMinimumHeight() {
            return this.mHeight;
        }

        public void draw(Canvas canvas) {
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }

        public int getOpacity() {
            return -3;
        }
    }

    private static class IconResizer {
        private final Canvas mCanvas = new Canvas();
        private final int mIconHeight;
        private final int mIconWidth;
        private final DisplayMetrics mMetrics;
        private final Rect mOldBounds = new Rect();

        public IconResizer(int width, int height, DisplayMetrics metrics) {
            this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
            this.mMetrics = metrics;
            this.mIconWidth = width;
            this.mIconHeight = height;
        }

        public Drawable createIconThumbnail(Drawable icon) {
            Drawable emptyDrawable;
            int width = this.mIconWidth;
            int height = this.mIconHeight;
            if (icon == null) {
                return new EmptyDrawable(width, height);
            }
            try {
                if (icon instanceof PaintDrawable) {
                    PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                } else if (icon instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                    if (bitmapDrawable.getBitmap().getDensity() == 0) {
                        bitmapDrawable.setTargetDensity(this.mMetrics);
                    }
                }
                int iconWidth = icon.getIntrinsicWidth();
                int iconHeight = icon.getIntrinsicHeight();
                if (iconWidth > 0 && iconHeight > 0) {
                    Bitmap thumb;
                    Canvas canvas;
                    int x;
                    int y;
                    Drawable icon2;
                    if (width < iconWidth || height < iconHeight) {
                        float ratio = ((float) iconWidth) / ((float) iconHeight);
                        if (iconWidth > iconHeight) {
                            height = (int) (((float) width) / ratio);
                        } else if (iconHeight > iconWidth) {
                            width = (int) (((float) height) * ratio);
                        }
                        thumb = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, icon.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
                        canvas = this.mCanvas;
                        canvas.setBitmap(thumb);
                        this.mOldBounds.set(icon.getBounds());
                        x = (this.mIconWidth - width) / 2;
                        y = (this.mIconHeight - height) / 2;
                        icon.setBounds(x, y, x + width, y + height);
                        icon.draw(canvas);
                        icon.setBounds(this.mOldBounds);
                        icon2 = new BitmapDrawable(thumb);
                        try {
                            ((BitmapDrawable) icon2).setTargetDensity(this.mMetrics);
                            canvas.setBitmap(null);
                            icon = icon2;
                        } catch (Throwable th) {
                            icon = icon2;
                            emptyDrawable = new EmptyDrawable(width, height);
                            return icon;
                        }
                    } else if (iconWidth < width && iconHeight < height) {
                        thumb = Bitmap.createBitmap(this.mIconWidth, this.mIconHeight, Config.ARGB_8888);
                        canvas = this.mCanvas;
                        canvas.setBitmap(thumb);
                        this.mOldBounds.set(icon.getBounds());
                        x = (width - iconWidth) / 2;
                        y = (height - iconHeight) / 2;
                        icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                        icon.draw(canvas);
                        icon.setBounds(this.mOldBounds);
                        icon2 = new BitmapDrawable(thumb);
                        ((BitmapDrawable) icon2).setTargetDensity(this.mMetrics);
                        canvas.setBitmap(null);
                        icon = icon2;
                    }
                }
            } catch (Throwable th2) {
                emptyDrawable = new EmptyDrawable(width, height);
                return icon;
            }
            return icon;
        }
    }

    protected static class PickAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final List<Item> mItems;

        public static class Item implements LabelledItem {
            protected static IconResizer sResizer;
            String className;
            Bundle extras;
            Drawable icon;
            CharSequence label;
            String packageName;

            protected IconResizer getResizer(Context context) {
                if (sResizer == null) {
                    Resources resources = context.getResources();
                    int size = (int) resources.getDimension(17104896);
                    sResizer = new IconResizer(size, size, resources.getDisplayMetrics());
                }
                return sResizer;
            }

            Item(Context context, CharSequence label, Drawable icon) {
                this.label = label;
                this.icon = getResizer(context).createIconThumbnail(icon);
            }

            Item(Context context, PackageManager pm, ResolveInfo resolveInfo) {
                this.label = resolveInfo.loadLabel(pm);
                if (this.label == null && resolveInfo.activityInfo != null) {
                    this.label = resolveInfo.activityInfo.name;
                }
                this.icon = getResizer(context).createIconThumbnail(resolveInfo.loadIcon(pm));
                this.packageName = resolveInfo.activityInfo.applicationInfo.packageName;
                this.className = resolveInfo.activityInfo.name;
            }

            Intent getIntent(Intent baseIntent) {
                Intent intent = new Intent(baseIntent);
                if (this.packageName == null || this.className == null) {
                    intent.setAction("android.intent.action.CREATE_SHORTCUT");
                    intent.putExtra("android.intent.extra.shortcut.NAME", this.label);
                } else {
                    intent.setClassName(this.packageName, this.className);
                    if (this.extras != null) {
                        intent.putExtras(this.extras);
                    }
                }
                return intent;
            }

            public CharSequence getLabel() {
                return this.label;
            }
        }

        public PickAdapter(Context context, List<Item> items) {
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mItems = items;
        }

        public int getCount() {
            return this.mItems.size();
        }

        public Object getItem(int position) {
            return this.mItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(2130968902, parent, false);
            }
            Item item = (Item) getItem(position);
            TextView textView = (TextView) convertView;
            textView.setText(item.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Parcelable parcel = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (parcel instanceof Intent) {
            this.mBaseIntent = (Intent) parcel;
        } else {
            this.mBaseIntent = new Intent("android.intent.action.MAIN", null);
            this.mBaseIntent.addCategory("android.intent.category.DEFAULT");
        }
        AlertParams params = this.mAlertParams;
        params.mOnClickListener = this;
        params.mOnCancelListener = this;
        if (intent.hasExtra("android.intent.extra.TITLE")) {
            params.mTitle = intent.getStringExtra("android.intent.extra.TITLE");
        } else {
            params.mTitle = getTitle();
        }
        this.mAdapter = new PickAdapter(this, getItems());
        params.mAdapter = this.mAdapter;
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        setResult(-1, getIntentForPosition(which));
        finish();
    }

    public void onCancel(DialogInterface dialog) {
        setResult(0);
        finish();
    }

    protected Intent getIntentForPosition(int position) {
        return ((Item) this.mAdapter.getItem(position)).getIntent(this.mBaseIntent);
    }

    protected List<Item> getItems() {
        PackageManager packageManager = getPackageManager();
        List<Item> items = new ArrayList();
        Intent intent = getIntent();
        ArrayList<String> labels = intent.getStringArrayListExtra("android.intent.extra.shortcut.NAME");
        ArrayList<ShortcutIconResource> icons = intent.getParcelableArrayListExtra("android.intent.extra.shortcut.ICON_RESOURCE");
        if (!(labels == null || icons == null || labels.size() != icons.size())) {
            for (int i = 0; i < labels.size(); i++) {
                CharSequence label = (String) labels.get(i);
                Drawable icon = null;
                try {
                    ShortcutIconResource iconResource = (ShortcutIconResource) icons.get(i);
                    Resources res = packageManager.getResourcesForApplication(iconResource.packageName);
                    icon = res.getDrawable(res.getIdentifier(iconResource.resourceName, null, null), null);
                } catch (NameNotFoundException e) {
                    Log.w("ActivityPicker", "getItems NameNotFoundException:", e);
                }
                items.add(new Item((Context) this, label, icon));
            }
        }
        if (this.mBaseIntent != null) {
            putIntentItems(this.mBaseIntent, items);
        }
        return items;
    }

    protected void putIntentItems(Intent baseIntent, List<Item> items) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(baseIntent, 0);
        Collections.sort(list, new DisplayNameComparator(packageManager));
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            items.add(new Item((Context) this, packageManager, (ResolveInfo) list.get(i)));
        }
    }
}
