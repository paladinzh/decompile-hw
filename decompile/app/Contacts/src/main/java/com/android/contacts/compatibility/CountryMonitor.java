package com.android.contacts.compatibility;

import android.content.Context;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.os.Looper;
import java.util.Locale;

public class CountryMonitor {
    private static volatile CountryMonitor mCountryMonitor;
    final CountryDetector countryDetector;
    private String mCurrentCountryIso;

    private CountryMonitor(Context context) {
        this.countryDetector = (CountryDetector) context.getSystemService("country_detector");
        loadCountryIso();
        this.countryDetector.addCountryListener(new CountryListener() {
            public void onCountryDetected(Country country) {
                CountryMonitor.this.mCurrentCountryIso = country.getCountryIso();
            }
        }, Looper.getMainLooper());
    }

    public static CountryMonitor getInstance(Context context) {
        if (mCountryMonitor == null) {
            synchronized (CountryMonitor.class) {
                if (mCountryMonitor == null) {
                    mCountryMonitor = new CountryMonitor(context);
                }
            }
        }
        return mCountryMonitor;
    }

    public String getCountryIso() {
        return this.mCurrentCountryIso;
    }

    public void loadCountryIso() {
        if (this.countryDetector.detectCountry() != null) {
            this.mCurrentCountryIso = this.countryDetector.detectCountry().getCountryIso();
        } else {
            this.mCurrentCountryIso = Locale.getDefault().getCountry();
        }
    }

    public static String getCountryNameByCountryIso(String countryIso) {
        if (countryIso == null) {
            return null;
        }
        return new Locale("", countryIso).getDisplayCountry();
    }
}
