package com.android.settings.smartcover;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class LocalResFragment extends Fragment implements LoaderCallbacks<List<CoverBackgroundSrcInfo>> {
    public CoverBackgroundSrcInfo mDetailInfo;
    private DiyHandler mDiyHandler;
    private DiyResAdapter mDiyResAdapter;
    private GridView mGridView;
    private ImageFetcher mImageWorker;
    private final OnItemClickListener mOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
            if (LocalResFragment.this.mDiyResAdapter != null) {
                LocalResFragment.this.mDiyResAdapter.resetCurrentImageStatus();
                LocalResFragment.this.mDetailInfo = LocalResFragment.this.mDiyResAdapter.getItem(position);
                if (LocalResFragment.this.mDetailInfo.getType() == -1) {
                    LocalResFragment.this.mDetailInfo.setType(8);
                } else {
                    LocalResFragment.this.mDetailInfo.setType(-1);
                }
                LocalResFragment.this.mDetailInfo = LocalResFragment.this.mDiyResAdapter.getItem(position);
                if (LocalResFragment.this.mDetailInfo != null) {
                    Global.putInt(LocalResFragment.this.getActivity().getContentResolver(), "cover_background_src_index", LocalResFragment.this.mDetailInfo.getmImageIndex());
                }
                if (LocalResFragment.this.mDiyHandler != null) {
                    LocalResFragment.this.mDiyHandler.removeMessages(5);
                    LocalResFragment.this.mDiyHandler.sendEmptyMessage(5);
                }
                LocalResFragment.this.getActivity().finish();
            }
        }
    };

    public class DiyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    if (LocalResFragment.this.mDiyResAdapter != null) {
                        LocalResFragment.this.mDiyResAdapter.clear();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("cover_background_src_index", Global.getInt(LocalResFragment.this.getActivity().getContentResolver(), "cover_background_src_index", 0));
                    LocalResFragment.this.getLoaderManager().restartLoader(101, bundle, LocalResFragment.this);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public static class DiyResAdapter extends BaseAdapter {
        private List<CoverBackgroundSrcInfo> mDatas = null;
        protected ImageWorker mImageWorker;
        private LayoutInflater mInflater;

        public DiyResAdapter(Context context) {
            if (context != null) {
                this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null && this.mInflater != null) {
                convertView = this.mInflater.inflate(2130968847, null);
                viewHolder.mBackgroundImage = (BackgroundImage) convertView.findViewById(2131886755);
                convertView.setTag(viewHolder);
            } else if (convertView != null) {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (convertView == null) {
                return null;
            }
            TextView textViewNoneBg = (TextView) convertView.findViewById(2131886756);
            if (textViewNoneBg != null) {
                textViewNoneBg.setVisibility(4);
            }
            CoverBackgroundSrcInfo info = getItem(position);
            if (info == null) {
                return convertView;
            }
            if (textViewNoneBg != null && info.getmImageIndex() == 0) {
                textViewNoneBg.setVisibility(0);
            }
            viewHolder.mBackgroundImage.setThemeType(info.getType());
            if (this.mImageWorker != null) {
                this.mImageWorker.loadImage(Integer.valueOf(info.getImageSrcId()), viewHolder.mBackgroundImage);
            }
            return convertView;
        }

        public void resetCurrentImageStatus() {
            if (this.mDatas != null && this.mDatas.size() != 0) {
                for (CoverBackgroundSrcInfo data : this.mDatas) {
                    if (data != null && data.getType() == 8) {
                        data.setType(-1);
                    }
                }
            }
        }

        public int getCount() {
            return this.mDatas == null ? 0 : this.mDatas.size();
        }

        public CoverBackgroundSrcInfo getItem(int position) {
            int count = getCount();
            if (this.mDatas == null || position >= count) {
                return null;
            }
            return (CoverBackgroundSrcInfo) this.mDatas.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public void addData(List<CoverBackgroundSrcInfo> infos) {
            if (infos != null) {
                if (this.mDatas == null) {
                    this.mDatas = new ArrayList();
                }
                int count = infos.size();
                for (int i = 0; i < count; i++) {
                    CoverBackgroundSrcInfo info = (CoverBackgroundSrcInfo) infos.get(i);
                    if (this.mDatas.contains(info)) {
                        int index = this.mDatas.indexOf(info);
                        if (index != -1) {
                            this.mDatas.remove(info);
                            this.mDatas.add(index, info);
                        }
                    } else {
                        this.mDatas.add(info);
                    }
                }
                notifyDataSetChanged();
            }
        }

        public void setImageWorker(ImageWorker worker) {
            this.mImageWorker = worker;
        }

        public void clear() {
            if (this.mDatas != null) {
                this.mDatas.clear();
            }
        }
    }

    private static class ViewHolder {
        BackgroundImage mBackgroundImage;

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mImageWorker = ImageFetcher.getGridImageFetcher(getActivity());
        this.mDiyHandler = new DiyHandler();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (inflater == null) {
            return null;
        }
        View v = inflater.inflate(2130968848, container, false);
        initView(v);
        return v;
    }

    public void onResume() {
        super.onResume();
        Bundle bundle = new Bundle();
        bundle.putInt("cover_background_src_index", Global.getInt(getActivity().getContentResolver(), "cover_background_src_index", 0));
        getLoaderManager().restartLoader(101, bundle, this);
    }

    public void initView(View v) {
        this.mGridView = (GridView) v.findViewById(2131886757);
        if (this.mGridView != null) {
            this.mGridView.setOnItemClickListener(this.mOnClickListener);
            this.mDiyResAdapter = new DiyResAdapter(getActivity());
            this.mDiyResAdapter.setImageWorker(this.mImageWorker);
            this.mGridView.setAdapter(this.mDiyResAdapter);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mImageWorker != null) {
            this.mImageWorker.clearMemoryCache();
        }
        if (this.mDiyHandler != null) {
            this.mDiyHandler.removeMessages(5);
        }
        getLoaderManager().destroyLoader(101);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }

    public Loader<List<CoverBackgroundSrcInfo>> onCreateLoader(int loader, Bundle data) {
        return new CoverBgSrcDataLoader(getActivity(), data);
    }

    public void onLoadFinished(Loader<List<CoverBackgroundSrcInfo>> loader, List<CoverBackgroundSrcInfo> data) {
        if (!(this.mDiyResAdapter == null || data == null || data.size() <= 0)) {
            this.mDiyResAdapter.addData(data);
        }
    }

    public void onLoaderReset(Loader<List<CoverBackgroundSrcInfo>> loader) {
    }
}
