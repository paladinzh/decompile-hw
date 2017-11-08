package com.huawei.systemmanager.comm.component;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;

public abstract class BaseListFragment<T> extends Fragment implements LoaderCallbacks<List<T>> {
    public static final String TAG = BaseListFragment.class.getSimpleName();
    public static final int TASK_FRAGMENT_REQUEST_CODE = 0;
    private View contentView;
    CommonAdapter<T> mAdapter;
    AdapterView mAdapterView;
    boolean mDataLoadFinished;
    private int mEmptyImageId;
    private ViewStub mEmptyStub;
    private int mEmptyTextId;
    View mEmptyView;
    private final OnItemClickListener mItemClickListen = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BaseListFragment.this.onListItemClick(parent, view, position, id);
        }
    };
    View mListContainer;
    private View mProgressContainer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment frg = getFragmentManager().findFragmentByTag(TaskFragment.TAG);
        if (frg != null) {
            frg.setTargetFragment(this, 0);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.contentView = view;
        this.mProgressContainer = this.contentView.findViewById(R.id.progress_container);
        this.mAdapterView = (AdapterView) this.contentView.findViewById(R.id.content_list);
        if (this.mAdapter != null) {
            setListAdapter(this.mAdapter);
        }
        this.mAdapterView.setOnItemClickListener(this.mItemClickListen);
        this.mListContainer = this.contentView.findViewById(R.id.list_container);
        if (this.mListContainer == null) {
            this.mListContainer = this.mAdapterView;
        }
        try {
            this.mEmptyStub = (ViewStub) this.contentView.findViewById(R.id.empty_view);
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyStub);
        } catch (Exception e) {
            HwLog.i(TAG, "emptyview is not viewstub");
        }
    }

    public void setEmptyTextAndImage(int textId, int imageId) {
        this.mEmptyTextId = textId;
        this.mEmptyImageId = imageId;
    }

    public void setListAdapter(CommonAdapter<T> adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter is null!");
        }
        this.mAdapter = adapter;
        if (this.mAdapterView != null) {
            this.mAdapterView.setAdapter(this.mAdapter);
        }
    }

    public CommonAdapter<T> getAdapter() {
        return this.mAdapter;
    }

    public final void initLoader(int loaderId) {
        getLoaderManager().initLoader(loaderId, new Bundle(), this);
    }

    public final void notifyLoader(int loaderId) {
        if (getActivity() == null) {
            HwLog.i(TAG, "notifyLoader activity is null");
            return;
        }
        Loader loader = getLoaderManager().getLoader(loaderId);
        if (loader == null) {
            HwLog.w(TAG, "notifyLoader could not find loaderid:" + loaderId);
        } else {
            loader.onContentChanged();
        }
    }

    public AdapterView getListView() {
        return this.mAdapterView;
    }

    public int getCount() {
        return this.mAdapter == null ? 0 : this.mAdapter.getCount();
    }

    public boolean startTask(TaskFragment<?, ?, ?> taskFragment) {
        if (ensureActivityInResmue()) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager == null) {
                HwLog.i(TAG, "start task failed,fragmentManager is null");
                return false;
            } else if (fragmentManager.findFragmentByTag(TaskFragment.TAG) != null) {
                HwLog.i(TAG, "start task failed,TaskFragment is already exist");
                return false;
            } else {
                taskFragment.setTargetFragment(this, 0);
                fragmentManager.beginTransaction().add(taskFragment, TaskFragment.TAG).commit();
                return true;
            }
        }
        HwLog.i(TAG, "start task failed,activity is not in resume state");
        return false;
    }

    public boolean swapAdapterData(List<T> list) {
        this.mDataLoadFinished = true;
        if (this.mAdapter == null) {
            HwLog.e(TAG, "swapAdapterData but adapter is null!!");
            return false;
        }
        boolean res = this.mAdapter.swapData(list);
        updateEmptyView();
        onAdapterDataChange();
        return res;
    }

    public boolean notifyDataSetChanged() {
        if (this.mAdapter == null) {
            HwLog.e(TAG, "notifyDataSetChanged called, but adpater is null!");
            return false;
        }
        this.mAdapter.notifyDataSetChanged();
        return true;
    }

    public boolean deleteItem(T item) {
        if (this.mAdapter == null || !this.mAdapter.deleteItem((Object) item)) {
            return false;
        }
        updateEmptyView();
        onAdapterDataChange();
        return true;
    }

    public void updateEmptyView() {
        setVisibility(this.mProgressContainer, 8);
        if (this.mAdapter == null) {
            HwLog.e(TAG, "updateEmptyView called, but adapter is null!");
            return;
        }
        if (this.mAdapter.isEmpty()) {
            HwLog.i(TAG, "show emptyview");
            initEmptyView();
            setVisibility(this.mEmptyView, 0);
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyView);
            setVisibility(this.mListContainer, 8);
        } else {
            setVisibility(this.mEmptyView, 8);
            setVisibility(this.mListContainer, 0);
            HwLog.i(TAG, "hide emptyview");
        }
    }

    private void initEmptyView() {
        if (this.mEmptyView == null && this.contentView != null) {
            if (this.mEmptyStub != null) {
                this.mEmptyStub.inflate();
            }
            this.mEmptyView = this.contentView.findViewById(R.id.empty_view_item);
            if (this.mEmptyView != null) {
                TextView textView = (TextView) this.mEmptyView.findViewById(R.id.empty_text);
                if (!(textView == null || this.mEmptyTextId == 0)) {
                    textView.setText(this.mEmptyTextId);
                }
                ImageView imageView = (ImageView) this.mEmptyView.findViewById(R.id.empty_image);
                if (!(imageView == null || this.mEmptyImageId == 0)) {
                    imageView.setImageResource(this.mEmptyImageId);
                }
            }
        }
    }

    public void showLoadingView() {
        setVisibility(this.mProgressContainer, 0);
    }

    private void setVisibility(View view, int visibleState) {
        if (view != null && visibleState != view.getVisibility()) {
            view.setVisibility(visibleState);
        }
    }

    public boolean ensureActivityInResmue() {
        Activity activity = getActivity();
        if (activity == null || !activity.isResumed()) {
            return false;
        }
        return true;
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
    }

    protected void onAdapterDataChange() {
    }

    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        swapAdapterData(data);
    }

    public void onLoaderReset(Loader<List<T>> loader) {
    }

    public boolean isDataLoadFinished() {
        return this.mDataLoadFinished;
    }

    protected final String getStringEx(int resId) {
        return GlobalContext.getContext().getString(resId);
    }

    protected final String getStringEx(int resId, Object... formatArgs) {
        return GlobalContext.getContext().getString(resId, formatArgs);
    }

    protected final Resources getResourcesEx() {
        return GlobalContext.getContext().getResources();
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    public List<T> getData() {
        if (this.mAdapter != null) {
            return this.mAdapter.getData();
        }
        HwLog.i(TAG, "getData() called, but adapter is null");
        return Collections.emptyList();
    }
}
