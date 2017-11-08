package com.huawei.systemmanager.adblock.ui.model;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdBlock.Cmp;
import com.huawei.systemmanager.adblock.comm.AdDispatcher;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdModelImpl implements IAdModel {
    private static final String TAG = "AdBlock_AdModelImpl";
    private AdBlockListLoader mAdBlockListLoader;
    private final Context mAppContext;
    private HsmSingleExecutor mHsmSingleExecutor = new HsmSingleExecutor();

    private class AdBlockListLoader extends AsyncTask<Void, Void, List<AdBlock>> {
        private int mCheckedCount;
        private IDataListener mDataListener;

        AdBlockListLoader(IDataListener listener) {
            this.mDataListener = listener;
        }

        protected List<AdBlock> doInBackground(Void... params) {
            List<AdBlock> result = AdBlock.getAllAdBlocks(AdModelImpl.this.mAppContext);
            Iterator<AdBlock> iterator = result.iterator();
            int checkedCount = 0;
            while (iterator.hasNext()) {
                AdBlock adBlock = (AdBlock) iterator.next();
                if (!adBlock.hasAd()) {
                    iterator.remove();
                } else if (adBlock.isPackageInstalled(AdModelImpl.this.mAppContext)) {
                    adBlock.loadLabelAndIcon(AdModelImpl.this.mAppContext);
                    if (adBlock.isEnable()) {
                        checkedCount++;
                    }
                } else {
                    iterator.remove();
                }
            }
            this.mCheckedCount = checkedCount;
            Collections.sort(result, new Cmp());
            return result;
        }

        protected void onPostExecute(List<AdBlock> result) {
            if (!isCancelled()) {
                this.mDataListener.onLoadCompleted(result, this.mCheckedCount);
            }
        }
    }

    private class EnableAdBlockRunnable implements Runnable {
        private final List<AdBlock> mAdBlocks = Lists.newArrayList();
        private final boolean mEnable;

        EnableAdBlockRunnable(boolean enable, List<AdBlock> adBlocks) {
            this.mEnable = enable;
            this.mAdBlocks.addAll(adBlocks);
        }

        public void run() {
            if (!this.mAdBlocks.isEmpty()) {
                int i;
                ContentValues cv = new ContentValues();
                String str = AdBlockColumns.COLUMN_ENABLE;
                if (this.mEnable) {
                    i = 1;
                } else {
                    i = 0;
                }
                cv.put(str, Integer.valueOf(i));
                StringBuilder where = new StringBuilder();
                where.append("pkg_name").append(" in ('").append(((AdBlock) this.mAdBlocks.get(0)).getPkgName());
                for (int i2 = 1; i2 < this.mAdBlocks.size(); i2++) {
                    where.append("','").append(((AdBlock) this.mAdBlocks.get(i2)).getPkgName());
                }
                where.append("')");
                HwLog.i(AdModelImpl.TAG, "cv=" + cv + ", where=" + where.toString());
                try {
                    AdModelImpl.this.mAppContext.getContentResolver().update(AdBlock.CONTENT_URI, cv, where.toString(), null);
                } catch (Exception e) {
                    HwLog.i(AdModelImpl.TAG, "run Exception", e);
                }
                AdDispatcher.enablePackages(this.mAdBlocks, this.mEnable);
                this.mAdBlocks.clear();
            }
        }
    }

    public interface IDataListener {
        void onLoadCompleted(List<AdBlock> list, int i);
    }

    public AdModelImpl(Context context) {
        this.mAppContext = context;
    }

    public void loadAdBlocks(IDataListener listener) {
        this.mAdBlockListLoader = new AdBlockListLoader(listener);
        this.mAdBlockListLoader.execute(new Void[0]);
    }

    public void cancelLoad() {
        if (this.mAdBlockListLoader != null && !this.mAdBlockListLoader.isCancelled()) {
            this.mAdBlockListLoader.cancel(false);
        }
    }

    public void enableAdBlock(boolean isChecked, List<AdBlock> adBlocks) {
        for (AdBlock adBlock : adBlocks) {
            adBlock.setEnable(isChecked);
        }
        this.mHsmSingleExecutor.execute(new EnableAdBlockRunnable(isChecked, adBlocks));
    }
}
