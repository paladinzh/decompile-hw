package com.android.gallery3d.data;

public final class DataSourceType {
    private static final Path GALLERY_ROOT = Path.fromString("/gallery");
    private static final Path LOCAL_ROOT = Path.fromString("/local");
    private static final Path MTP_ROOT = Path.fromString("/mtp");
    private static final Path PHOTOSHARE_ROOT = Path.fromString("/photoshare");
    private static final Path PICASA_ROOT = Path.fromString("/picasa");
    private static final Path VIRTUAL_ROOT = Path.fromString("/virtual");

    public static int identifySourceType(MediaSet set) {
        if (set == null) {
            return 0;
        }
        Path prefix = set.getPath().getPrefixPath();
        if (prefix == PICASA_ROOT) {
            return 2;
        }
        if (prefix == MTP_ROOT) {
            return 3;
        }
        if (prefix == LOCAL_ROOT) {
            return 1;
        }
        if (prefix == VIRTUAL_ROOT) {
            return 16;
        }
        if (prefix == GALLERY_ROOT) {
            return 20;
        }
        if (prefix == PHOTOSHARE_ROOT) {
            switch (set.getAlbumType()) {
                case 1:
                    return 12;
                case 2:
                case 3:
                case 7:
                    return 13;
                case 5:
                    return 17;
                case 6:
                    return 18;
                case 10:
                    return 19;
            }
        }
        return 0;
    }
}
