package com.huawei.gallery.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Switch;
import com.android.gallery3d.R;
import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.StandardTitleActionMode;

public class ListSetAlbumHiddenFragment extends AbstractGalleryFragment implements OnItemClickListener {
    private GalleryActionBar mActionBar;
    protected AlbumSetDataLoader mAlbumSetDataLoader;
    protected ListView mAlbumSetList;
    protected ListSetAlbumHiddenDataAdapter mAlbumSetListAdapter;
    protected Activity mContext;
    private View mEmptyListView;
    private boolean mIsActive = false;
    protected MediaSet mMediaSet;

    private class MyLoadingListener implements LoadingListener {
        private MyLoadingListener() {
        }

        public void onLoadingStarted() {
        }

        public void onLoadingFinished(boolean loadingFailed) {
            if (ListSetAlbumHiddenFragment.this.mIsActive && ListSetAlbumHiddenFragment.this.mAlbumSetDataLoader.size() == 0) {
                GalleryLog.d("ListAlbumSetFragment", "hidden fragment onLoadingFinished and album is empty");
                ListSetAlbumHiddenFragment.this.mAlbumSetList.setEmptyView(ListSetAlbumHiddenFragment.this.mEmptyListView);
                ListAlbumSetFragment.updateEmptyLayoutPadding(ListSetAlbumHiddenFragment.this.mContext, ListSetAlbumHiddenFragment.this.mEmptyListView);
            }
        }

        public void onVisibleRangeLoadFinished() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        Bundle data = getArguments();
        if (data != null) {
            this.mMediaSet = getGalleryContext().getDataManager().getMediaSet(data.getString("media-path"));
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_albumset, container, false);
        this.mAlbumSetList = (ListView) view.findViewById(R.id.list_albumset);
        this.mEmptyListView = view.findViewById(R.id.list_empty);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(258);
        StandardTitleActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
        am.setTitle((int) R.string.hide_albums_title);
        am.show();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAlbumSetDataLoader = new AlbumSetDataLoader(this.mContext, this.mMediaSet, 64);
        this.mAlbumSetListAdapter = new ListSetAlbumHiddenDataAdapter(getActivity(), this.mAlbumSetDataLoader, this.mAlbumSetList);
        this.mAlbumSetDataLoader.setLoadingListener(new MyLoadingListener());
        this.mAlbumSetList.setAdapter(this.mAlbumSetListAdapter);
        this.mAlbumSetList.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean z;
        Switch switchButton = (Switch) view.findViewById(R.id.switchButton);
        if (switchButton.isChecked()) {
            z = false;
        } else {
            z = true;
        }
        switchButton.setChecked(z);
        String status = switchButton.isChecked() ? "On" : "Off";
        ReportToBigData.report(108, String.format("{HiddenAlbumSet:%s}", new Object[]{status}));
    }

    public void onResume() {
        super.onResume();
        this.mIsActive = true;
        getGalleryContext().getDataManager().notifyReload(Constant.RELOAD_URI_ALBUMSET, 18694);
        this.mAlbumSetDataLoader.resume();
        this.mAlbumSetListAdapter.resume();
    }

    public void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mAlbumSetDataLoader.pause();
        this.mAlbumSetListAdapter.pause();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ListAlbumSetFragment.updateEmptyLayoutPadding(getActivity(), this.mEmptyListView);
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        ListAlbumSetFragment.updateEmptyLayoutPadding(this.mContext, this.mEmptyListView);
    }
}
