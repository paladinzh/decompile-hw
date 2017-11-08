package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.SystemProperties;
import fyusion.vislib.BuildConfig;

public class HwCustCoverWeatherViewImpl extends HwCustCoverWeatherView {
    private static final String CELSIUS_UNIT = "Celsius";
    private static final String FAHRENHEIT_UNIT = "Fahrenheit";
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private Context context;

    public HwCustCoverWeatherViewImpl(Context context) {
        super(context);
        this.context = context;
    }

    public static final int getResIdentifier(Context context, String name, String postfix, String defType, String defPackage, int defResIdentifier) {
        int identifier = context.getResources().getIdentifier(name + postfix, defType, defPackage);
        if (identifier > 0) {
            return identifier;
        }
        return defResIdentifier;
    }

    private int getWeatherResources(boolean isNight, int resId, String suffix) {
        if (isNight) {
            return getResIdentifier(this.context, "lockscreen_weather_icon_night", suffix, "array", "com.android.systemui", resId);
        }
        return getResIdentifier(this.context, "lockscreen_weather_icon", suffix, "array", "com.android.systemui", resId);
    }

    public int getWeatherResId(boolean isNight, int resId) {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_1047x1312")) {
            return getWeatherResources(isNight, resId, "_big");
        }
        if (str.equals("_401x1920") || str.equals("_500x2560") || str.equals("_540x2560") || str.equals("_747x1920") || str.equals("_1440x2560")) {
            return getWeatherResources(isNight, resId, "_500x2560");
        }
        return resId;
    }

    private int getTempResources(String temperatureUnit, int resId, String suffix) {
        if (temperatureUnit.equals(CELSIUS_UNIT)) {
            return getResIdentifier(this.context, "weather_unit_c", suffix, "drawable", "com.android.systemui", resId);
        } else if (!temperatureUnit.equals(FAHRENHEIT_UNIT)) {
            return 0;
        } else {
            return getResIdentifier(this.context, "weather_unit_f", suffix, "drawable", "com.android.systemui", resId);
        }
    }

    public int getTempeUnitView(String temperatureUnit, int resId) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return getTempResources(temperatureUnit, resId, "_big");
        }
        return resId;
    }
}
