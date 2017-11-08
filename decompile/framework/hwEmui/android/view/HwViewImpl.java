package android.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareLog;
import android.rms.iaware.FastgrabConfigReader;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.android.bastet.IBastetManager.Stub;

public class HwViewImpl implements IHwView {
    private static final String ATTR_ANIMATION_VIEW = "animationRootView";
    private static final String ATTR_BOOST_VIEW = "boostRootView";
    private static final String ATTR_SWITCH = "switch";
    private static final String TAG = "HwViewImpl";
    private static HwViewImpl mInstance = null;

    public static synchronized HwViewImpl getDefault() {
        HwViewImpl hwViewImpl;
        synchronized (HwViewImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewImpl();
            }
            hwViewImpl = mInstance;
        }
        return hwViewImpl;
    }

    private HwViewImpl() {
    }

    public boolean cancelAnimation(View view, Context context) {
        if (!checkView(view, context, ATTR_ANIMATION_VIEW)) {
            return false;
        }
        AwareLog.i(TAG, "LuckyMoney Animation Canceled !");
        return true;
    }

    public void onClick(View view, Context context) {
        if (checkView(view, context, ATTR_BOOST_VIEW)) {
            cpuBoost(context);
        }
    }

    private boolean checkView(View view, Context context, String tagName) {
        if (view == null) {
            return false;
        }
        FastgrabConfigReader fastgrabConfigReader = FastgrabConfigReader.getInstance(context);
        if (fastgrabConfigReader != null && fastgrabConfigReader.getInt(ATTR_SWITCH) == 1) {
            String rootView = fastgrabConfigReader.getString(tagName);
            if (!(rootView == null || rootView.isEmpty())) {
                View root = view.getRootView();
                return root != null && root.toString().contains(rootView);
            }
        }
    }

    private void cpuBoost(Context context) {
        if (context != null) {
            IBastetManager bastet = Stub.asInterface(ServiceManager.getService("BastetService"));
            if (bastet != null) {
                try {
                    ApplicationInfo ai = context.getApplicationInfo();
                    if (ai != null) {
                        bastet.indicateAction(0, 100, ai.uid);
                        AwareLog.i(TAG, "cpu boost on uid = " + ai.uid);
                    }
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "failed calling BastetService");
                }
            }
        }
    }
}
