package com.android.contacts.model.dataitem;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.AccountType.StringInflater;
import com.google.common.collect.Iterators;
import java.text.SimpleDateFormat;
import java.util.List;

public final class DataKind {
    public StringInflater actionAltHeader;
    public StringInflater actionBody;
    public StringInflater actionHeader;
    public SimpleDateFormat dateFormatWithYear;
    public SimpleDateFormat dateFormatWithoutYear;
    public ContentValues defaultValues;
    public boolean editable;
    public List<EditField> fieldList;
    public int iconAltDescriptionRes;
    public int iconAltRes;
    private Bitmap mBitmapFromDetailCard;
    public int maxLinesForDisplay;
    public String mimeType;
    public String resourcePackageName;
    public boolean shouldShowDatePicker;
    public int titleRes;
    public String typeColumn;
    public List<EditType> typeList;
    public int typeOverallMax;
    public int typeToSelect;
    public int weight;

    public Bitmap getmBitmapFromDetailCard() {
        return this.mBitmapFromDetailCard;
    }

    public void setmBitmapFromDetailCard(Bitmap mBitmapFromDetailCard) {
        this.mBitmapFromDetailCard = mBitmapFromDetailCard;
    }

    public DataKind() {
        this.typeToSelect = -1;
        this.shouldShowDatePicker = false;
        this.maxLinesForDisplay = 1;
    }

    public DataKind(String mimeType, int titleRes, int weight, boolean editable) {
        this.typeToSelect = -1;
        this.shouldShowDatePicker = false;
        this.mimeType = mimeType;
        this.titleRes = titleRes;
        this.weight = weight;
        this.editable = editable;
        this.typeOverallMax = -1;
        if ("vnd.android.cursor.item/note".equals(mimeType)) {
            this.maxLinesForDisplay = 100;
        } else {
            this.maxLinesForDisplay = 1;
        }
    }

    public String getKindString(Context context) {
        return (this.titleRes == -1 || this.titleRes == 0) ? "" : context.getString(this.titleRes);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataKind:");
        sb.append(" resPackageName=").append(this.resourcePackageName);
        sb.append(" mimeType=").append(this.mimeType);
        sb.append(" titleRes=").append(this.titleRes);
        sb.append(" iconAltRes=").append(this.iconAltRes);
        sb.append(" iconAltDescriptionRes=").append(this.iconAltDescriptionRes);
        sb.append(" weight=").append(this.weight);
        sb.append(" editable=").append(this.editable);
        sb.append(" actionHeader=").append(this.actionHeader);
        sb.append(" actionAltHeader=").append(this.actionAltHeader);
        sb.append(" actionBody=").append(this.actionBody);
        sb.append(" typeColumn=").append(this.typeColumn);
        sb.append(" typeOverallMax=").append(this.typeOverallMax);
        sb.append(" typeList=").append(toString(this.typeList));
        sb.append(" fieldList=").append(toString(this.fieldList));
        sb.append(" defaultValues=").append(this.defaultValues);
        sb.append(" dateFormatWithoutYear=").append(toString(this.dateFormatWithoutYear));
        sb.append(" dateFormatWithYear=").append(toString(this.dateFormatWithYear));
        return sb.toString();
    }

    public static String toString(SimpleDateFormat format) {
        return format == null ? "(null)" : format.toPattern();
    }

    public static String toString(Iterable<?> list) {
        if (list == null) {
            return "(null)";
        }
        return Iterators.toString(list.iterator());
    }
}
