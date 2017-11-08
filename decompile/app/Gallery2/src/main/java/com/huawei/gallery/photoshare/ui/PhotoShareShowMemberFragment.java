package com.huawei.gallery.photoshare.ui;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.Path;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.photoshare.adapter.PhotoShareMemberAdapter;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils.updateHeadInfoListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoShareShowMemberFragment extends AbstractGalleryFragment implements updateHeadInfoListener {
    protected GalleryActionBar mActionBar = null;
    private PhotoShareMemberAdapter mAdapter;
    private PhotoShareEditFriendsGridview mGroupMember = null;
    private TextView mGroupTitle;
    private ArrayList<ShareReceiver> mInvitedFriendsList = new ArrayList();
    private String mOwnerId;
    private String mShareId;
    private ShareInfo mShareInfo;
    private String mSharePath;
    private String mTitle;

    public void onActionItemClicked(Action action) {
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public boolean onBackPressed() {
        return false;
    }

    public void headInfoChanged(String shareId) {
        if (this.mShareId != null && this.mShareId.equals(shareId)) {
            initReceiverList();
        }
    }

    protected void onCreateActionBar(Menu menu) {
        requestFeature(258);
        if (this.mActionBar != null) {
            this.mActionBar.enterActionMode(false);
            ActionMode am = (ActionMode) this.mActionBar.getCurrentMode();
            am.setTitle(this.mTitle);
            am.setBothAction(Action.NONE, Action.NONE);
            am.show();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        this.mSharePath = data.getString("sharePath");
        this.mShareId = Path.fromString(this.mSharePath).getSuffix();
        this.mTitle = data.getString("shareName");
        this.mAdapter = new PhotoShareMemberAdapter(getActivity());
        requestHeadPic();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_show_menber, container, false);
        this.mGroupMember = (PhotoShareEditFriendsGridview) view.findViewById(R.id.group_member);
        this.mGroupMember.setAdapter(this.mAdapter);
        this.mGroupMember.setFocusable(false);
        this.mGroupMember.updateGridViewColums();
        ((ScrollView) view.findViewById(R.id.scroll_view)).setOverScrollMode(2);
        Locale defloc = Locale.getDefault();
        this.mGroupTitle = (TextView) view.findViewById(R.id.group_title);
        this.mGroupTitle.setText(this.mGroupTitle.getText().toString().toUpperCase(defloc));
        if (PhotoShareUtils.isSupportPhotoShare()) {
            if (PhotoShareUtils.getServer() != null) {
                initReceiverList();
            } else {
                PhotoShareUtils.setRunnable(new Runnable() {
                    public void run() {
                        PhotoShareShowMemberFragment.this.initReceiverList();
                    }
                });
            }
        }
        return view;
    }

    private void requestHeadPic() {
        try {
            ShareInfo shareinfo = PhotoShareUtils.getServer().getShare(this.mShareId);
            if (shareinfo != null) {
                List<ShareReceiver> receivers = shareinfo.getReceiverList();
                ArrayList<String> userIdList = new ArrayList();
                userIdList.add(shareinfo.getOwnerId());
                if (receivers != null && receivers.size() > 0) {
                    for (ShareReceiver receiver : receivers) {
                        userIdList.add(receiver.getReceiverId());
                    }
                }
                PhotoShareUtils.getServer().getAlbumHeadPic(this.mShareId, (String[]) userIdList.toArray(new String[userIdList.size()]));
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    public void onResume() {
        super.onResume();
        PhotoShareUtils.setUpdateHeadInfoListener(this);
    }

    public void onPause() {
        super.onPause();
        PhotoShareUtils.setUpdateHeadInfoListener(null);
    }

    private void initReceiverList() {
        try {
            this.mShareInfo = PhotoShareUtils.getServer().getShare(this.mShareId);
            if (this.mShareInfo != null) {
                this.mOwnerId = this.mShareInfo.getOwnerId();
                ArrayList<ShareReceiver> list = new ArrayList();
                Object obj = null;
                Object obj2 = null;
                String myId = PhotoShareUtils.getLoginUserId();
                List<ShareReceiver> tempList = PhotoShareUtils.getServer().getAlbumLocalHeadPic(this.mShareId);
                if (tempList != null && tempList.size() > 0) {
                    for (ShareReceiver receiver : tempList) {
                        if (receiver.getReceiverId().equals(this.mOwnerId)) {
                            ShareReceiver owner = receiver;
                        } else if (receiver.getReceiverId().equals(myId)) {
                            ShareReceiver me = receiver;
                        } else if (receiver.getStatus() != 2) {
                            list.add(receiver);
                        }
                    }
                }
                if (obj == null) {
                    obj = new ShareReceiver();
                    obj.setReceiverId(this.mOwnerId);
                }
                list.add(0, obj);
                if (obj2 == null) {
                    obj2 = new ShareReceiver();
                    obj2.setReceiverId(myId);
                }
                list.add(1, obj2);
                this.mInvitedFriendsList.clear();
                this.mInvitedFriendsList.addAll(list);
                if (!(this.mAdapter == null || this.mShareInfo == null)) {
                    this.mAdapter.setData(this.mInvitedFriendsList);
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }
}
