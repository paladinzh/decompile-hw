package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.optimize.MemoryManager;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;

public class GroupTypeExpande extends GroupType {
    private static final String TAG = "GroupTypeExpande";
    public static final GroupTypeFactory sExpandeGroupFactory = new GroupTypeFactory() {
        GroupType createHolder(View view) {
            return new GroupTypeExpande(view);
        }

        int getLayoutResId() {
            return R.layout.spaceclean_expand_group_item_twolines_arrow_checkbox;
        }

        int getType() {
            return 1;
        }
    };
    private final CheckBox checkbox;
    private final ImageView indicator;
    private long mCachedFree;
    private final TextView number;
    private final TextView totalSize;

    private class LoadMemoryTask extends AsyncTask<Void, Void, Long> {
        private WeakReference<TextView> mRef;
        private long trashSize;

        public LoadMemoryTask(TextView tv, long trashSize) {
            this.mRef = new WeakReference(tv);
            this.trashSize = trashSize;
        }

        protected void onPreExecute() {
            setText();
        }

        protected Long doInBackground(Void... params) {
            return Long.valueOf(MemoryManager.getFreeMemoryWithBackground(GlobalContext.getContext()));
        }

        protected void onPostExecute(Long aLong) {
            if (aLong != null) {
                GroupTypeExpande.this.mCachedFree = aLong.longValue();
            }
            setText();
        }

        private void setText() {
            if (this.mRef != null) {
                TextView tv = (TextView) this.mRef.get();
                if (tv != null && tv.getTag() == this) {
                    tv.setText(getProcessDes(GroupTypeExpande.this.mCachedFree));
                }
            }
        }

        private String getProcessDes(long free) {
            if (free == 0) {
                return FileUtil.getFileSize(this.trashSize);
            }
            Context ctx = GlobalContext.getContext();
            long total = MemoryManager.getTotal(ctx);
            double usedValue = total <= 0 ? 0.0d : (((double) (total - free)) * 1.0d) / ((double) total);
            NumberFormat.getPercentInstance().setMinimumFractionDigits(0);
            return ctx.getString(R.string.space_clean_cleanable_size_and_memory_info, new Object[]{FileUtil.getFileSize(this.trashSize), NumberFormat.getPercentInstance().format(usedValue), FileUtil.getFileSize(ctx, used), FileUtil.getFileSize(ctx, total)});
        }
    }

    public GroupTypeExpande(View view) {
        super(view);
        this.title = (TextView) view.findViewById(R.id.text1);
        this.totalSize = (TextView) view.findViewById(R.id.text2);
        this.number = (TextView) view.findViewById(R.id.number);
        this.indicator = (ImageView) view.findViewById(R.id.arrow);
        this.checkbox = (CheckBox) view.findViewById(R.id.list_item_checkbox);
    }

    int getType() {
        return 1;
    }

    void bindView(boolean isExpanded, TrashItemGroup itemGroup, SpaceState state, OnClickListener itemClicker, Boolean canShowProgress, OnClickListener checkClicker) {
        this.title.setText(itemGroup.getName());
        int childCount = itemGroup.getSize();
        if (itemGroup.getTrashType() == 32768) {
            this.number.setText("");
        } else {
            this.number.setText(GlobalContext.getContext().getResources().getQuantityString(R.plurals.spaceclean_items, childCount, new Object[]{Integer.valueOf(childCount)}));
        }
        this.indicator.setImageResource(isExpanded ? R.drawable.spacecleaner_expander_arrow_close : R.drawable.spacecleaner_expander_arrow_open);
        this.totalSize.setTag(null);
        if (itemGroup.getTrashType() == 131072) {
            this.checkbox.setVisibility(8);
            this.totalSize.setVisibility(8);
            return;
        }
        if (itemGroup.isNoTrash()) {
            this.totalSize.setVisibility(8);
            this.checkbox.setVisibility(8);
        } else {
            this.totalSize.setVisibility(0);
            if (itemGroup.isCleaned()) {
                this.checkbox.setVisibility(8);
                this.totalSize.setText(R.string.space_cache_item_cleaned);
            } else {
                long trashSize = itemGroup.getTrashSizeCleaned(false);
                if (itemGroup.getTrashType() == 32768) {
                    LoadMemoryTask task = new LoadMemoryTask(this.totalSize, trashSize);
                    this.totalSize.setTag(task);
                    task.executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, new Void[0]);
                } else {
                    this.totalSize.setText(FileUtil.getFileSize(trashSize));
                }
                this.checkbox.setVisibility(0);
                this.checkbox.setOnClickListener(checkClicker);
                this.checkbox.setChecked(itemGroup.isChecked());
                this.checkbox.setTag(itemGroup);
            }
        }
        if (state.isCleanEnd()) {
            this.checkbox.setVisibility(8);
        }
    }
}
