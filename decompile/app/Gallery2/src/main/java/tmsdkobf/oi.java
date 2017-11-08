package tmsdkobf;

import com.huawei.watermark.manager.parse.util.WMWeatherService;

/* compiled from: Unknown */
public class oi {
    public static final int bU(int i) {
        return i % 20;
    }

    public static final int bV(int i) {
        return (i % 10000) - bU(i);
    }

    public static final int bW(int i) {
        return ((i % 1000000) - bV(i)) - bU(i);
    }

    public static int bX(int i) {
        switch (i) {
            case -170000:
                return i - 5;
            case -160000:
                return i - 6;
            case -150000:
                return i - 3;
            case -140000:
                return i - 3;
            case -130000:
                return i - 4;
            case -120000:
                return i - 3;
            case -110000:
                return i - 3;
            case -100000:
                return i - 3;
            case -90000:
                return i - 3;
            case -80000:
                return i - 3;
            case -70000:
                return i - 3;
            case -60000:
                return i - 3;
            case -50000:
                return i - 4;
            case -40000:
                return i - 3;
            case -30000:
                return i - 3;
            case -20000:
                return i - 3;
            case WMWeatherService.TEMPERATURE_UNKOWN /*-10000*/:
                return i - 3;
            default:
                return i;
        }
    }
}
