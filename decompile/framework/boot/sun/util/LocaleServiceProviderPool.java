package sun.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;
import libcore.icu.ICU;
import sun.util.logging.PlatformLogger;
import sun.util.resources.OpenListResourceBundle;

public final class LocaleServiceProviderPool {
    private static volatile List<Locale> availableJRELocales = null;
    private static Locale locale_ja_JP_JP = new Locale("ja", "JP", "JP");
    private static Locale locale_th_TH_TH = new Locale("th", "TH", "TH");
    private static ConcurrentMap<Class<? extends LocaleServiceProvider>, LocaleServiceProviderPool> poolOfPools = new ConcurrentHashMap();
    private Set<Locale> availableLocales = null;
    private Set<Locale> providerLocales = null;
    private Set<LocaleServiceProvider> providers = new LinkedHashSet();
    private Map<Locale, LocaleServiceProvider> providersCache = new ConcurrentHashMap();

    public interface LocalizedObjectGetter<P, S> {
        S getObject(P p, Locale locale, String str, Object... objArr);
    }

    private static class AllAvailableLocales {
        static final Locale[] allAvailableLocales;

        private AllAvailableLocales() {
        }

        static {
            Class<LocaleServiceProvider>[] providerClasses = new Class[]{BreakIteratorProvider.class, CollatorProvider.class, DateFormatProvider.class, DateFormatSymbolsProvider.class, DecimalFormatSymbolsProvider.class, NumberFormatProvider.class, CurrencyNameProvider.class, LocaleNameProvider.class, TimeZoneNameProvider.class};
            Locale[] allLocales = ICU.getAvailableLocales();
            Set<Locale> all = new HashSet(allLocales.length);
            for (Locale locale : allLocales) {
                all.add(LocaleServiceProviderPool.getLookupLocale(locale));
            }
            for (Class<LocaleServiceProvider> providerClass : providerClasses) {
                all.addAll(LocaleServiceProviderPool.getPool(providerClass).getProviderLocales());
            }
            allAvailableLocales = (Locale[]) all.toArray(new Locale[0]);
        }
    }

    private static class NullProvider extends LocaleServiceProvider {
        private static final NullProvider INSTANCE = new NullProvider();

        private NullProvider() {
        }

        public Locale[] getAvailableLocales() {
            throw new RuntimeException("Should not get called.");
        }
    }

    public static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> providerClass) {
        LocaleServiceProviderPool pool = (LocaleServiceProviderPool) poolOfPools.get(providerClass);
        if (pool != null) {
            return pool;
        }
        LocaleServiceProviderPool newPool = new LocaleServiceProviderPool(providerClass);
        pool = (LocaleServiceProviderPool) poolOfPools.putIfAbsent(providerClass, newPool);
        if (pool == null) {
            return newPool;
        }
        return pool;
    }

    private LocaleServiceProviderPool(final Class<? extends LocaleServiceProvider> c) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() {
                    for (LocaleServiceProvider provider : ServiceLoader.loadInstalled(c)) {
                        LocaleServiceProviderPool.this.providers.add(provider);
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            config(e.toString());
        }
    }

    private static void config(String message) {
        PlatformLogger.getLogger("sun.util.LocaleServiceProviderPool").config(message);
    }

    public static Locale[] getAllAvailableLocales() {
        return (Locale[]) AllAvailableLocales.allAvailableLocales.clone();
    }

    public synchronized Locale[] getAvailableLocales() {
        Locale[] tmp;
        if (this.availableLocales == null) {
            this.availableLocales = new HashSet(getJRELocales());
            if (hasProviders()) {
                this.availableLocales.addAll(getProviderLocales());
            }
        }
        tmp = new Locale[this.availableLocales.size()];
        this.availableLocales.toArray(tmp);
        return tmp;
    }

    private synchronized Set<Locale> getProviderLocales() {
        if (this.providerLocales == null) {
            this.providerLocales = new HashSet();
            if (hasProviders()) {
                for (LocaleServiceProvider lsp : this.providers) {
                    for (Locale locale : lsp.getAvailableLocales()) {
                        this.providerLocales.add(getLookupLocale(locale));
                    }
                }
            }
        }
        return this.providerLocales;
    }

    public boolean hasProviders() {
        return !this.providers.isEmpty();
    }

    private List<Locale> getJRELocales() {
        if (availableJRELocales == null) {
            synchronized (LocaleServiceProviderPool.class) {
                if (availableJRELocales == null) {
                    Locale[] allLocales = ICU.getAvailableLocales();
                    List<Locale> tmpList = new ArrayList(allLocales.length);
                    for (Locale locale : allLocales) {
                        tmpList.add(getLookupLocale(locale));
                    }
                    availableJRELocales = tmpList;
                }
            }
        }
        return availableJRELocales;
    }

    private boolean isJRESupported(Locale locale) {
        return getJRELocales().contains(getLookupLocale(locale));
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, Object... params) {
        return getLocalizedObjectImpl(getter, locale, true, null, null, null, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, null, bundle, key, params);
    }

    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter, Locale locale, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, bundleKey, bundle, key, params);
    }

    private <P, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> getter, Locale locale, boolean isObjectProvider, String bundleKey, OpenListResourceBundle bundle, String key, Object... params) {
        if (hasProviders()) {
            S providersObj;
            if (bundleKey == null) {
                bundleKey = key;
            }
            Object locale2 = bundle != null ? bundle.getLocale() : null;
            List<Locale> lookupLocales = getLookupLocales(locale);
            Set<Locale> provLoc = getProviderLocales();
            for (int i = 0; i < lookupLocales.size(); i++) {
                Locale current = (Locale) lookupLocales.get(i);
                if (locale2 == null) {
                    if (isJRESupported(current)) {
                        break;
                    }
                } else if (current.equals(locale2)) {
                    break;
                }
                if (provLoc.contains(current)) {
                    Object lsp = findProvider(current);
                    if (lsp != null) {
                        providersObj = getter.getObject(lsp, locale, key, params);
                        if (providersObj != null) {
                            return providersObj;
                        }
                        if (isObjectProvider) {
                            config("A locale sensitive service provider returned null for a localized objects,  which should not happen.  provider: " + lsp + " locale: " + locale);
                        }
                    } else {
                        continue;
                    }
                }
            }
            while (bundle != null) {
                Locale bundleLocale = bundle.getLocale();
                if (bundle.handleGetKeys().contains(bundleKey)) {
                    return null;
                }
                P lsp2 = findProvider(bundleLocale);
                if (lsp2 != null) {
                    providersObj = getter.getObject(lsp2, locale, key, params);
                    if (providersObj != null) {
                        return providersObj;
                    }
                }
                bundle = bundle.getParent();
            }
        }
        return null;
    }

    private LocaleServiceProvider findProvider(Locale locale) {
        if (!hasProviders()) {
            return null;
        }
        if (this.providersCache.containsKey(locale)) {
            LocaleServiceProvider provider = (LocaleServiceProvider) this.providersCache.get(locale);
            if (provider != NullProvider.INSTANCE) {
                return provider;
            }
        }
        for (LocaleServiceProvider lsp : this.providers) {
            for (Locale available : lsp.getAvailableLocales()) {
                if (locale.equals(getLookupLocale(available))) {
                    LocaleServiceProvider providerInCache = (LocaleServiceProvider) this.providersCache.put(locale, lsp);
                    if (providerInCache == null) {
                        providerInCache = lsp;
                    }
                    return providerInCache;
                }
            }
        }
        this.providersCache.put(locale, NullProvider.INSTANCE);
        return null;
    }

    private static List<Locale> getLookupLocales(Locale locale) {
        return new Control() {
        }.getCandidateLocales("", locale);
    }

    private static Locale getLookupLocale(Locale locale) {
        Locale lookupLocale = locale;
        if (locale.getExtensionKeys().isEmpty() || locale.equals(locale_ja_JP_JP) || locale.equals(locale_th_TH_TH)) {
            return lookupLocale;
        }
        Builder locbld = new Builder();
        try {
            locbld.setLocale(locale);
            locbld.clearExtensions();
            return locbld.build();
        } catch (IllformedLocaleException e) {
            config("A locale(" + locale + ") has non-empty extensions, but has illformed fields.");
            return new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant());
        }
    }
}
