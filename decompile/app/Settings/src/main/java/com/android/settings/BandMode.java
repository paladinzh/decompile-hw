package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class BandMode extends Activity {
    private static final String[] BAND_NAMES = new String[]{"Automatic", "Europe", "United States", "Japan", "Australia", "Australia 2", "Cellular 800", "PCS", "Class 3 (JTACS)", "Class 4 (Korea-PCS)", "Class 5", "Class 6 (IMT2000)", "Class 7 (700Mhz-Upper)", "Class 8 (1800Mhz-Upper)", "Class 9 (900Mhz)", "Class 10 (800Mhz-Secondary)", "Class 11 (Europe PAMR 400Mhz)", "Class 15 (US-AWS)", "Class 16 (US-2500Mhz)"};
    private ListView mBandList;
    private ArrayAdapter mBandListAdapter;
    private OnItemClickListener mBandSelectionHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            BandMode.this.getWindow().setFeatureInt(5, -1);
            BandMode.this.mTargetBand = (BandListItem) parent.getAdapter().getItem(position);
            BandMode.this.mPhone.setBandMode(BandMode.this.mTargetBand.getBand(), BandMode.this.mHandler.obtainMessage(200));
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    BandMode.this.bandListLoaded(msg.obj);
                    return;
                case 200:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    BandMode.this.getWindow().setFeatureInt(5, -2);
                    if (!BandMode.this.isFinishing()) {
                        BandMode.this.displayBandSelectionResult(ar.exception);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private Phone mPhone = null;
    private DialogInterface mProgressPanel;
    private BandListItem mTargetBand = null;

    private static class BandListItem {
        private int mBandMode = 0;

        public BandListItem(int bm) {
            this.mBandMode = bm;
        }

        public int getBand() {
            return this.mBandMode;
        }

        public String toString() {
            if (this.mBandMode >= BandMode.BAND_NAMES.length) {
                return "Band mode " + this.mBandMode;
            }
            return BandMode.BAND_NAMES[this.mBandMode];
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(5);
        setContentView(2130968642);
        setTitle(getString(2131624534));
        getWindow().setLayout(-1, -2);
        this.mPhone = PhoneFactory.getDefaultPhone();
        this.mBandList = (ListView) findViewById(2131886273);
        this.mBandListAdapter = new ArrayAdapter(this, 17367043);
        this.mBandList.setAdapter(this.mBandListAdapter);
        this.mBandList.setOnItemClickListener(this.mBandSelectionHandler);
        loadBandList();
    }

    private void loadBandList() {
        this.mProgressPanel = new Builder(this).setMessage(getString(2131624535)).show();
        this.mPhone.queryAvailableBandMode(this.mHandler.obtainMessage(100));
    }

    private void bandListLoaded(AsyncResult result) {
        try {
            int i;
            if (!(this.mProgressPanel == null || isFinishing())) {
                this.mProgressPanel.dismiss();
            }
            clearList();
            boolean addBandSuccess = false;
            if (result.result != null) {
                int[] bands = result.result;
                if (bands.length == 0) {
                    Log.wtf("phone", "No Supported Band Modes");
                    return;
                }
                int size = bands[0];
                if (size > 0) {
                    for (i = 1; i <= size; i++) {
                        this.mBandListAdapter.add(new BandListItem(bands[i]));
                    }
                    addBandSuccess = true;
                }
            }
            if (!addBandSuccess) {
                for (i = 0; i < 19; i++) {
                    this.mBandListAdapter.add(new BandListItem(i));
                }
            }
            this.mBandList.requestFocus();
        } catch (RuntimeException e) {
        }
    }

    private void displayBandSelectionResult(Throwable ex) {
        String status = getString(2131624536) + " [" + this.mTargetBand.toString() + "] ";
        if (ex != null) {
            status = status + getString(2131624537);
        } else {
            status = status + getString(2131624538);
        }
        this.mProgressPanel = new Builder(this).setMessage(status).setPositiveButton(17039370, null).show();
    }

    private void clearList() {
        while (this.mBandListAdapter.getCount() > 0) {
            this.mBandListAdapter.remove(this.mBandListAdapter.getItem(0));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mProgressPanel != null && ((AlertDialog) this.mProgressPanel).isShowing()) {
            this.mProgressPanel.dismiss();
        }
    }
}
