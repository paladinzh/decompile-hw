package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.System;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HwCustTelecInfoImpl extends HwCustTelecInfo {
    public File[] filterAuthenticatePngs(File[] files, Context context) {
        String allPngInfo = System.getString(context.getContentResolver(), "authenticate_by_product");
        if (allPngInfo == null) {
            return files;
        }
        String authenticatePngName = null;
        for (String pngInfo : allPngInfo.split(";")) {
            String[] productAndPngName = pngInfo.split(":");
            if (productAndPngName.length == 2 && Build.PRODUCT.equals(productAndPngName[0].trim())) {
                authenticatePngName = productAndPngName[1].trim();
                break;
            }
        }
        if (authenticatePngName == null) {
            return files;
        }
        List<File> noNeedFilterFiles = new ArrayList();
        for (File file : files) {
            String fileName = file.getName();
            for (String pngName : authenticatePngName.split(",")) {
                if (fileName.equals(pngName)) {
                    noNeedFilterFiles.add(file);
                }
            }
            if (fileName.startsWith("unrelate_product")) {
                noNeedFilterFiles.add(file);
            }
        }
        return (File[]) noNeedFilterFiles.toArray(new File[noNeedFilterFiles.size()]);
    }

    public boolean isHideCustomizedAuthen(Context context) {
        if (context == null) {
            return false;
        }
        boolean isHideCustAuthen = false;
        String allPngInfo = System.getString(context.getContentResolver(), "authenticate_by_product");
        if (allPngInfo != null) {
            isHideCustAuthen = allPngInfo.equals("Hide_all_cust_Authen");
        }
        return isHideCustAuthen;
    }
}
