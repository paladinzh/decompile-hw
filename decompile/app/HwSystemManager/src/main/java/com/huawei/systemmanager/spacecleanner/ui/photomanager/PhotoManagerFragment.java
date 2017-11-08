package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.DataHolder;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.onCallTrashSetListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.LinkedList;
import java.util.List;

public class PhotoManagerFragment extends Fragment {
    private static final String TAG = "PhotoManagerFragment";
    private PhotoManagerAdapter mAdapter;
    private DataHolder mDataHolder;
    private View mEmptyView;
    private List<PhotoManagerBean> mHelpList = new LinkedList();
    private ListView mListView;
    private onCallTrashSetListener mTrashListener;

    public class HelpAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            PhotoManagerFragment.this.mHelpList.clear();
            TrashScanHandler scanHandler = PhotoManagerFragment.this.mTrashListener.getTrashHandler();
            for (Integer i : PhotoManagerBean.getPhotoType()) {
                TrashGroup trashGroup = scanHandler.getTrashByType(i.intValue());
                if (trashGroup != null) {
                    PhotoManagerBean bean = PhotoManagerBean.creator(i.intValue(), trashGroup.getTrashSize());
                    if (bean != null) {
                        PhotoManagerFragment.this.mHelpList.add(bean);
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            PhotoManagerFragment.this.mAdapter.swapData(PhotoManagerFragment.this.mHelpList);
            if (PhotoManagerFragment.this.mAdapter.getCount() > 0) {
                PhotoManagerFragment.this.showListView();
            } else {
                PhotoManagerFragment.this.showEmptyView();
            }
        }
    }

    public void onAttach(Activity ac) {
        super.onAttach(ac);
        this.mTrashListener = (onCallTrashSetListener) ac;
        this.mDataHolder = this.mTrashListener.getDataHolder();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mListView = (ListView) view.findViewById(R.id.list_albumset);
        this.mEmptyView = view.findViewById(R.id.empty_view);
        ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyView);
        initEmptyView(R.string.no_album_trash, R.drawable.ic_no_picture);
        this.mAdapter = new PhotoManagerAdapter();
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PhotoManagerBean bean = (PhotoManagerBean) PhotoManagerFragment.this.mHelpList.get(position);
                if (bean != null) {
                    Intent intent = new Intent(PhotoManagerFragment.this.getActivity(), bean.mClass);
                    intent.putExtra("handler_id", PhotoManagerFragment.this.mDataHolder.trashHander.getId());
                    intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, bean.mParam);
                    try {
                        PhotoManagerFragment.this.startActivity(intent);
                    } catch (Exception e) {
                        HwLog.i(PhotoManagerFragment.TAG, "setOnItemClickListener, exception in start activity", e);
                    }
                }
            }
        });
        return view;
    }

    public void onResume() {
        super.onResume();
        HwLog.i(TAG, "onResume");
        new HelpAsyncTask().execute(new Void[0]);
    }

    private void initEmptyView(int textId, int imageId) {
        if (this.mEmptyView == null) {
            HwLog.i(TAG, "initEmptyView, empty view is null");
            return;
        }
        ViewUtils.setText((TextView) this.mEmptyView.findViewById(R.id.empty_text), textId);
        ViewUtils.setImageResource((ImageView) this.mEmptyView.findViewById(R.id.empty_image), imageId);
    }

    private void showListView() {
        ViewUtils.setVisibility(this.mListView, 0);
        ViewUtils.setVisibility(this.mEmptyView, 8);
    }

    private void showEmptyView() {
        ViewUtils.setVisibility(this.mEmptyView, 0);
        ViewUtils.setVisibility(this.mListView, 8);
    }
}
