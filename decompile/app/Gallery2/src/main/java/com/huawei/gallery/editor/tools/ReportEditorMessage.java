package com.huawei.gallery.editor.tools;

import com.huawei.gallery.editor.filters.FilterBasicRepresentation;
import com.huawei.gallery.editor.filters.FilterCropRepresentation;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation;
import com.huawei.gallery.editor.filters.FilterStraightenRepresentation;
import com.huawei.gallery.editor.filters.FilterWaterMarkRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterFeminineFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterGoogleFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterHuaweiCommonFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterHuaweiMistFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterMorphoFxRepresentation;
import java.util.HashMap;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportEditorMessage {
    private static void putValue(JSONObject jsonObject, String key, JSONArray valueArray) throws JSONException {
        if (jsonObject != null && valueArray != null && valueArray.length() != 0) {
            if (valueArray.length() == 1) {
                jsonObject.put(key, valueArray.get(0));
            } else {
                jsonObject.put(key, valueArray);
            }
        }
    }

    private static void putArrayValue(JSONArray jsonArray, JSONArray valueArray) throws JSONException {
        if (jsonArray != null && valueArray != null) {
            for (int i = 0; i < valueArray.length(); i++) {
                jsonArray.put(valueArray.get(i));
            }
        }
    }

    public static String getReportMsg(Vector<FilterRepresentation> filters) throws JSONException {
        if (filters == null || filters.size() == 0) {
            return "";
        }
        HashMap<String, JSONArray> hashMap = new HashMap();
        hashMap.put(FilterRotateRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterStraightenRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterMirrorRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterGoogleFxRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterHuaweiCommonFxRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterHuaweiMistFxRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterFeminineFxRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterMorphoFxRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterBasicRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterCropRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterFaceRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterWaterMarkRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterLabelRepresentation.class.getName(), new JSONArray());
        hashMap.put(FilterIllusionRepresentation.class.getName(), new JSONArray());
        for (int i = 0; i < filters.size(); i++) {
            FilterRepresentation rep = (FilterRepresentation) filters.get(i);
            String clsName = rep.getClass().getName();
            String reportMsg = rep.getReportMsg();
            if (hashMap.containsKey(clsName) && reportMsg != null) {
                ((JSONArray) hashMap.get(clsName)).put(reportMsg);
            }
        }
        hashMap.put(FilterMosaicRepresentation.class.getName(), new JSONArray());
        getDrawReportMsg(filters, (JSONArray) hashMap.get(FilterMosaicRepresentation.class.getName()));
        JSONObject jsonObject = new JSONObject();
        JSONArray rotateArray = new JSONArray();
        putArrayValue(rotateArray, (JSONArray) hashMap.get(FilterRotateRepresentation.class.getName()));
        putArrayValue(rotateArray, (JSONArray) hashMap.get(FilterMirrorRepresentation.class.getName()));
        putArrayValue(rotateArray, (JSONArray) hashMap.get(FilterStraightenRepresentation.class.getName()));
        putValue(jsonObject, "Rotate", rotateArray);
        JSONArray filterArray = new JSONArray();
        putArrayValue(filterArray, (JSONArray) hashMap.get(FilterGoogleFxRepresentation.class.getName()));
        putArrayValue(filterArray, (JSONArray) hashMap.get(FilterHuaweiCommonFxRepresentation.class.getName()));
        putArrayValue(filterArray, (JSONArray) hashMap.get(FilterHuaweiMistFxRepresentation.class.getName()));
        putArrayValue(filterArray, (JSONArray) hashMap.get(FilterFeminineFxRepresentation.class.getName()));
        putArrayValue(filterArray, (JSONArray) hashMap.get(FilterMorphoFxRepresentation.class.getName()));
        putValue(jsonObject, "Filter", filterArray);
        putValue(jsonObject, "Adjust", (JSONArray) hashMap.get(FilterBasicRepresentation.class.getName()));
        putValue(jsonObject, "Crop", (JSONArray) hashMap.get(FilterCropRepresentation.class.getName()));
        putValue(jsonObject, "Beauty", (JSONArray) hashMap.get(FilterFaceRepresentation.class.getName()));
        putValue(jsonObject, "WaterMark", (JSONArray) hashMap.get(FilterWaterMarkRepresentation.class.getName()));
        putValue(jsonObject, "Mosaic", (JSONArray) hashMap.get(FilterMosaicRepresentation.class.getName()));
        putValue(jsonObject, "Label", (JSONArray) hashMap.get(FilterLabelRepresentation.class.getName()));
        putValue(jsonObject, "Illusion", (JSONArray) hashMap.get(FilterIllusionRepresentation.class.getName()));
        return jsonObject.toString();
    }

    private static void getDrawReportMsg(Vector<FilterRepresentation> filters, JSONArray jsonArray) {
        boolean hasGraffiti = false;
        boolean hasMosaic = false;
        boolean hasErase = false;
        for (int i = 0; i < filters.size(); i++) {
            FilterRepresentation rep = (FilterRepresentation) filters.get(i);
            if (rep instanceof FilterMosaicRepresentation) {
                boolean[] type = ((FilterMosaicRepresentation) rep).getReportStrokeType();
                if (type.length > 2) {
                    if (!hasGraffiti) {
                        hasGraffiti = type[0];
                    }
                    if (!hasMosaic) {
                        hasMosaic = type[1];
                    }
                    if (!hasErase) {
                        hasErase = type[2];
                    }
                }
                if (hasGraffiti && r2 && r0) {
                    break;
                }
            }
        }
        if (hasGraffiti) {
            jsonArray.put("DRAW_GRAFFITI");
        }
        if (hasMosaic) {
            jsonArray.put("DRAW_MOSAIC");
        }
        if (hasErase) {
            jsonArray.put("DRAW_ERASE");
        }
    }
}
