package com.huawei.gallery.photoshare.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;

public class PhotoShareMemberAdapter extends BaseAdapter {
    private final Context mContext;
    private final String mOwnerName = this.mContext.getResources().getString(R.string.photoshare_group_owner);
    private ArrayList<ShareReceiver> mReceiverList;

    public PhotoShareMemberAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(ArrayList<ShareReceiver> receiverList) {
        this.mReceiverList = receiverList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mReceiverList == null ? 0 : this.mReceiverList.size();
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public Object getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.photoshare_edit_firends_item, parent, false);
        }
        updateViewData(convertView, position);
        return convertView;
    }

    private Bitmap getItemPhoto(int position) {
        if (this.mReceiverList != null && this.mReceiverList.size() > position) {
            ShareReceiver item = (ShareReceiver) this.mReceiverList.get(position);
            if (item != null) {
                String headPath = PhotoShareUtils.getValueFromJson(item.getShareId(), "headPictureLocalPath");
                if (!TextUtils.isEmpty(headPath)) {
                    return BitmapFactory.decodeFile(headPath);
                }
            }
        }
        return null;
    }

    public Bitmap toRoundCorner(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        float pixels = this.mContext.getResources().getDimension(R.dimen.photoshare_member_inner_radius);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private ShareReceiver getFriendItem(int position) {
        if (this.mReceiverList == null || this.mReceiverList.size() <= position) {
            return null;
        }
        return (ShareReceiver) this.mReceiverList.get(position);
    }

    private String getItemName(int position) {
        if (1 == position) {
            return this.mOwnerName;
        }
        ShareReceiver item = getFriendItem(position);
        if (item == null) {
            return null;
        }
        if (item.getStatus() != -1) {
            String receiverName = PhotoShareUtils.getValueFromJson(item.getShareId(), "receiverName");
            if (TextUtils.isEmpty(receiverName)) {
                return item.getReceiverAcc();
            }
            return receiverName;
        } else if (TextUtils.isEmpty(item.getReceiverName())) {
            return item.getReceiverAcc();
        } else {
            return item.getReceiverName();
        }
    }

    private void updateViewData(View view, int position) {
        if (position < getCount()) {
            Bitmap src = getItemPhoto(position);
            ImageView photo = (ImageView) view.findViewById(R.id.friends_icon);
            if (src != null) {
                photo.setImageBitmap(toRoundCorner(src));
                photo.setBackgroundResource(R.drawable.head_picture_bg);
            } else {
                photo.setBackgroundResource(0);
                photo.setImageResource(R.drawable.ic_contact_default);
            }
            ((TextView) view.findViewById(R.id.friends_name)).setText(getItemName(position));
        }
    }
}
