package com.android.contacts.gridwidget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.utils.RoundedBitmapUtils;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactWidgetService extends RemoteViewsService {
    private static final String[] COLUMNS = new String[]{"photo_uri", "data15", "_id"};
    private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{"_id", "display_name", "photo_id", "lookup"};
    private static final String[] CONTACTS_SUMMARY_PROJECTION_PRIVATE;

    static class ContactWidgetFactory implements RemoteViewsFactory {
        private static int oldOrientation = 1;
        private SoftReference<Bitmap> mAddContactBitmap;
        private int mAppWidgetId;
        private HashMap<Long, byte[]> mCache;
        private Context mContext;
        private Cursor mCursor;
        private int mCursorCount;
        private HashMap<Integer, SoftReference<Bitmap>> mDefaultCache;
        private boolean mIsKeyGuard;
        private boolean mLocaleChanged;
        private boolean mNeedRefresh;
        private RemoteViews mRemoteViews;
        Resources mResources;
        private int maxColumnWidget;
        private int maxContactsInWidget;
        private ArrayList<Long> photoIds;
        private ContactsPreferences preferences;
        private WidgetStatus widgetStatus;

        public ContactWidgetFactory(Context context, int aAppWidgetId) {
            this.mAppWidgetId = -1;
            this.mCache = new HashMap();
            this.photoIds = new ArrayList();
            this.mCursorCount = 0;
            this.maxContactsInWidget = 8;
            this.maxColumnWidget = 4;
            this.mContext = context.getApplicationContext();
            this.preferences = new ContactsPreferences(this.mContext);
            this.mAppWidgetId = aAppWidgetId;
            this.widgetStatus = WidgetStatus.getWidgetStatus();
            if (HwLog.HWDBG) {
                HwLog.d("ContactWidgetService", "ContactWidgetFactory init, mAppWidgetId -" + this.mAppWidgetId);
            }
            this.mResources = context.getResources();
            this.mDefaultCache = new HashMap();
            this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.grid_item);
            this.mRemoteViews.setDrawableParameters(R.id.thumbnail, true, 255, -1, null, -1);
            this.mRemoteViews.setTextViewText(R.id.profileName, "");
            RoundedBitmapUtils.initailize(this.mResources);
            this.maxContactsInWidget = this.mResources.getInteger(R.integer.max_favorite_widget_contacts);
            this.maxColumnWidget = this.mResources.getInteger(R.integer.favorite_widget_contacts_column);
        }

        public ContactWidgetFactory(Context context, int aAppWidgetId, boolean aIsLocaleChanged, boolean aIsKeyguard) {
            this(context, aAppWidgetId);
            this.mLocaleChanged = aIsLocaleChanged;
            this.mIsKeyGuard = aIsKeyguard;
        }

        public int getCount() {
            return this.mCursorCount;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public RemoteViews getLoadingView() {
            return this.mRemoteViews;
        }

        public RemoteViews getViewAt(int aPosition) {
            boolean moveToPosition = this.mCursor != null ? this.mCursor.moveToPosition(aPosition) : false;
            if (this.mLocaleChanged && aPosition == 0) {
                this.mLocaleChanged = false;
                this.mNeedRefresh = true;
            }
            long contactid = -1;
            RemoteViews lRemoteViews = initRemoteViewByPosition(aPosition);
            lRemoteViews.setDrawableParameters(R.id.thumbnail, true, 255, -1, null, -1);
            int currentOrientation = this.mResources.getConfiguration().orientation;
            if (oldOrientation != currentOrientation) {
                this.mAddContactBitmap = null;
                this.mDefaultCache.clear();
                oldOrientation = currentOrientation;
                RoundedBitmapUtils.initailize(this.mResources);
            }
            if (moveToPosition) {
                contactid = this.mCursor.getLong(0);
                String lookUpKey = this.mCursor.getString(3);
                String lDisplayName = this.mCursor.getString(1);
                long lPhotoId = this.mCursor.getLong(2);
                if (lDisplayName == null || lDisplayName.isEmpty()) {
                    lRemoteViews.setTextViewText(R.id.profileName, this.mContext.getString(R.string.missing_name));
                } else {
                    lRemoteViews.setTextViewText(R.id.profileName, lDisplayName);
                }
                lRemoteViews.setImageViewBitmap(R.id.thumbnail, getFavoriteItemThumbnail(lPhotoId));
                Intent contactDetailIntent = new Intent("com.android.contacts.quickcontact.QuickContactActivity");
                contactDetailIntent.putExtra("appWidgetId", this.mAppWidgetId);
                contactDetailIntent.putExtra("contact_id", contactid);
                contactDetailIntent.putExtra("lookupKey", lookUpKey);
                lRemoteViews.setOnClickFillInIntent(R.id.mainLayoutList, contactDetailIntent);
            } else {
                Intent multiSelectIntent = new Intent();
                multiSelectIntent.setAction("com.android.huawei.multiselect");
                lRemoteViews.setImageViewBitmap(R.id.thumbnail, getNormalItemThumbnail());
                lRemoteViews.setTextViewText(R.id.profileName, this.mContext.getResources().getString(R.string.add_label));
                lRemoteViews.setOnClickFillInIntent(R.id.mainLayoutList, multiSelectIntent);
            }
            if (this.widgetStatus.isEditMode() && moveToPosition) {
                lRemoteViews.setViewVisibility(R.id.deleteImage, 0);
                this.widgetStatus.setDeleteMode(true);
            } else if (!(this.widgetStatus.isEditMode() && moveToPosition)) {
                lRemoteViews.setViewVisibility(R.id.deleteImage, 8);
                this.widgetStatus.setDeleteMode(false);
            }
            if (this.widgetStatus.isDeleteMode()) {
                Intent intent = new Intent();
                intent.setAction("com.android.huawei.DELETE");
                intent.putExtra("contact_id", contactid);
                lRemoteViews.setOnClickFillInIntent(R.id.mainLayoutList, intent);
            }
            return lRemoteViews;
        }

        private RemoteViews initRemoteViewByPosition(int aPosition) {
            int i;
            if (aPosition == 0) {
                String packageName = this.mContext.getPackageName();
                if (this.mIsKeyGuard) {
                    i = R.layout.grid_item_head_lock;
                } else {
                    i = R.layout.grid_item_head;
                }
                return new RemoteViews(packageName, i);
            }
            packageName = this.mContext.getPackageName();
            if (this.mIsKeyGuard) {
                i = R.layout.grid_item_lock;
            } else {
                i = R.layout.grid_item;
            }
            return new RemoteViews(packageName, i);
        }

        private Bitmap getFavoriteItemThumbnail(long photoId) {
            Bitmap bitmap = null;
            if (photoId <= 0) {
                return getDefaultBitmap();
            }
            byte[] byteArray = (byte[]) this.mCache.get(Long.valueOf(photoId));
            if (byteArray != null) {
                new Options().inPreferredConfig = Config.ARGB_8888;
                bitmap = RoundedBitmapUtils.getCompressBitmap(this.mContext, byteArray);
                Bitmap bitmap2 = null;
                if (bitmap != null) {
                    bitmap2 = RoundedBitmapUtils.getCutBitmap(bitmap);
                    bitmap.recycle();
                    bitmap = null;
                }
                if (bitmap2 != null) {
                    bitmap = RoundedBitmapUtils.getRoundBitmap(bitmap2, 16);
                }
            }
            if (bitmap == null) {
                return getDefaultBitmap();
            }
            return bitmap;
        }

        private Bitmap getNormalItemThumbnail() {
            if (this.mAddContactBitmap != null && this.mAddContactBitmap.get() != null) {
                return (Bitmap) this.mAddContactBitmap.get();
            }
            Bitmap thumbnail = BitmapFactory.decodeResource(this.mResources, R.drawable.dial_num_3_blkpress);
            if (thumbnail == null) {
                return thumbnail;
            }
            this.mAddContactBitmap = new SoftReference(thumbnail);
            return thumbnail;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean hasStableIds() {
            return true;
        }

        public void onCreate() {
        }

        public void onDataSetChanged() {
            String sortOrder;
            String[] -get2;
            this.mCursorCount = 0;
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            this.photoIds.clear();
            String lWhereClause = "starred= 1";
            if (1 == this.preferences.getSortOrder()) {
                sortOrder = "sort_key";
            } else {
                sortOrder = "sort_key_alt";
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = Contacts.CONTENT_URI;
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                -get2 = ContactWidgetService.CONTACTS_SUMMARY_PROJECTION_PRIVATE;
            } else {
                -get2 = ContactWidgetService.CONTACTS_SUMMARY_PROJECTION;
            }
            Cursor lCursor = contentResolver.query(uri, -get2, lWhereClause, null, sortOrder + " ASC");
            if (lCursor == null || lCursor.getCount() == 0) {
                if (lCursor != null) {
                    lCursor.close();
                    lCursor = null;
                }
                this.mCursorCount = 0;
            } else {
                while (lCursor.moveToNext()) {
                    long lPhotoId = lCursor.getLong(2);
                    if (lPhotoId > 0) {
                        this.photoIds.add(Long.valueOf(lPhotoId));
                    }
                }
                if (this.photoIds.size() > 0) {
                    queryPhotos();
                }
                this.mCursorCount = lCursor.getCount();
                if (this.mNeedRefresh) {
                    this.mNeedRefresh = false;
                    this.mContext.sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
                }
            }
            this.mCursor = lCursor;
            if (this.mCursorCount < this.maxContactsInWidget) {
                this.mCursorCount = this.maxContactsInWidget;
            } else {
                this.mCursorCount = ((this.mCursorCount / this.maxColumnWidget) + 1) * this.maxColumnWidget;
            }
        }

        public void onDestroy() {
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            this.mCache.clear();
            this.mCursor = null;
            this.mCache = null;
        }

        private void queryPhotos() {
            AssetFileDescriptor assetFileDescriptor;
            Cursor cursor = null;
            StringBuilder lQueryBuilder = join(new StringBuilder("_id IN ("));
            lQueryBuilder.append(")");
            if (HwLog.HWDBG) {
                HwLog.d("ContactWidgetService", "Query : " + lQueryBuilder.toString());
            }
            InputStream inputStream;
            try {
                cursor = this.mContext.getContentResolver().query(Data.CONTENT_URI, ContactWidgetService.COLUMNS, lQueryBuilder.toString(), null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        inputStream = null;
                        assetFileDescriptor = null;
                        String imageUri = cursor.getString(0);
                        Long id = Long.valueOf(cursor.getLong(2));
                        assetFileDescriptor = this.mContext.getContentResolver().openAssetFileDescriptor(Uri.parse(imageUri), "r");
                        if (assetFileDescriptor != null) {
                            byte[] data;
                            inputStream = assetFileDescriptor.createInputStream();
                            int length = inputStream.available();
                            if (length == 0) {
                                data = cursor.getBlob(1);
                            } else {
                                data = new byte[length];
                            }
                            inputStream.read(data);
                            this.mCache.put(id, data);
                            this.photoIds.remove(id);
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (assetFileDescriptor != null) {
                            assetFileDescriptor.close();
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (Throwable e3) {
                try {
                    e3.printStackTrace();
                    if (HwLog.HWDBG) {
                        HwLog.d("ContactWidgetService", "Cache : " + this.mCache.size());
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (HwLog.HWDBG) {
                HwLog.d("ContactWidgetService", "Cache : " + this.mCache.size());
            }
        }

        private StringBuilder join(StringBuilder builder) {
            for (int i = 0; i < this.photoIds.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(this.photoIds.get(i));
            }
            return builder;
        }

        private Bitmap getDefaultBitmap() {
            Integer key = Integer.valueOf(R.drawable.contact_divider_horizontal_gray);
            SoftReference<Bitmap> defaultBm = (SoftReference) this.mDefaultCache.get(key);
            if (defaultBm != null && defaultBm.get() != null) {
                return (Bitmap) defaultBm.get();
            }
            Bitmap thumbnail = BitmapFactory.decodeResource(this.mResources, R.drawable.contact_divider_horizontal_gray);
            if (thumbnail == null) {
                return thumbnail;
            }
            this.mDefaultCache.put(key, new SoftReference(thumbnail));
            return thumbnail;
        }
    }

    static {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            CONTACTS_SUMMARY_PROJECTION_PRIVATE = new String[(CONTACTS_SUMMARY_PROJECTION.length + 1)];
            System.arraycopy(CONTACTS_SUMMARY_PROJECTION, 0, CONTACTS_SUMMARY_PROJECTION_PRIVATE, 0, CONTACTS_SUMMARY_PROJECTION.length);
            CONTACTS_SUMMARY_PROJECTION_PRIVATE[CONTACTS_SUMMARY_PROJECTION.length] = "is_private";
            return;
        }
        CONTACTS_SUMMARY_PROJECTION_PRIVATE = CONTACTS_SUMMARY_PROJECTION;
    }

    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if (intent == null) {
            HwLog.w("ContactWidgetService", "Missing INTENT");
            return null;
        }
        int lAppWidgetId = intent.getIntExtra("appWidgetId", 0);
        boolean lIsLocaleChanged = intent.getBooleanExtra("localeChanged", false);
        if (lAppWidgetId < 0) {
            HwLog.w("ContactWidgetService", "Missing EXTRA_APPWIDGET_ID!");
            return null;
        }
        return new ContactWidgetFactory(getApplicationContext(), lAppWidgetId, lIsLocaleChanged, intent.getBooleanExtra("isKeyguard", false));
    }
}
