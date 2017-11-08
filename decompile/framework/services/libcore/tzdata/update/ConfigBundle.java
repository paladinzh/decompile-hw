package libcore.tzdata.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ConfigBundle {
    private static final int BUFFER_SIZE = 8192;
    public static final String CHECKSUMS_FILE_NAME = "checksums";
    public static final String ICU_DATA_FILE_NAME = "icu/icu_tzdata.dat";
    public static final String TZ_DATA_VERSION_FILE_NAME = "tzdata_version";
    public static final String ZONEINFO_FILE_NAME = "tzdata";
    private final byte[] bytes;

    public ConfigBundle(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBundleBytes() {
        return this.bytes;
    }

    public void extractTo(File targetDir) throws IOException {
        extractZipSafely(new ByteArrayInputStream(this.bytes), targetDir, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void extractZipSafely(InputStream is, File targetDir, boolean makeWorldReadable) throws IOException {
        Throwable th;
        Throwable th2;
        Throwable th3;
        FileUtils.ensureDirectoriesExist(targetDir, makeWorldReadable);
        Throwable th4 = null;
        ZipInputStream zipInputStream = null;
        try {
            ZipInputStream zipInputStream2 = new ZipInputStream(is);
            try {
                FileOutputStream fileOutputStream;
                byte[] buffer = new byte[8192];
                while (true) {
                    ZipEntry entry = zipInputStream2.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    File entryFile = FileUtils.createSubFile(targetDir, entry.getName());
                    if (entry.isDirectory()) {
                        FileUtils.ensureDirectoriesExist(entryFile, makeWorldReadable);
                    } else {
                        if (!entryFile.getParentFile().exists()) {
                            FileUtils.ensureDirectoriesExist(entryFile.getParentFile(), makeWorldReadable);
                        }
                        th = null;
                        fileOutputStream = null;
                        try {
                            FileOutputStream fos = new FileOutputStream(entryFile);
                            while (true) {
                                try {
                                    int count = zipInputStream2.read(buffer);
                                    if (count == -1) {
                                        break;
                                    }
                                    fos.write(buffer, 0, count);
                                } catch (Throwable th5) {
                                    th2 = th5;
                                    fileOutputStream = fos;
                                }
                            }
                            fos.getFD().sync();
                            if (fos != null) {
                                fos.close();
                            }
                            if (th != null) {
                                throw th;
                            } else if (makeWorldReadable) {
                                FileUtils.makeWorldReadable(entryFile);
                            }
                        } catch (Throwable th6) {
                            th2 = th6;
                        }
                    }
                }
                if (zipInputStream2 != null) {
                    try {
                        zipInputStream2.close();
                    } catch (Throwable th7) {
                        th4 = th7;
                    }
                }
                if (th4 != null) {
                    throw th4;
                }
                return;
                try {
                    throw th2;
                } catch (Throwable th8) {
                    th3 = th8;
                    th8 = th2;
                    th2 = th3;
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (th8 != null) {
                    throw th8;
                } else {
                    throw th2;
                }
            } catch (Throwable th9) {
                th2 = th9;
                th8 = null;
                zipInputStream = zipInputStream2;
            }
        } catch (Throwable th10) {
            th2 = th10;
            th8 = null;
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Throwable th42) {
                    if (th8 == null) {
                        th8 = th42;
                    } else if (th8 != th42) {
                        th8.addSuppressed(th42);
                    }
                }
            }
            if (th8 != null) {
                throw th8;
            }
            throw th2;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(this.bytes, ((ConfigBundle) o).bytes);
    }
}
