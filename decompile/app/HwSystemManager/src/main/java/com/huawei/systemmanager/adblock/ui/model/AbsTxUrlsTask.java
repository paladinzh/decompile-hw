package com.huawei.systemmanager.adblock.ui.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

public abstract class AbsTxUrlsTask extends AsyncTask<Void, Void, Integer[]> {
    private static final String TAG = "AdBlock_AbsTxUrlsTask";
    private final Context mContext;
    private List<AdBlock> mNeedCreate = Lists.newArrayList();
    private List<AdBlock> mNeedUpdate = Lists.newArrayList();
    private Map<String, ScanResultEntity> mScanResultsBanUrl = new HashMap();
    private final boolean mScanSuccess;

    private class AntiVirusBanUrlsRunnable implements Runnable {
        private AntiVirusBanUrlsRunnable() {
        }

        public void run() {
            if (!AbsTxUrlsTask.this.mNeedUpdate.isEmpty() || !AbsTxUrlsTask.this.mNeedCreate.isEmpty()) {
                for (AdBlock adBlock : AbsTxUrlsTask.this.mNeedUpdate) {
                    ContentValues values = new ContentValues();
                    values.put(AdBlockColumns.COLUMN_TX_URLS, adBlock.getTxUrls());
                    adBlock.update(AbsTxUrlsTask.this.mContext, values);
                }
                for (AdBlock adBlock2 : AbsTxUrlsTask.this.mNeedCreate) {
                    try {
                        adBlock2.save(AbsTxUrlsTask.this.mContext);
                    } catch (RuntimeException e) {
                        HwLog.w(AbsTxUrlsTask.TAG, "run RuntimeException pkg=" + adBlock2.getPkgName(), e);
                    }
                }
                AdUtils.dispatchAll(AbsTxUrlsTask.this.mContext);
            }
        }
    }

    protected abstract void onTaskFinished(int i, int i2);

    public AbsTxUrlsTask(Context context, boolean scanSuccess, Map<String, ScanResultEntity> scanResultsBanUrl) {
        this.mContext = context;
        this.mScanSuccess = scanSuccess;
        if (scanResultsBanUrl != null) {
            this.mScanResultsBanUrl.putAll(scanResultsBanUrl);
        }
    }

    protected Integer[] doInBackground(Void... params) {
        Iterator<AdBlock> iterator = AdBlock.getAllAdBlocks(this.mContext).iterator();
        int checkedCount = 0;
        while (iterator.hasNext()) {
            String urls;
            boolean z;
            AdBlock adBlock = (AdBlock) iterator.next();
            ScanResultEntity entity = (ScanResultEntity) this.mScanResultsBanUrl.get(adBlock.getPkgName());
            if (entity != null) {
                this.mScanResultsBanUrl.remove(entity.getPackageName());
                urls = getBanUrls(entity);
                z = true;
            } else {
                urls = "";
                z = this.mScanSuccess;
            }
            if (z && !TextUtils.equals(adBlock.getTxUrls(), urls)) {
                adBlock.setTxUrls(urls);
                this.mNeedUpdate.add(adBlock);
            }
            if (!adBlock.hasAd()) {
                iterator.remove();
            } else if (adBlock.isEnable()) {
                checkedCount++;
            }
        }
        for (ScanResultEntity entity2 : this.mScanResultsBanUrl.values()) {
            try {
                PackageInfo info = PackageManagerWrapper.getPackageInfo(this.mContext.getPackageManager(), entity2.getPackageName(), 8192);
                AdBlock newAdBlock = new AdBlock(entity2.getPackageName(), info.versionCode, info.versionName);
                newAdBlock.setTxUrls(getBanUrls(entity2));
                this.mNeedCreate.add(newAdBlock);
            } catch (NameNotFoundException e) {
                HwLog.i(TAG, "app not installed for " + entity2.getPackageName());
            }
        }
        AbsTxUrlsTask absTxUrlsTask = this;
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new AntiVirusBanUrlsRunnable());
        return new Integer[]{Integer.valueOf(adBlocks.size()), Integer.valueOf(checkedCount)};
    }

    protected void onPostExecute(Integer[] result) {
        super.onPostExecute(result);
        onTaskFinished(result[0].intValue(), result[1].intValue());
    }

    private String getBanUrls(ScanResultEntity entity) {
        JSONArray array = new JSONArray();
        for (String url : entity.mBanUrls) {
            array.put(url);
        }
        return array.toString();
    }
}
