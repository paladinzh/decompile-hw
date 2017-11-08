package com.android.contacts.hap.list;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
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
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyDataListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyManager;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.CustomStateListDrawable;
import com.huawei.cspcommon.util.ViewUtil;

public class FavoritesFrequentAdapter extends BaseAdapter {
    private static final String[] COLUMNS = new String[]{"_id", "display_name", "starred", "photo_id", "photo_uri", "lookup", "data1", "data2", "data3", "contact_id", "data4"};
    RoamingDialPadDirectlyDataListener favoritesFrequentAdapterListener = new RoamingDialPadDirectlyDataListener() {
        public void selectedDirectlyData(String number) {
            if (!TextUtils.isEmpty(number)) {
                FavoritesFrequentAdapter.this.mListener.onCallNumber(number);
            }
        }
    };
    private OnClickListener frequentItemActionListener = new OnClickListener() {
        public void onClick(View view) {
            if (FavoritesFrequentAdapter.this.mListener != null) {
                int viewId = view.getId();
                if (viewId == R.id.contact_frequent_detail_btn_layout) {
                    if (view.getTag() instanceof FrequentItemDataHolder) {
                        FrequentItemDataHolder itemData = (FrequentItemDataHolder) view.getTag();
                        FavoritesFrequentAdapter.this.mListener.onViewContact(Contacts.getLookupUri(itemData.contactId, itemData.lookupkey), itemData.number);
                        StatisticalHelper.report(2019);
                    }
                } else if (viewId == R.id.contact_frequent_list_item) {
                    String number = RoamingDialPadDirectlyManager.getRoamingDialNumber(FavoritesFrequentAdapter.this.mContext, PhoneNumberFormatter.parsePhoneNumber(((FrequentItemViewHolder) view.getTag()).number.getText().toString()), ((FrequentItemViewHolder) view.getTag()).normalized_number, FavoritesFrequentAdapter.this.favoritesFrequentAdapterListener);
                    if (CommonUtilMethods.calcIfNeedSplitScreen() && !TextUtils.isEmpty(number)) {
                        FavoritesFrequentAdapter.this.mListener.onItmeClick(((FrequentItemViewHolder) view.getTag()).contactUri, ((FrequentItemViewHolder) view.getTag()).column_number);
                    }
                    if (!TextUtils.isEmpty(number)) {
                        FavoritesFrequentAdapter.this.mListener.onCallNumber(number);
                        StatisticalHelper.report(2018);
                    }
                }
            }
        }
    };
    private Context mContext;
    private Cursor mCursor;
    private OnLongClickListener mFrequentItemLongClickedListener = new OnLongClickListener() {
        public boolean onLongClick(View view) {
            if (!(view.getTag() instanceof FrequentItemViewHolder)) {
                return false;
            }
            FrequentItemViewHolder holder = (FrequentItemViewHolder) view.getTag();
            if (!(holder.actionViewLayout.getTag() instanceof FrequentItemDataHolder)) {
                return false;
            }
            FavoritesFrequentAdapter.this.mListener.onViewLongClicked(Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(((FrequentItemDataHolder) holder.actionViewLayout.getTag()).dataId)));
            return true;
        }
    };
    private boolean mIsSelectionVisible = false;
    private ItemActionListener mListener;
    private ContactPhotoManager mPhotoManager;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private String mSelectNumber;
    private Uri mSelectUri;

    public interface ItemActionListener {
        void onCallNumber(String str);

        void onItmeClick(Uri uri, String str);

        void onViewContact(Uri uri, String str);

        void onViewLongClicked(Uri uri);
    }

    private static class FrequentItemDataHolder {
        long contactId;
        long dataId;
        String lookupkey;
        String number;

        private FrequentItemDataHolder() {
        }
    }

    private static class FrequentItemViewHolder {
        ImageView actionView;
        LinearLayout actionViewLayout;
        String column_number;
        Uri contactUri;
        View divider;
        ViewGroup infoDetail;
        TextView name;
        String normalized_number;
        TextView number;
        TextView phoneLabel;
        ImageView photo;
        ImageView photoOverlay;

        private FrequentItemViewHolder() {
        }
    }

    public FavoritesFrequentAdapter(Context context) {
        this.mContext = context;
    }

    public static String[] getFrequentColumns() {
        return (String[]) COLUMNS.clone();
    }

    public int getCount() {
        if (this.mCursor == null || this.mCursor.isClosed()) {
            return 0;
        }
        return this.mCursor.getCount();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public Uri getItmeContactUri(int pos) {
        if (this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(pos)) {
            return null;
        }
        return Contacts.getLookupUri((long) this.mCursor.getInt(9), this.mCursor.getString(5));
    }

    public String getItmeContactNmuber(int pos) {
        if (this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(pos)) {
            return null;
        }
        return this.mCursor.getString(6);
    }

    public void setSelectUri(Uri uri, String number) {
        this.mSelectNumber = number;
        this.mSelectUri = uri;
    }

    public void setSelectionVisible(boolean isVisible) {
        this.mIsSelectionVisible = isVisible;
    }

    public int getSelectUriPos(Uri uri, String number) {
        if (uri == null || number == null) {
            return -1;
        }
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (isSelectedUri(i, uri, number)) {
                return i;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSelectedUri(int pos, Uri selectUri, String number) {
        if (!(selectUri == null || number == null || this.mCursor == null || this.mCursor.isClosed() || !this.mCursor.moveToPosition(pos))) {
            Uri cursorUri = Contacts.getLookupUri((long) this.mCursor.getInt(9), this.mCursor.getString(5));
            String cursorNum = this.mCursor.getString(6);
            if (selectUri.equals(cursorUri) && number.equals(cursorNum)) {
                return true;
            }
        }
        return false;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        FrequentItemViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.contact_frequent_item, parent, false);
            holder = new FrequentItemViewHolder();
            holder.photo = (ImageView) convertView.findViewById(R.id.contact_frequent_photo);
            holder.photoOverlay = (ImageView) convertView.findViewById(R.id.contact_frequent_photo_overlay);
            holder.infoDetail = (ViewGroup) convertView.findViewById(R.id.contact_frequent_information);
            holder.name = (TextView) convertView.findViewById(R.id.contact_frequent_name);
            holder.phoneLabel = (TextView) convertView.findViewById(R.id.contact_frequent_number_label);
            holder.number = (TextView) convertView.findViewById(R.id.contact_frequent_number);
            holder.divider = convertView.findViewById(R.id.contact_frequent_divider);
            holder.actionView = (ImageView) convertView.findViewById(R.id.contact_frequent_detail_btn);
            holder.actionViewLayout = (LinearLayout) convertView.findViewById(R.id.contact_frequent_detail_btn_layout);
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                holder.column_number = getItmeContactNmuber(position);
                holder.contactUri = getItmeContactUri(position);
            }
            convertView.setTag(holder);
            convertView.setOnClickListener(this.frequentItemActionListener);
        }
        this.mCursor.moveToPosition(position);
        bindListViewPhoto(convertView);
        bindListViewName(convertView);
        bindListPhoneLabel(convertView);
        bindListPhoneNumber(convertView);
        bindListViewAction(convertView);
        if (this.mIsSelectionVisible) {
            if (isSelectedUri(position, this.mSelectUri, this.mSelectNumber)) {
                convertView.setBackgroundColor(this.mContext.getResources().getColor(R.color.split_itme_selected));
            } else {
                convertView.setBackgroundColor(0);
            }
        }
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            Resources res = this.mContext.getResources();
            int paddingStart = res.getDimensionPixelSize(R.dimen.contact_list_item_content_padding_left) + res.getDimensionPixelSize(R.dimen.ContactListItemView_text_adjust_start_padding);
            holder = (FrequentItemViewHolder) convertView.getTag();
            holder.infoDetail.setPaddingRelative(paddingStart, 0, 0, 0);
            ((MarginLayoutParams) holder.divider.getLayoutParams()).setMarginStart(paddingStart);
        }
        return convertView;
    }

    private void bindListViewName(View view) {
        FrequentItemViewHolder holder = (FrequentItemViewHolder) view.getTag();
        if (Constants.isEXTRA_HUGE()) {
            holder.name.setTextSize(1, 28.0f);
        }
        holder.name.setText(this.mCursor.getString(1));
        CommonUtilMethods.setNameViewDirection(holder.name);
    }

    private void bindListViewPhoto(View view) {
        FrequentItemViewHolder viewHolder = (FrequentItemViewHolder) view.getTag();
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            viewHolder.photo.setVisibility(8);
            viewHolder.photoOverlay.setVisibility(8);
            return;
        }
        viewHolder.photo.setVisibility(0);
        viewHolder.photoOverlay.setVisibility(0);
        long photoId = this.mCursor.getLong(this.mCursor.getColumnIndex("photo_id"));
        if (photoId > 0) {
            this.mPhotoManager.loadThumbnail(viewHolder.photo, photoId, false, null, 2);
            viewHolder.photoOverlay.setVisibility(0);
        } else {
            this.mRequest.displayName = this.mCursor.getString(1);
            this.mRequest.identifier = this.mCursor.getString(9);
            this.mRequest.isCircular = true;
            this.mPhotoManager.loadDirectoryPhoto(viewHolder.photo, null, false, 0, this.mRequest);
            viewHolder.photoOverlay.setVisibility(8);
        }
    }

    private void bindListPhoneLabel(View view) {
        ((FrequentItemViewHolder) view.getTag()).phoneLabel.setText((String) Phone.getTypeLabel(this.mContext.getResources(), this.mCursor.getInt(7), this.mCursor.getString(8)));
    }

    private void bindListPhoneNumber(View view) {
        if (view.getTag() != null && (view.getTag() instanceof FrequentItemViewHolder)) {
            FrequentItemViewHolder frequentItemViewHolder = (FrequentItemViewHolder) view.getTag();
            String number = this.mCursor.getString(6);
            if (!TextUtils.isEmpty(number)) {
                if (EmuiFeatureManager.isChinaArea()) {
                    number = ContactsUtils.getChinaFormatNumber(number);
                }
                if (frequentItemViewHolder.number != null) {
                    frequentItemViewHolder.number.setVisibility(0);
                    frequentItemViewHolder.number.setText(number);
                }
                frequentItemViewHolder.normalized_number = this.mCursor.getString(10);
            } else if (frequentItemViewHolder.number != null) {
                frequentItemViewHolder.number.setVisibility(8);
            }
        }
    }

    private void bindListViewAction(View view) {
        FrequentItemDataHolder itemData;
        FrequentItemViewHolder holder = (FrequentItemViewHolder) view.getTag();
        if (holder.actionViewLayout.getTag() instanceof FrequentItemDataHolder) {
            itemData = (FrequentItemDataHolder) holder.actionViewLayout.getTag();
        } else {
            itemData = new FrequentItemDataHolder();
            holder.actionViewLayout.setTag(itemData);
        }
        itemData.contactId = this.mCursor.getLong(9);
        itemData.lookupkey = this.mCursor.getString(5);
        itemData.number = this.mCursor.getString(6);
        itemData.dataId = this.mCursor.getLong(0);
        holder.actionViewLayout.setOnClickListener(this.frequentItemActionListener);
        view.setOnLongClickListener(this.mFrequentItemLongClickedListener);
        if (!(holder.actionView.getDrawable() instanceof CustomStateListDrawable)) {
            ViewUtil.setStateListIcon(view.getContext(), holder.actionView, false);
        }
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    public Cursor getCursor() {
        return this.mCursor;
    }

    public void setPhotoManager(ContactPhotoManager manager) {
        this.mPhotoManager = manager;
    }

    public void setItemActionListener(ItemActionListener listener) {
        this.mListener = listener;
    }
}
