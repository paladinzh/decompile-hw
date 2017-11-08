package com.google.android.gms.maps.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/* compiled from: Unknown */
public abstract class UrlTileProvider implements TileProvider {
    private final int zzoG;
    private final int zzoH;

    public UrlTileProvider(int width, int height) {
        this.zzoG = width;
        this.zzoH = height;
    }

    private static long zza(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[4096];
        long j = 0;
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return j;
            }
            outputStream.write(bArr, 0, read);
            j += (long) read;
        }
    }

    private static byte[] zzl(InputStream inputStream) throws IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        zza(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public final Tile getTile(int x, int y, int zoom) {
        URL tileUrl = getTileUrl(x, y, zoom);
        if (tileUrl == null) {
            return NO_TILE;
        }
        Tile tile;
        try {
            tile = new Tile(this.zzoG, this.zzoH, zzl(tileUrl.openStream()));
        } catch (IOException e) {
            tile = null;
        }
        return tile;
    }

    public abstract URL getTileUrl(int i, int i2, int i3);
}
