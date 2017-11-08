package com.android.contacts.hap.list;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.CustomStateListDrawable;
import com.huawei.cspcommon.util.ViewUtil;

public class FavoritesStarredAdapter extends BaseAdapter {
    private static final String[] COLUMNS = new String[]{"_id", "display_name", "starred", "photo_uri", "lookup", "photo_id"};
    private boolean ifHasFrequentItem = false;
    private Context mContext;
    private Cursor mCursor;
    private boolean mIsSelectionVisible = false;
    private OnActionListener mListener;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private Uri mSelectUri;
    private OnClickListener mStarredItemClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (FavoritesStarredAdapter.this.mListener != null) {
                switch (view.getId()) {
                    case R.id.contact_starred_list_item:
                        if (view.getTag(R.id.contact_starred_data_holder) instanceof StarredItemDataHolder) {
                            StarredItemDataHolder dataHolder = (StarredItemDataHolder) view.getTag(R.id.contact_starred_data_holder);
                            FavoritesStarredAdapter.this.mListener.onContactClicked(Contacts.getLookupUri(dataHolder.contactId, dataHolder.lookupkey));
                            break;
                        }
                        return;
                    case R.id.contact_starred_detail_btn_layout:
                        if (view.getTag() instanceof StarredItemDataHolder) {
                            StarredItemDataHolder itemData = (StarredItemDataHolder) view.getTag();
                            FavoritesStarredAdapter.this.mListener.onViewContactDetail(Contacts.getLookupUri(itemData.contactId, itemData.lookupkey));
                            break;
                        }
                        return;
                }
            }
        }
    };
    private OnLongClickListener mStarredItemLongClickListener = new OnLongClickListener() {
        public boolean onLongClick(View view) {
            if (FavoritesStarredAdapter.this.mListener == null || view.getId() != R.id.contact_starred_list_item) {
                return false;
            }
            FavoritesStarredAdapter.this.mListener.onContactLongClicked(Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(((StarredItemDataHolder) view.getTag(R.id.contact_starred_data_holder)).contactId)));
            return true;
        }
    };

    public interface OnActionListener {
        void onContactClicked(Uri uri);

        void onContactLongClicked(Uri uri);

        void onViewContactDetail(Uri uri);
    }

    private static class StarredItemDataHolder {
        long contactId;
        String lookupkey;

        private StarredItemDataHolder() {
        }
    }

    private static class StarredItemViewHolder {
        ImageView actionView;
        LinearLayout actionViewLayout;
        View divider;
        TextView name;
        ImageView photo;
        ImageView photoOverlay;

        private StarredItemViewHolder() {
        }
    }

    public FavoritesStarredAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    public static String[] getStarredColumns() {
        if (!EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            return (String[]) COLUMNS.clone();
        }
        String[] columnPrivate = new String[(COLUMNS.length + 1)];
        System.arraycopy(COLUMNS, 0, columnPrivate, 0, COLUMNS.length);
        columnPrivate[COLUMNS.length] = "is_private";
        return columnPrivate;
    }

    public int getCount() {
        if (this.mCursor == null || this.mCursor.isClosed()) {
            return 0;
        }
        return this.mCursor.getCount();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public Uri getItmeContactUri(int pos) {
        if (this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(pos)) {
            return null;
        }
        return Contacts.getLookupUri((long) this.mCursor.getInt(0), this.mCursor.getString(4));
    }

    public void setSelectionVisible(boolean isVisible) {
        this.mIsSelectionVisible = isVisible;
    }

    public void setSelectUri(Uri uri) {
        this.mSelectUri = uri;
    }

    public int getSelectUriPos(Uri uri) {
        if (uri == null) {
            return -1;
        }
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Uri tmp = getItmeContactUri(i);
            if (tmp != null && uri.equals(tmp)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSelectedUri(int pos, Uri currentUri) {
        if (this.mSelectUri != null && this.mSelectUri.equals(currentUri)) {
            return true;
        }
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        StarredItemDataHolder dataHolder;
        this.mCursor.moveToPosition(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.contact_starred_list_item, parent, false);
            StarredItemViewHolder viewHolder = new StarredItemViewHolder();
            dataHolder = new StarredItemDataHolder();
            viewHolder.photo = (ImageView) convertView.findViewById(R.id.contact_starrd_photo);
            viewHolder.photoOverlay = (ImageView) convertView.findViewById(R.id.contact_starrd_photo_overlay);
            viewHolder.name = (TextView) convertView.findViewById(R.id.contact_starred_name);
            viewHolder.divider = convertView.findViewById(R.id.contact_horizontal_starred_divider);
            viewHolder.actionView = (ImageView) convertView.findViewById(R.id.contact_starred_detail_btn);
            viewHolder.actionViewLayout = (LinearLayout) convertView.findViewById(R.id.contact_starred_detail_btn_layout);
            convertView.setTag(R.id.contact_starred_view_holder, viewHolder);
            convertView.setTag(R.id.contact_starred_data_holder, dataHolder);
            convertView.setOnClickListener(this.mStarredItemClickListener);
            convertView.setOnLongClickListener(this.mStarredItemLongClickListener);
        }
        View divider = ((StarredItemViewHolder) convertView.getTag(R.id.contact_starred_view_holder)).divider;
        if (divider != null) {
            if (getCount() == position + 1 && this.ifHasFrequentItem) {
                divider.setVisibility(8);
            } else {
                divider.setVisibility(0);
            }
        }
        dataHolder = (StarredItemDataHolder) convertView.getTag(R.id.contact_starred_data_holder);
        dataHolder.contactId = this.mCursor.getLong(0);
        dataHolder.lookupkey = this.mCursor.getString(4);
        if (this.mIsSelectionVisible) {
            if (isSelectedUri(position, Contacts.getLookupUri(dataHolder.contactId, dataHolder.lookupkey))) {
                convertView.setBackgroundColor(this.mContext.getResources().getColor(R.color.split_itme_selected));
            } else {
                convertView.setBackgroundColor(0);
            }
        }
        bindListViewName(convertView);
        bindListViewPhoto(convertView);
        bindListViewAction(convertView);
        return convertView;
    }

    private void bindListViewName(View view) {
        StarredItemViewHolder viewHolder = (StarredItemViewHolder) view.getTag(R.id.contact_starred_view_holder);
        Resources res = this.mContext.getResources();
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            int paddingStart = res.getDimensionPixelSize(R.dimen.contact_list_item_content_padding_left) + res.getDimensionPixelSize(R.dimen.ContactListItemView_text_adjust_start_padding);
            viewHolder.name.setPaddingRelative(paddingStart, 0, 0, 0);
            ((MarginLayoutParams) viewHolder.divider.getLayoutParams()).setMarginStart(paddingStart);
        }
        if (Constants.isEXTRA_HUGE()) {
            viewHolder.name.setTextSize(1, 28.0f);
        }
        String name = this.mCursor.getString(1);
        if (TextUtils.isEmpty(name)) {
            name = res.getString(R.string.missing_name);
        }
        viewHolder.name.setText(name);
        CommonUtilMethods.setNameViewDirection(viewHolder.name);
    }

    private void bindListViewPhoto(View view) {
        StarredItemViewHolder viewHolder = (StarredItemViewHolder) view.getTag(R.id.contact_starred_view_holder);
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            viewHolder.photo.setVisibility(8);
            viewHolder.photoOverlay.setVisibility(8);
            return;
        }
        viewHolder.photo.setVisibility(0);
        viewHolder.photoOverlay.setVisibility(0);
        long photoId = this.mCursor.getLong(5);
        ContactPhotoManager photoManger = ContactPhotoManager.getInstance(this.mContext);
        if (photoId > 0) {
            photoManger.loadThumbnail(viewHolder.photo, photoId, false, null, 2);
            viewHolder.photoOverlay.setVisibility(0);
        } else {
            this.mRequest.displayName = this.mCursor.getString(5);
            this.mRequest.identifier = this.mCursor.getString(0);
            this.mRequest.isCircular = true;
            photoManger.loadDirectoryPhoto(viewHolder.photo, null, false, 0, this.mRequest);
            viewHolder.photoOverlay.setVisibility(8);
        }
    }

    private void bindListViewAction(View view) {
        StarredItemViewHolder viewHolder = (StarredItemViewHolder) view.getTag(R.id.contact_starred_view_holder);
        viewHolder.actionViewLayout.setTag((StarredItemDataHolder) view.getTag(R.id.contact_starred_data_holder));
        viewHolder.actionViewLayout.setOnClickListener(this.mStarredItemClickListener);
        viewHolder.actionViewLayout.setOnLongClickListener(this.mStarredItemLongClickListener);
        if (!(viewHolder.actionView.getDrawable() instanceof CustomStateListDrawable)) {
            ViewUtil.setStateListIcon(view.getContext(), viewHolder.actionView);
        }
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.mListener = listener;
    }

    public void setIfHasFrequentItem(int count) {
        if (count > 0) {
            this.ifHasFrequentItem = true;
        } else {
            this.ifHasFrequentItem = false;
        }
    }
}
