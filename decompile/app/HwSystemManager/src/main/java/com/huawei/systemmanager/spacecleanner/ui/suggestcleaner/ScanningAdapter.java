package com.huawei.systemmanager.spacecleanner.ui.suggestcleaner;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.SimpleAnimatorListener;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.NormalCovertor;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;
import java.util.List;

public class ScanningAdapter extends CommonAdapter<TrashItemGroup> {
    private static final int SCAN_SHOW_PROGRESS_BAR_DELAY = 550;
    private static final String TAG = "ScanningAdapter";
    private SparseArray<WeakReference<ViewHolder>> mHolderMaps = new SparseArray();

    public static class ViewHolder {
        Animator mAnima;
        ImageView mEndFlag;
        private TrashItemGroup mItem;
        ProgressBar mProgress;
        ViewStub mStub;
        TextView mTitle;

        public ViewHolder(View view) {
            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mStub = (ViewStub) view.findViewById(R.id.end_icon_container_stub);
            view.postDelayed(new Runnable() {
                public void run() {
                    ViewHolder.this.ensureStub();
                }
            }, 550);
        }

        public void bindView(TrashItemGroup item) {
            this.mItem = item;
            this.mTitle.setText(item.getName());
            bindStateView();
        }

        public void playScanEnd() {
            if (this.mStub != null) {
                HwLog.i(ScanningAdapter.TAG, "playScanEnd, but stub is still not null");
            } else if (this.mEndFlag.getVisibility() == 0) {
                HwLog.i(ScanningAdapter.TAG, "playScanEnd, end flag is visiable, need not play");
            } else if (this.mAnima != null) {
                HwLog.e(ScanningAdapter.TAG, "playScanEnd, mAnima != null, something wrong!");
            } else {
                this.mAnima = buildAnimator();
                this.mAnima.addListener(new SimpleAnimatorListener() {
                    public void onAnimationEnd(Animator animation) {
                        ViewHolder.this.mAnima = null;
                    }
                });
                HwLog.i(ScanningAdapter.TAG, "playScanEnd, start anima safe");
                this.mAnima.start();
            }
        }

        private boolean ensureStub() {
            if (this.mStub == null) {
                return false;
            }
            View view = this.mStub.inflate();
            this.mStub = null;
            this.mEndFlag = (ImageView) view.findViewById(R.id.imageview);
            this.mProgress = (ProgressBar) view.findViewById(R.id.progress_bar);
            bindStateView();
            return true;
        }

        private void bindStateView() {
            if (this.mStub == null) {
                if (this.mAnima != null) {
                    this.mAnima.cancel();
                    this.mAnima = null;
                }
                if (this.mItem != null) {
                    if (this.mItem.isScanFinished()) {
                        ScanningAdapter.showView(this.mEndFlag);
                        this.mProgress.setVisibility(4);
                    } else {
                        ScanningAdapter.showView(this.mProgress);
                        this.mEndFlag.setVisibility(4);
                    }
                }
            }
        }

        private Animator buildAnimator() {
            Context ctx = GlobalContext.getContext();
            Animator anima1 = AnimatorInflater.loadAnimator(ctx, R.animator.space_clean_view_disappear_anima);
            anima1.setTarget(this.mProgress);
            anima1.addListener(new SimpleAnimatorListener() {
                public void onAnimationEnd(Animator animation) {
                    ViewHolder.this.mProgress.setVisibility(8);
                }
            });
            Animator anima2 = AnimatorInflater.loadAnimator(ctx, R.animator.space_clean_view_appear_anima);
            anima2.setTarget(this.mEndFlag);
            anima2.addListener(new SimpleAnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    ViewHolder.this.mEndFlag.setVisibility(0);
                    ViewHolder.this.mEndFlag.setAlpha(0.0f);
                }
            });
            AnimatorSet result = new AnimatorSet();
            result.playSequentially(new Animator[]{anima1, anima2});
            return result;
        }
    }

    public ScanningAdapter(Context context, LayoutInflater inflater) {
        super(context, inflater);
        init();
    }

    private void init() {
        this.mList.addAll(new NormalCovertor().getScanningList());
    }

    public void setList(ListView lv) {
        lv.setAdapter(this);
    }

    protected View newView(int position, ViewGroup parent, TrashItemGroup item) {
        View view = getInflater().inflate(R.layout.spaceclean_trashlist_group_item_rounding, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        HwLog.i(TAG, "new view, position:" + position + ", item:" + item + ",holder:" + holder);
        return view;
    }

    protected void bindView(int position, View view, TrashItemGroup item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.bindView(item);
        HwLog.i(TAG, "bindView, position:" + position + ", holder item:" + holder.mItem.getName());
        this.mHolderMaps.put(position, new WeakReference(holder));
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        HwLog.e(TAG, "do not call notifyDataSetChanged");
    }

    public void notifyScanEnd(int trashTypes) {
        List<TrashItemGroup> trashes = this.mList;
        int size = trashes.size();
        for (int i = 0; i < size; i++) {
            TrashItemGroup item = (TrashItemGroup) trashes.get(i);
            if (!item.isScanFinished()) {
                int itemType = item.getTrashType();
                if ((trashTypes & itemType) == itemType) {
                    HwLog.i(TAG, "notifyScanEnd, itemType:" + Integer.toBinaryString(itemType) + ", finishedType:" + Integer.toBinaryString(trashTypes) + ", item name:" + item.getName());
                    item.setScanFinished(true);
                    WeakReference<ViewHolder> holderRef = (WeakReference) this.mHolderMaps.get(i);
                    if (holderRef == null) {
                        HwLog.i(TAG, "notifyScanEnd cannot find holderRef");
                    } else {
                        ViewHolder holder = (ViewHolder) holderRef.get();
                        if (holder == null) {
                            HwLog.i(TAG, "notifyScanEnd holderRef.get() is null");
                            this.mHolderMaps.remove(i);
                        } else if (holder.mItem != item) {
                            HwLog.i(TAG, "notifyScanEnd holder.mItem != itemGroup, holder item :" + holder.mItem.getName() + ", finished item:" + item.getName() + ",i=" + i);
                        } else {
                            HwLog.i(TAG, "ready to play scan end anima");
                            holder.playScanEnd();
                        }
                    }
                }
            }
        }
    }

    public boolean hasStableIds() {
        return true;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public static void showView(View view) {
        view.setAlpha(Utility.ALPHA_MAX);
        view.setVisibility(0);
    }
}
