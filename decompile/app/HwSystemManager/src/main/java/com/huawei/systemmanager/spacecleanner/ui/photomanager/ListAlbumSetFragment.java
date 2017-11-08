package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.DataHolder;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.onCallTrashSetListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class ListAlbumSetFragment extends Fragment implements OnItemClickListener, MessageHandler {
    public static final String TAG = "ListAlbumSetFragment";
    protected ListView mAlbumSetList;
    protected ListAlbumSetDataAdapter mAlbumSetListAdapter;
    protected View mEmtpyView;
    private List<PhotoFolder> mPhotoFolders = new ArrayList();
    private onCallPhotoTrashSetListener mPhotoTrashListener;
    private onCallTrashSetListener mTrashListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof onCallTrashSetListener) {
            this.mTrashListener = (onCallTrashSetListener) activity;
        } else {
            HwLog.d(TAG, "activity not have onCallTrashSetListener interface! ");
        }
        if (activity instanceof onCallPhotoTrashSetListener) {
            this.mPhotoTrashListener = (onCallPhotoTrashSetListener) activity;
        } else {
            HwLog.d(TAG, "activity not have onCallPhotoTrashSetListener interface! ");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.i(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mAlbumSetList = (ListView) view.findViewById(R.id.list_albumset);
        this.mEmtpyView = view.findViewById(R.id.empty_view);
        ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmtpyView);
        return view;
    }

    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            initActionBar();
            if (this.mAlbumSetListAdapter != null) {
                if (this.mTrashListener == null) {
                    HwLog.i(TAG, "mTrashListener is null");
                    return;
                }
                this.mTrashListener.resetTrashSet();
                this.mAlbumSetListAdapter.notifyDataSetChanged();
                showEmptyOrList();
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initActionBar();
        this.mAlbumSetListAdapter = new ListAlbumSetDataAdapter(getApplicationContext(), this.mPhotoFolders);
        if (this.mAlbumSetList != null) {
            this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
            this.mAlbumSetList.setOnItemClickListener(this);
        }
        showEmptyOrList();
    }

    public void showEmptyOrList() {
        if (this.mAlbumSetListAdapter != null && this.mEmtpyView != null && this.mAlbumSetList != null) {
            if (this.mAlbumSetListAdapter.getCount() > 0) {
                this.mEmtpyView.setVisibility(8);
                this.mAlbumSetList.setVisibility(0);
                return;
            }
            this.mEmtpyView.setVisibility(0);
            this.mAlbumSetList.setVisibility(8);
        }
    }

    private void initData() {
        if (this.mPhotoTrashListener == null) {
            HwLog.i(TAG, "mPhotoTrashListener is null");
            return;
        }
        this.mPhotoFolders.clear();
        this.mPhotoFolders = this.mPhotoTrashListener.getPhotoFolders();
    }

    private void initActionBar() {
        if (this.mTrashListener == null) {
            HwLog.i(TAG, "mTrashListener is null");
            return;
        }
        DataHolder dataHolder = this.mTrashListener.getDataHolder();
        if (dataHolder == null) {
            HwLog.i(TAG, "dataHolder is null");
            return;
        }
        Activity ac = getActivity();
        if (ac == null) {
            HwLog.i(TAG, "getActivity is null");
            return;
        }
        ActionBar actionBar = ac.getActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(dataHolder.param.getTitleStr());
        actionBar.show();
        ac.setTitle(dataHolder.param.getTitleStr());
        setEmptyTextAndImage(R.string.no_album_trash, R.drawable.ic_no_picture);
    }

    public void setEmptyTextAndImage(int textId, int imageId) {
        if (this.mEmtpyView != null) {
            TextView textView = (TextView) this.mEmtpyView.findViewById(R.id.empty_text);
            if (!(textView == null || textId == 0)) {
                textView.setText(textId);
            }
            ImageView imageView = (ImageView) this.mEmtpyView.findViewById(R.id.empty_image);
            if (!(imageView == null || imageId == 0)) {
                imageView.setImageResource(imageId);
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        onSingleTapUp(position);
    }

    protected void onSingleTapUp(int position) {
        if (this.mPhotoFolders == null || this.mPhotoFolders.size() == 0) {
            HwLog.e(TAG, "mPhotoFolders is empty when click");
            return;
        }
        int folderSize = this.mPhotoFolders.size();
        if (folderSize <= position) {
            HwLog.e(TAG, "photo folders size:" + folderSize + " but position:" + position);
            return;
        }
        PhotoFolder folder = (PhotoFolder) this.mPhotoFolders.get(position);
        if (this.mPhotoTrashListener == null) {
            HwLog.i(TAG, "mPhotoTrashListener is null");
        } else {
            this.mPhotoTrashListener.startGridSetFragment(folder.getFolderPath());
        }
    }

    public void handleMessage(Message msg) {
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }
}
