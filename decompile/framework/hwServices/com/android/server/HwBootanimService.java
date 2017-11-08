package com.android.server;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.HwBootAnimationOeminfo;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipFile;

public class HwBootanimService {
    static final String BOOTANIMATION = "bootanimation";
    static final String SHUTANIMATIN = "shutdownanimation";
    static final int SWITCH_CLOSE = 2;
    static final int SWITCH_OPEN = 1;
    static final String TAG = "HwBootanimService";
    static final int VALUEE_RROR = -2;
    static final String aniamtion_path = "/system/media";
    static final String custBootSoundFile = "/data/cust/media/audio/animationsounds/bootSound.ogg";
    static final String custShutSoundFile = "/data/cust/media/audio/animationsounds/shutSound.ogg";
    static final String cust_aniamtion_path = "/data/cust/media";
    static final String cust_preinstall_aniamtion_path = "/cust/preinstalled/public/media";
    private static volatile HwBootanimService mInstance = null;
    private static boolean mIsBootOrShutdownSoundCapable = false;
    static final String oem_aniamtion_path = "/oem/media";
    static final String systemBootSoundFile = "/system/media/audio/animationsounds/bootSound.ogg";
    static final String systemShutSoundFile = "/system/media/audio/animationsounds/shutSound.ogg";
    private Context mContext;

    public HwBootanimService(Context context) {
        this.mContext = context;
    }

    public static synchronized HwBootanimService getInstance(Context context) {
        HwBootanimService hwBootanimService;
        synchronized (HwBootanimService.class) {
            if (mInstance == null) {
                mInstance = new HwBootanimService(context);
            }
            hwBootanimService = mInstance;
        }
        return hwBootanimService;
    }

    public void switchBootOrShutSound(String openOrClose) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", null);
        int value = -2;
        if (openOrClose.equals("open")) {
            value = 1;
        } else if (openOrClose.equals("close")) {
            value = 2;
        }
        if (-2 == value) {
            Slog.d(TAG, "switchBootOrShutSound parameter error");
        } else {
            HwBootAnimationOeminfo.setBootAnimSoundSwitch(value);
        }
    }

    public boolean isBootOrShutdownSoundCapable() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", null);
        Slog.d(TAG, "Boot or Shutdown sound is Capable :" + mIsBootOrShutdownSoundCapable);
        return mIsBootOrShutdownSoundCapable;
    }

    private static void setIsBootOrShutdownSoundCapable(boolean isBootOrShutdownSoundCapable) {
        mIsBootOrShutdownSoundCapable = isBootOrShutdownSoundCapable;
    }

    public void isBootOrShutdownSoundCapableForService() {
        setIsBootOrShutdownSoundCapable(isBootOrShutdownSoundCapableInter());
    }

    private boolean isBootOrShutdownSoundCapableInter() {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", PPPOEStateMachine.PHASE_DEAD);
        if (isPlaySound(mccmnc, BOOTANIMATION) && isBootSoundExist(mccmnc)) {
            return true;
        }
        if (isPlaySound(mccmnc, SHUTANIMATIN) && isShutSoundExist(mccmnc)) {
            return true;
        }
        return false;
    }

    private boolean isBootSoundExist(String mccmnc) {
        String BootSoundPath = null;
        boolean doseBootSoundFileExist = false;
        boolean dosemccmncBootSoundFileExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/audio/animationsounds/bootSound.ogg", 0) != null) {
                doseBootSoundFileExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            BootSoundPath = "media/audio/animationsounds/bootSound_" + mccmnc + ".ogg";
        }
        if (BootSoundPath != null) {
            try {
                if (HwCfgFilePolicy.getCfgFile(BootSoundPath, 0) != null) {
                    dosemccmncBootSoundFileExist = true;
                }
            } catch (NoClassDefFoundError e2) {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
        }
        if (doseBootSoundFileExist || (BootSoundPath != null && dosemccmncBootSoundFileExist)) {
            return true;
        }
        boolean ifFileExsit = false;
        File custBootSoundF = new File(custBootSoundFile);
        File systemBootSoundF = new File(systemBootSoundFile);
        File file = null;
        String mccmncBootSoundPath = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            mccmncBootSoundPath = "/data/cust/media/audio/animationsounds/bootSound_" + mccmnc + ".ogg";
        }
        if (mccmncBootSoundPath != null) {
            file = new File(mccmncBootSoundPath);
        }
        if (isFileExist(custBootSoundF, systemBootSoundF, file)) {
            ifFileExsit = true;
        }
        return ifFileExsit;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isPlaySound(String mccmnc, String animtionName) {
        File animation = null;
        if (!PPPOEStateMachine.PHASE_DEAD.equals(mccmnc)) {
            animation = HwCfgFilePolicy.getCfgFile(String.format("media/%s_%s.zip", new Object[]{animtionName, mccmnc}), 0);
        }
        String animPath = String.format("media/%s.zip", new Object[]{animtionName});
        String decrypt = SystemProperties.get("vold.decrypt");
        String encryptedAnimPath = String.format("media/%s-encrypted.zip", new Object[]{animtionName});
        boolean equals = PPPOEStateMachine.PHASE_DEAD.equals(decrypt) ? "trigger_restart_min_framework".equals(decrypt) : true;
        if (animation == null) {
            try {
                animation = HwCfgFilePolicy.getCfgFile(animPath, 0);
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
        if (animation == null && BOOTANIMATION.equals(animtionName)) {
            animation = getCustAnimation(mccmnc, BOOTANIMATION);
        }
        if (animation == null && equals) {
            try {
                animation = HwCfgFilePolicy.getCfgFile(encryptedAnimPath, 0);
            } catch (NoClassDefFoundError e2) {
                return false;
            }
        }
        if (animation == null && SHUTANIMATIN.equals(animtionName)) {
            animation = getCustAnimation(mccmnc, SHUTANIMATIN);
        }
        if (animation == null) {
            File animationTemp;
            String oemAnimPath = String.format("%s/%s.zip", new Object[]{oem_aniamtion_path, animtionName});
            animPath = String.format("%s/%s.zip", new Object[]{aniamtion_path, animtionName});
            encryptedAnimPath = String.format("%s/%s-encrypted.zip", new Object[]{aniamtion_path, animtionName});
            if (equals) {
                animationTemp = new File(encryptedAnimPath);
            }
            animationTemp = new File(oemAnimPath);
            if (!animationTemp.exists()) {
                animationTemp = new File(animPath);
            }
            animation = animationTemp;
        }
        if (parseBootanimationZip(animation)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseBootanimationZip(File animation) {
        ZipFile bootanimationZip;
        BufferedReader br;
        Throwable th;
        ZipFile zipFile = null;
        if (animation != null && animation.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                bootanimationZip = new ZipFile(animation);
                try {
                    inputStream = bootanimationZip.getInputStream(bootanimationZip.getEntry("desc.txt"));
                    br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while (true) {
                        try {
                            String cha = br.readLine();
                            if (cha != null) {
                                if (cha.endsWith(PPPOEStateMachine.PHASE_INITIALIZE) && cha.split("[^\\w\\d]").length == 6) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } catch (IOException e) {
                            bufferedReader = br;
                            zipFile = bootanimationZip;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = br;
                            zipFile = bootanimationZip;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            Slog.w(TAG, "read close error");
                        } catch (Throwable th3) {
                        }
                    }
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e3) {
                            Slog.w(TAG, "br close error");
                        } catch (Throwable th4) {
                        }
                    }
                    if (bootanimationZip != null) {
                        try {
                            bootanimationZip.close();
                        } catch (IOException e4) {
                            Slog.w(TAG, "bootanimationZip close error");
                        } catch (Throwable th5) {
                        }
                    }
                } catch (IOException e5) {
                    zipFile = bootanimationZip;
                    try {
                        Slog.d(TAG, "PlaySound IO error");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e6) {
                                Slog.w(TAG, "read close error");
                            } catch (Throwable th6) {
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e7) {
                                Slog.w(TAG, "br close error");
                            } catch (Throwable th7) {
                            }
                        }
                        if (zipFile != null) {
                            try {
                                zipFile.close();
                            } catch (IOException e8) {
                                Slog.w(TAG, "bootanimationZip close error");
                            } catch (Throwable th8) {
                            }
                        }
                        return false;
                    } catch (Throwable th9) {
                        th = th9;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e9) {
                                Slog.w(TAG, "read close error");
                            } catch (Throwable th10) {
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e10) {
                                Slog.w(TAG, "br close error");
                            } catch (Throwable th11) {
                            }
                        }
                        if (zipFile != null) {
                            try {
                                zipFile.close();
                            } catch (IOException e11) {
                                Slog.w(TAG, "bootanimationZip close error");
                            } catch (Throwable th12) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    zipFile = bootanimationZip;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (zipFile != null) {
                        zipFile.close();
                    }
                    throw th;
                }
            } catch (IOException e12) {
                Slog.d(TAG, "PlaySound IO error");
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
                return false;
            }
        }
        return false;
        if (br != null) {
            try {
                br.close();
            } catch (IOException e13) {
                Slog.w(TAG, "br close error");
            } catch (Throwable th14) {
            }
        }
        if (bootanimationZip != null) {
            try {
                bootanimationZip.close();
            } catch (IOException e14) {
                Slog.w(TAG, "bootanimationZip close error");
            } catch (Throwable th15) {
            }
        }
        return true;
        if (bootanimationZip != null) {
            bootanimationZip.close();
        }
        return true;
        return true;
    }

    private File getCustAnimation(String mccmnc, String animtionName) {
        String custAnimPath = String.format("%s/%s.zip", new Object[]{cust_aniamtion_path, animtionName});
        String custPreinstallAnimPath = String.format("%s/%s.zip", new Object[]{cust_preinstall_aniamtion_path, animtionName});
        File file = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            file = new File(String.format("%s/%s_%s.zip", new Object[]{cust_aniamtion_path, animtionName, mccmnc}));
        }
        if (file == null || !file.exists()) {
            file = new File(custAnimPath);
            if (!file.exists()) {
                file = new File(custPreinstallAnimPath);
                if (file.exists()) {
                    return file;
                }
                return null;
            }
        }
        return file;
    }

    private boolean isShutSoundExist(String mccmnc) {
        String ShutSoundPath = null;
        boolean doseShutSoundFileExist = false;
        boolean dosemccmncShutSoundFileExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/audio/animationsounds/shutSound.ogg", 0) != null) {
                doseShutSoundFileExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            ShutSoundPath = "media/audio/animationsounds/shutSound_" + mccmnc + ".ogg";
        }
        if (ShutSoundPath != null) {
            try {
                if (HwCfgFilePolicy.getCfgFile(ShutSoundPath, 0) != null) {
                    dosemccmncShutSoundFileExist = true;
                }
            } catch (NoClassDefFoundError e2) {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
        }
        if (doseShutSoundFileExist || (ShutSoundPath != null && dosemccmncShutSoundFileExist)) {
            return true;
        }
        boolean ifFileExsit = false;
        File custShutSoundF = new File(custShutSoundFile);
        File systemShutSoundF = new File(systemShutSoundFile);
        File file = null;
        String mccmncShutSoundPath = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            mccmncShutSoundPath = "/data/cust/media/audio/animationsounds/shutSound_" + mccmnc + ".ogg";
        }
        if (mccmncShutSoundPath != null) {
            file = new File(mccmncShutSoundPath);
        }
        if (isFileExist(custShutSoundF, systemShutSoundF, file)) {
            ifFileExsit = true;
        }
        return ifFileExsit;
    }

    private boolean isFileExist(File custSoundF, File systemSoundF, File mccmncSoundFile) {
        if (custSoundF.exists() || systemSoundF.exists()) {
            return true;
        }
        return mccmncSoundFile != null ? mccmncSoundFile.exists() : false;
    }
}
