package com.android.gallery3d.exif;

class JpegHeader {
    JpegHeader() {
    }

    public static final boolean isSofMarker(short marker) {
        if (marker < (short) -64 || marker > (short) -49 || marker == (short) -60 || marker == (short) -56 || marker == (short) -52) {
            return false;
        }
        return true;
    }
}
