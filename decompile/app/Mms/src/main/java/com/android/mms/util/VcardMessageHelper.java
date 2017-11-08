package com.android.mms.util;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import com.android.mms.TempFileProvider;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.model.VcardModel.VcardAdapter;
import com.android.vcard.VCardUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.StatisticalHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VcardMessageHelper {
    private Bitmap mBitmap;
    private Context mContext;
    private byte[] mData;
    private List<VCardDetailNode> mVcardDetailList;

    public VcardMessageHelper(Context context) {
        this.mContext = context;
    }

    public VcardMessageHelper(Context context, byte[] data, List<VCardDetailNode> vcardDetailList, Bitmap bitmap) {
        byte[] bArr = null;
        this.mContext = context;
        if (data != null) {
            bArr = Arrays.copyOf(data, data.length);
        }
        this.mData = bArr;
        this.mVcardDetailList = vcardDetailList;
        if (bitmap == null) {
            bitmap = stripDefaultVcardPhoto();
        }
        this.mBitmap = bitmap;
    }

    public VcardMessageHelper(Context context, byte[] data, List<VCardDetailNode> vcardDetailList) {
        this(context, data, vcardDetailList, null);
    }

    public void viewVcardDetail() {
        Builder vCardDialogBuilder = new Builder(this.mContext).setTitle(R.string.view_vcard).setIcon(new BitmapDrawable(this.mContext.getResources(), this.mBitmap));
        vCardDialogBuilder.setAdapter(new VcardAdapter(this.mContext, this.mVcardDetailList, false), null);
        vCardDialogBuilder.show();
        StatisticalHelper.incrementReportCount(this.mContext, 2232);
    }

    public String[] getVcardDetail() {
        String[] vcardDetail = new String[this.mVcardDetailList.size()];
        int index = 0;
        for (VCardDetailNode node : this.mVcardDetailList) {
            vcardDetail[index] = node.getValue();
            index++;
        }
        return vcardDetail;
    }

    private boolean isMultiContactinVcard() {
        if (this.mVcardDetailList.size() > 0) {
            VCardDetailNode node = (VCardDetailNode) this.mVcardDetailList.get(0);
            if (node != null && node.getValue() == null) {
                return true;
            }
        }
        return false;
    }

    public Bitmap stripDefaultVcardPhoto() {
        if (isMultiContactinVcard()) {
            return BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.cs_textfield_default_emui);
        }
        return BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.cs_spinner);
    }

    public void saveVcard() {
        Builder vCardDialogBuilder = new Builder(this.mContext).setTitle(R.string.save_to_contacts).setIcon(new BitmapDrawable(this.mContext.getResources(), this.mBitmap));
        vCardDialogBuilder.setPositiveButton(R.string.save, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                VcardMessageHelper.this.updateVcardData();
                VcardMessageHelper.this.createTmpVcardFile();
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setPackage("com.android.contacts");
                intent.addFlags(1);
                intent.setDataAndType(TempFileProvider.SCRAP_VCARD_IMP_URI, "text/x-vCard".toLowerCase());
                VcardMessageHelper.this.mContext.startActivity(intent);
            }
        }).setNegativeButton(R.string.no, null);
        VcardAdapter vCardAdapter = new VcardAdapter(this.mContext, this.mVcardDetailList, true);
        vCardDialogBuilder.setAdapter(vCardAdapter, null);
        vCardAdapter.setButton(vCardDialogBuilder.show().getButton(-1));
    }

    public void updateVcardModel(Context context, VcardModel vcardModel) {
        Exception e;
        try {
            VcardMessageHelper vCardMessageHelper = new VcardMessageHelper(context, vcardModel.getData(), vcardModel.getVcardDetailList());
            try {
                vCardMessageHelper.updateVcardData();
                vCardMessageHelper.createTmpVcardFile();
                vcardModel.setData(vCardMessageHelper.getData());
                VcardMessageHelper vcardMessageHelper = vCardMessageHelper;
            } catch (Exception e2) {
                e = e2;
                MLog.e("Mms/VcardUtils", "updateVcardModel exception" + e);
            }
        } catch (Exception e3) {
            e = e3;
            MLog.e("Mms/VcardUtils", "updateVcardModel exception" + e);
        }
    }

    public void editVcardDetailEx(VcardModel vCardModel) {
        if (vCardModel != null) {
            vCardModel.cacheSelectState();
            updateVcardModel(this.mContext, vCardModel);
        }
    }

    public void updateVcardData() {
        if (isMultiContactinVcard()) {
            updateMultiVcard();
        } else {
            updateOneVcard();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateMultiVcard() {
        List<String> unSelectList = new ArrayList();
        for (VCardDetailNode vCardDetailNode : this.mVcardDetailList) {
            if (!vCardDetailNode.isSelect()) {
                unSelectList.add(vCardDetailNode.getPropName());
            }
        }
        if (!unSelectList.isEmpty()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.mData), Charset.defaultCharset()));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, Charset.defaultCharset()));
            int vCardNo = -1;
            while (true) {
                try {
                    String line = br.readLine();
                    if (line != null) {
                        if (line.trim().equals("BEGIN:VCARD")) {
                            vCardNo++;
                            if (unSelectList.contains(String.valueOf(vCardNo))) {
                                while (!"END:VCARD".equals(line)) {
                                    line = br.readLine();
                                    if (line != null) {
                                        line = line.trim();
                                    }
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(line)) {
                            bw.write(line);
                        }
                        bw.newLine();
                        bw.flush();
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    try {
                        br.close();
                        bw.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            }
            br.close();
            bw.close();
            setData(bos.toByteArray());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateOneVcard() {
        List<VCardDetailNode> unSelectList = new ArrayList();
        for (VCardDetailNode vCardDetailNode : this.mVcardDetailList) {
            if (!vCardDetailNode.isSelect()) {
                unSelectList.add(vCardDetailNode);
            }
        }
        if (!unSelectList.isEmpty()) {
            String[] dataArray = new String(this.mData, Charset.defaultCharset()).replace("\r\n", "\n").replace("\r", "\n").split("\n");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, Charset.defaultCharset()));
            int type = -1;
            boolean isLastItemSelected = false;
            int i = 0;
            while (i < dataArray.length) {
                try {
                    String line = dataArray[i];
                    if (!TextUtils.isEmpty(line)) {
                        if (useNumTypeForData(line)) {
                            type++;
                        }
                        if (line.contains(":")) {
                            if (isUnSelect(unSelectList, line, type, dataArray, i)) {
                                isLastItemSelected = false;
                            } else {
                                wirteLine(bw, line);
                                isLastItemSelected = true;
                            }
                        } else if (isLastItemSelected) {
                            wirteLine(bw, line);
                        }
                    }
                    i++;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    try {
                        bw.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            try {
                bw.close();
            } catch (IOException e22) {
                e22.printStackTrace();
            }
            setData(bos.toByteArray());
        }
    }

    private void wirteLine(BufferedWriter bw, String line) throws IOException {
        bw.write(line);
        bw.newLine();
        bw.flush();
    }

    private boolean useNumTypeForData(String line) {
        boolean z = true;
        String upperLine = line.toUpperCase();
        if (upperLine.startsWith("URL") || upperLine.startsWith("ORG") || upperLine.startsWith("TITLE")) {
            return true;
        }
        String subLine = null;
        if (upperLine.startsWith("TEL") || upperLine.startsWith("ADR")) {
            subLine = line.substring(3);
        } else if (upperLine.startsWith("EMAIL")) {
            subLine = line.substring(5);
        }
        if (subLine == null) {
            return false;
        }
        if (subLine.startsWith(";WORK") || subLine.startsWith(";HOME") || subLine.startsWith(";CELL")) {
            z = false;
        }
        return z;
    }

    private boolean useNumTypeForNode(VCardDetailNode vCardDetailNode) {
        String propName = vCardDetailNode.getPropName().toUpperCase();
        if (propName.startsWith("URL") || propName.startsWith("ORG") || propName.startsWith("TITLE")) {
            return true;
        }
        if (propName.equals("TEL") || propName.equals("ADR") || propName.equals("EMAIL")) {
            try {
                Integer.parseInt(vCardDetailNode.getPropType());
                return true;
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    private boolean isUnSelect(List<VCardDetailNode> unSelectList, String line, int type, String[] array, int offset) {
        if (line.startsWith(" ") || TextUtils.isEmpty(line)) {
            return false;
        }
        String qpconvertvalue = null;
        int index = 0;
        while (index < unSelectList.size()) {
            VCardDetailNode vCardDetailNode = (VCardDetailNode) unSelectList.get(index);
            if (line.toUpperCase().startsWith("N") && "FN".equals(vCardDetailNode.getPropName())) {
                return true;
            }
            boolean isQPConvertProp;
            if ("ADR".equals(vCardDetailNode.getPropName()) || "ORG".equals(vCardDetailNode.getPropName()) || "N".equals(vCardDetailNode.getPropName())) {
                isQPConvertProp = true;
            } else {
                isQPConvertProp = false;
            }
            boolean result = line.toUpperCase().startsWith(vCardDetailNode.getPropName());
            if (vCardDetailNode.getPropType() != null) {
                if (useNumTypeForNode(vCardDetailNode)) {
                    result &= vCardDetailNode.getPropType().equals(String.valueOf(type));
                    index = removeMatchedUnselectedListItem(unSelectList, result, index);
                } else {
                    int propNameLength = vCardDetailNode.getPropName().length();
                    if (line.length() > propNameLength) {
                        String subLine = line.substring(propNameLength);
                        if (subLine.contains(";")) {
                            String[] e = subLine.split(";");
                            if (e.length > 2) {
                                result &= !e[1].startsWith(vCardDetailNode.getPropType()) ? formatMultiPropType(subLine).equals(vCardDetailNode.getPropType()) : 1;
                            } else {
                                result &= subLine.startsWith(";" + vCardDetailNode.getPropType());
                            }
                        } else {
                            result &= subLine.startsWith(";" + vCardDetailNode.getPropType());
                        }
                        if (vCardDetailNode.getValue() != null) {
                            int propTypeLength = vCardDetailNode.getPropType().length();
                            if (line.length() > ((propNameLength + 1) + propTypeLength) + 1) {
                                String valueLine = line.substring(((propNameLength + 1) + propTypeLength) + 1).replace(';', ' ').trim();
                                int pos = -1;
                                boolean hasQP = false;
                                if (valueLine.contains("QUOTED-PRINTABLE")) {
                                    pos = valueLine.lastIndexOf("QUOTED-PRINTABLE");
                                    hasQP = true;
                                }
                                String charsetvalue = null;
                                if (valueLine.contains("CHARSET")) {
                                    charsetvalue = valueLine.substring(valueLine.lastIndexOf("CHARSET")).split("\\s")[0].substring("CHARSET".length() + 1);
                                }
                                if (hasQP && isQPConvertProp) {
                                    if (qpconvertvalue == null) {
                                        pos = ("QUOTED-PRINTABLE".length() + pos) + 1;
                                        if (pos < line.length()) {
                                            String subvalue = valueLine.substring(pos).trim();
                                            if (subvalue.trim().endsWith("=")) {
                                                subvalue = getCompleteValue(subvalue, array, offset + 1);
                                            }
                                            if (charsetvalue == null) {
                                                try {
                                                    charsetvalue = "UTF-8";
                                                } catch (Exception e2) {
                                                    MLog.e("Mms/VcardUtils", MLog.getStackTraceString(e2));
                                                }
                                            }
                                            qpconvertvalue = VCardUtils.parseQuotedPrintable(subvalue, false, "ISO-8859-1", charsetvalue);
                                        }
                                    }
                                    if (qpconvertvalue != null) {
                                        result &= qpconvertvalue.startsWith(vCardDetailNode.getValue());
                                    }
                                } else {
                                    result &= valueLine.contains(vCardDetailNode.getValue());
                                }
                            }
                        }
                    }
                }
            }
            MLog.v("Mms/VcardUtils", "isUnSelect:: result is: " + result + "and propName is: " + vCardDetailNode.getPropName());
            if (result) {
                return result;
            }
            index++;
        }
        return false;
    }

    private String getCompleteValue(String subvalue, String[] array, int offset) {
        int subpos = subvalue.length() - 1;
        do {
        } while (subvalue.charAt(subpos) != '=');
        StringBuilder builder = new StringBuilder();
        builder.append(subvalue.substring(0, subpos));
        int i = offset;
        while (i < array.length) {
            String nextline = array[i];
            if (nextline == null) {
                MLog.e("Mms/VcardUtils", "newnextline = null");
                break;
            }
            try {
                String newlinesub = nextline.replace(';', ' ').trim();
                if (!newlinesub.trim().endsWith("=")) {
                    builder.append(newlinesub);
                    break;
                }
                subpos = newlinesub.length() - 1;
                do {
                } while (newlinesub.charAt(subpos) != '=');
                builder.append(newlinesub.substring(0, subpos));
                i++;
            } catch (Exception e) {
                MLog.e("Mms/VcardUtils", MLog.getStackTraceString(e));
            }
        }
        return builder.toString().trim();
    }

    private synchronized void createTmpVcardFile() {
        this.mContext.deleteFile("vcard_temp.vcf");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mContext.openFileOutput("vcard_temp.vcf", 0);
            fileOutputStream.write(this.mData);
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e3) {
                }
            }
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e4) {
                }
            }
        }
        return;
    }

    public void setData(byte[] data) {
        byte[] bArr = null;
        if (data != null) {
            bArr = Arrays.copyOf(data, data.length);
        }
        this.mData = bArr;
    }

    public byte[] getData() {
        if (this.mData == null) {
            return null;
        }
        byte[] data = new byte[this.mData.length];
        System.arraycopy(this.mData, 0, data, 0, this.mData.length);
        return data;
    }

    public static boolean isVCardSmsSubject(String subject) {
        String vbg = "begin:vcard";
        if (TextUtils.isEmpty(subject) || subject.length() <= "begin:vcard".length()) {
            return false;
        }
        return "begin:vcard".equals(subject.substring(0, "begin:vcard".length()).toLowerCase());
    }

    public static String filterVcardNumbers(String vCardString) {
        if (TextUtils.isEmpty(vCardString)) {
            return "";
        }
        StringBuffer newVcardString = new StringBuffer();
        for (String line : vCardString.replace("\r\n", "\n").replace("\r", "\n").split("\n")) {
            if (!TextUtils.isEmpty(line) && line.startsWith("TEL") && line.contains(":")) {
                String[] vcardPair = line.split(":", 2);
                newVcardString.append(vcardPair[0]);
                newVcardString.append(":");
                String formatNumber = PhoneNumberFormatter.getFromatedNumber(vcardPair[1]);
                if (TextUtils.isEmpty(formatNumber)) {
                    newVcardString.append(vcardPair[1]);
                } else {
                    newVcardString.append(formatNumber);
                }
                newVcardString.append("\n");
            } else {
                newVcardString.append(line);
                newVcardString.append("\n");
            }
        }
        return newVcardString.toString();
    }

    private int removeMatchedUnselectedListItem(List<VCardDetailNode> unSelectList, boolean result, int index) {
        if (!result) {
            return index;
        }
        unSelectList.remove(index);
        return index - 1;
    }

    public String formatMultiPropType(String originalType) {
        StringBuilder formatedType = new StringBuilder();
        if (TextUtils.isEmpty(originalType)) {
            return "";
        }
        if (!originalType.contains(";") || originalType.split(";").length <= 2) {
            return originalType;
        }
        String[] types = originalType.split(":")[0].split(";");
        for (int i = types.length - 1; i >= 0; i--) {
            formatedType.append(types[i]);
        }
        return formatedType.toString();
    }
}
