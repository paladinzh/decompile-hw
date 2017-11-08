package android.content.res;

import android.content.res.AbsResourcesImpl.ThemeColor;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayAdjustments;
import com.huawei.android.text.HwTextUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.ZipFileCache;
import java.io.File;
import java.util.Locale;

public class HwResources extends Resources {
    private static final boolean DEBUG_DRAWABLE = false;
    private static final String DRAWABLE_FHD = "drawable-xxhdpi";
    private static final String FRAMEWORK_RES = "framework-res";
    private static final String FRAMEWORK_RES_EXT = "framework-res-hwext";
    static final String TAG = "HwResources";
    private static ResourcesUtils resUtils = ((ResourcesUtils) EasyInvokeFactory.getInvokeUtils(ResourcesUtils.class));
    private static boolean sSerbiaLocale = false;
    protected String mPackageName;
    private boolean system;

    public ResourcesImpl getImpl() {
        return super.getImpl();
    }

    public void setPackageName(String name) {
        this.mPackageName = name;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Bitmap getThemeBitmap(TypedValue value, int id, Rect padding) throws NotFoundException {
        if (resUtils.getPreloading(getImpl())) {
            return null;
        }
        String packageName = getImpl().getHwResourcesImpl().getThemeResource(id).packageName;
        if (value.string == null) {
            return null;
        }
        String file = value.string.toString().replaceFirst("-v\\d+/", "/");
        if (file.isEmpty()) {
            return null;
        }
        if (packageName.indexOf(FRAMEWORK_RES) >= 0 && file.indexOf("_holo") >= 0) {
            return null;
        }
        Bitmap bmp;
        int themeDensity;
        String dir;
        boolean isLand = file.indexOf("-land") >= 0;
        boolean isFramework = packageName.equals(FRAMEWORK_RES);
        boolean isHwFramework = packageName.equals(FRAMEWORK_RES_EXT);
        if (!((!isFramework && !isHwFramework) || this.mPackageName == null || this.mPackageName.isEmpty())) {
            ZipFileCache frameworkZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), this.mPackageName);
            String key = packageName + "/" + file;
            if (frameworkZipFileCache != null) {
                bmp = frameworkZipFileCache.getBitmapEntry(this, value, key, padding);
                if (bmp == null && !file.contains(DRAWABLE_FHD)) {
                    frameworkZipFileCache.initResDirInfo();
                    int index = isFramework ? isLand ? 3 : 2 : isLand ? 5 : 4;
                    themeDensity = frameworkZipFileCache.getDrawableDensity(index);
                    dir = frameworkZipFileCache.getDrawableDir(index);
                    if (!(themeDensity == -1 || dir == null)) {
                        bmp = frameworkZipFileCache.getBitmapEntry(this, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
                    }
                }
                if (bmp != null) {
                    return bmp;
                }
            }
        }
        ZipFileCache packageZipFileCache = ZipFileCache.getAndCheckCachedZipFile(getImpl().getHwResourcesImpl().getThemeDir(), packageName);
        if (packageZipFileCache == null) {
            return null;
        }
        bmp = packageZipFileCache.getBitmapEntry(this, value, file, padding);
        if (bmp == null && !file.contains(DRAWABLE_FHD)) {
            packageZipFileCache.initResDirInfo();
            index = file.indexOf("-land") >= 0 ? 1 : 0;
            themeDensity = packageZipFileCache.getDrawableDensity(index);
            dir = packageZipFileCache.getDrawableDir(index);
            if (!(themeDensity == -1 || dir == null)) {
                bmp = packageZipFileCache.getBitmapEntry(this, value, dir + File.separator + file.substring(file.lastIndexOf(File.separator) + 1), padding);
            }
        }
        return bmp;
    }

    public Bitmap getThemeBitmap(TypedValue value, int id) throws NotFoundException {
        return getThemeBitmap(value, id, null);
    }

    protected ColorStateList loadColorStateList(TypedValue value, int id, Theme theme) throws NotFoundException {
        boolean isThemeColor = false;
        ColorStateList csl = null;
        if (HwThemeManager.isHwTheme() && id != 0) {
            ThemeColor colorValue = getThemeColor(value, id);
            if (colorValue != null) {
                isThemeColor = colorValue.mIsThemed;
                csl = ColorStateList.valueOf(colorValue.mColor);
            }
        }
        if (isThemeColor) {
            return csl;
        }
        return super.loadColorStateList(value, id, theme);
    }

    public int getColor(int id, Theme theme) throws NotFoundException {
        TypedValue value = obtainTempTypedValue();
        try {
            getValue(id, value, true);
            int color;
            if (value.type < 16 || value.type > 31) {
                color = super.getColor(id, theme);
                releaseTempTypedValue(value);
                return color;
            }
            ThemeColor themecolor = getThemeColor(value, id);
            if (!HwThemeManager.isHwTheme() || themecolor == null) {
                color = value.data;
                releaseTempTypedValue(value);
                return color;
            }
            int colorVaue = themecolor.mColor;
            return colorVaue;
        } finally {
            releaseTempTypedValue(value);
        }
    }

    public ThemeColor getThemeColor(TypedValue value, int id) {
        ResourcesImpl impl = getImpl();
        if (impl == null) {
            return null;
        }
        impl.getHwResourcesImpl().setPackageName(getPackageName());
        return impl.getHwResourcesImpl().getThemeColor(value, id);
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        this(assets, metrics, config, new DisplayAdjustments(), null);
    }

    public HwResources(ClassLoader classLoader) {
        super(classLoader);
        this.system = false;
        this.mPackageName = null;
    }

    public HwResources(boolean system) {
        this.system = false;
        this.mPackageName = null;
        this.system = system;
        getImpl().getHwResourcesImpl().initResource();
    }

    public HwResources(AssetManager assets, DisplayMetrics metrics, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        super(assets, metrics, config, displayAdjustments);
        this.system = false;
        this.mPackageName = null;
        setIsSRLocale("sr".equals(Locale.getDefault().getLanguage()));
        getImpl().getHwResourcesImpl().checkChangedNameFile();
    }

    public HwResources() {
        this.system = false;
        this.mPackageName = null;
        getImpl().getHwResourcesImpl().initResource();
    }

    public Drawable getDrawableForDynamic(String packageName, String iconName) throws NotFoundException {
        return getImpl().getHwResourcesImpl().getDrawableForDynamic(this, packageName, iconName);
    }

    protected CharSequence serbianSyrillic2Latin(CharSequence res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    protected CharSequence[] serbianSyrillic2Latin(CharSequence[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    protected String serbianSyrillic2Latin(String res) {
        if (sSerbiaLocale) {
            return HwTextUtils.serbianSyrillic2Latin(res);
        }
        return res;
    }

    protected String[] serbianSyrillic2Latin(String[] res) {
        if (sSerbiaLocale) {
            for (int i = 0; i < res.length; i++) {
                res[i] = HwTextUtils.serbianSyrillic2Latin(res[i]);
            }
        }
        return res;
    }

    protected boolean isSRLocale() {
        return sSerbiaLocale;
    }

    protected static void setIsSRLocale(boolean isSerbia) {
        if (sSerbiaLocale != isSerbia) {
            sSerbiaLocale = isSerbia;
        }
    }

    public CharSequence getText(int id) throws NotFoundException {
        CharSequence res = getImpl().mAssets.getResourceText(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
    }

    public CharSequence getText(int id, CharSequence def) {
        CharSequence res = null;
        if (id != 0) {
            res = getImpl().mAssets.getResourceText(id);
        }
        if (res == null) {
            res = def;
        }
        return serbianSyrillic2Latin(res);
    }

    public CharSequence[] getTextArray(int id) throws NotFoundException {
        CharSequence[] res = getImpl().mAssets.getResourceTextArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
    }

    public String[] getStringArray(int id) throws NotFoundException {
        String[] res = getImpl().mAssets.getResourceStringArray(id);
        if (res != null) {
            return serbianSyrillic2Latin(res);
        }
        throw new NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
    }
}
