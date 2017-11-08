package huawei.android.os;

public class HwBootanimManager {
    private static final String TAG = "HwBootanimManager";
    private static volatile HwBootanimManager mInstance = null;

    public static synchronized HwBootanimManager getInstance() {
        HwBootanimManager hwBootanimManager;
        synchronized (HwBootanimManager.class) {
            if (mInstance == null) {
                mInstance = new HwBootanimManager();
            }
            hwBootanimManager = mInstance;
        }
        return hwBootanimManager;
    }

    public void switchBootOrShutSound(String openOrClose) {
        HwGeneralManager.getInstance().switchBootOrShutSound(openOrClose);
    }

    public boolean isBootOrShutdownSoundCapable() {
        return HwGeneralManager.getInstance().isBootOrShutdownSoundCapable();
    }
}
