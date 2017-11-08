package com.huawei.gallery.barcode;

public class BarcodeScanResultItem {
    private Object[] barcodeScanResult;
    private int bitmapHeight;
    private int bitmapWidth;

    public synchronized Object[] getBarcodeScanResult() {
        Object[] scanResult;
        scanResult = new Object[this.barcodeScanResult.length];
        System.arraycopy(this.barcodeScanResult, 0, scanResult, 0, this.barcodeScanResult.length);
        return scanResult;
    }

    public synchronized void setBarcodeScanResult(Object[] barcodeScanResult) {
        Object[] scanResult = new Object[barcodeScanResult.length];
        System.arraycopy(barcodeScanResult, 0, scanResult, 0, barcodeScanResult.length);
        this.barcodeScanResult = scanResult;
    }

    public void setBitmapWidth(int bitmapWidth) {
        this.bitmapWidth = bitmapWidth;
    }

    public void setBitmapHeight(int bitmapHeight) {
        this.bitmapHeight = bitmapHeight;
    }
}
