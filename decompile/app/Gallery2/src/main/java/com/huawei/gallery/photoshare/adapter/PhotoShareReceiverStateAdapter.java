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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.photoshare.ui.FriendSelectionManager;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;

public class PhotoShareReceiverStateAdapter extends BaseAdapter {
    private final Context mContext;
    private final FriendSelectionManager mFriendSelectionManager;
    private final String mOwnerName = this.mContext.getResources().getString(R.string.photoshare_group_owner);
    private ArrayList<ShareReceiver> mReceiverList;

    public PhotoShareReceiverStateAdapter(Context context, FriendSelectionManager selectionMode) {
        this.mContext = context;
        this.mFriendSelectionManager = selectionMode;
    }

    public void setData(ArrayList<ShareReceiver> receiverList) {
        this.mReceiverList = receiverList;
        notifyDataSetChanged();
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

    private ShareReceiver getFriendItem(int position) {
        if (this.mReceiverList == null || this.mReceiverList.size() <= position) {
            return null;
        }
        return (ShareReceiver) this.mReceiverList.get(position);
    }

    private String getItemName(int position) {
        if (position == 0) {
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

    public Bitmap toRoundCorner(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int squareLen = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, squareLen, squareLen);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, ((float) squareLen) / 2.0f, ((float) squareLen) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private int getColorByReceiveState(int state) {
        switch (state) {
            case -1:
            case 2:
                return -1509949440;
            case 0:
                return -16744961;
            case 1:
                return -1509949440;
            case 3:
                return -1509949440;
            default:
                return -1509949440;
        }
    }

    private int getItemColor(int position) {
        if (position == 0) {
            return -1509949440;
        }
        ShareReceiver item = getFriendItem(position);
        if (item != null) {
            return getColorByReceiveState(item.getStatus());
        }
        return -1509949440;
    }

    private int getIdByReceiveState(int state) {
        switch (state) {
            case -1:
                return R.string.photoshare_invite_friend_failed;
            case 0:
                return R.string.photoshare_state_invite;
            case 1:
                return R.string.photoshare_state_accept;
            case 2:
                return R.string.photoshare_state_reject;
            case 3:
                return R.string.photoshare_state_unsubscribe;
            default:
                return R.string.photoshare_state_owner;
        }
    }

    private int getItemState(int position) {
        if (position == 0) {
            return R.string.photoshare_group_owner_state;
        }
        ShareReceiver item = getFriendItem(position);
        if (item != null) {
            return getIdByReceiveState(item.getStatus());
        }
        return 0;
    }

    private void updateViewData(View view, int position) {
        if (position < getCount()) {
            ImageView photo = (ImageView) view.findViewById(R.id.friends_icon);
            Bitmap src = getItemPhoto(position);
            if (src != null) {
                photo.setImageBitmap(toRoundCorner(src));
                photo.setBackgroundResource(R.drawable.head_picture_bg);
            } else {
                photo.setBackgroundResource(0);
                photo.setImageResource(R.drawable.ic_contact_default);
            }
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            if (this.mFriendSelectionManager.inSelectionMode()) {
                final ShareReceiver friendItem = getFriendItem(position);
                if (friendItem == null || position == 0) {
                    checkBox.setVisibility(8);
                } else {
                    checkBox.setVisibility(0);
                    checkBox.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            PhotoShareReceiverStateAdapter.this.mFriendSelectionManager.toggle(friendItem);
                        }
                    });
                    if (this.mFriendSelectionManager.isItemSelected(friendItem)) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }
                }
            } else {
                checkBox.setVisibility(8);
            }
            ((TextView) view.findViewById(R.id.friends_name)).setText(getItemName(position));
            TextView state = (TextView) view.findViewById(R.id.receiver_state);
            int resourceId = getItemState(position);
            if (resourceId != 0) {
                state.setTextColor(getItemColor(position));
                state.setText(resourceId);
                state.setVisibility(0);
            } else {
                state.setVisibility(8);
            }
            GalleryUtils.setTypeFaceAsSlim(state);
        }
    }

    public int getCount() {
        return this.mReceiverList == null ? 0 : this.mReceiverList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.photoshare_edit_firends_item, parent, false);
        }
        updateViewData(convertView, position);
        return convertView;
    }
}
