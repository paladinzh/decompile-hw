package com.huawei.gallery.photoshare.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.huawei.android.cg.vo.ShareReceiver;

public class PhotoShareReceiverView extends TextView {
    private OnItemListener listener;
    private ShareReceiver mFriendsInfo;

    public interface OnItemListener {
        void onDelete(View view);
    }

    public PhotoShareReceiverView(Context context, PhotoShareReceiverViewGroup viewgroup) {
        super(context);
    }

    public void setFriendsInfo(ShareReceiver info) {
        this.mFriendsInfo = info;
    }

    public ShareReceiver getFriendsInfo() {
        return this.mFriendsInfo;
    }

    public void setOnItemClickListener(OnItemListener l) {
        this.listener = l;
        setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PhotoShareReceiverView.this.listener != null) {
                    PhotoShareReceiverView.this.listener.onDelete(v);
                }
            }
        });
    }
}
