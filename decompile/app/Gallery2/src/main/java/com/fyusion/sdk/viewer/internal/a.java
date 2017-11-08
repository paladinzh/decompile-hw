package com.fyusion.sdk.viewer.internal;

import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.n;
import com.fyusion.sdk.viewer.internal.f.b;
import com.huawei.watermark.manager.parse.WMElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class a {
    public static h a(h hVar, n nVar, JSONObject jSONObject) throws JSONException {
        float f = WMElement.CAMERASIZEVALUE1B1;
        boolean z = true;
        hVar.setId(jSONObject.optString("a"));
        if (jSONObject.has("z")) {
            hVar.setName(jSONObject.getJSONObject("z").optString("f"));
            hVar.setUserName(jSONObject.getJSONObject("z").optString("e"));
        }
        hVar.setTimeStamp((long) jSONObject.optInt("i"));
        hVar.setUrl(jSONObject.optString("c", null));
        JSONObject optJSONObject = jSONObject.optJSONObject("fy");
        if (optJSONObject != null) {
            int i;
            nVar.setDirectionX((float) optJSONObject.optDouble("dx"));
            nVar.setDirectionY((float) optJSONObject.optDouble("dy"));
            nVar.setNumProcessedFrames(optJSONObject.optInt("f"));
            nVar.setCurvature((float) optJSONObject.optDouble("cv"));
            nVar.setThumbnailIndex(optJSONObject.optInt("t"));
            nVar.setFrontCamera(optJSONObject.optInt("fr"));
            if (optJSONObject.has("o") && optJSONObject.optInt("o") > 0) {
                nVar.setAndroid(true);
            }
            if (optJSONObject.has("p")) {
                nVar.setFlags(optJSONObject.optInt("p"));
            } else {
                nVar.setFlags(0);
            }
            JSONArray optJSONArray = optJSONObject.optJSONArray("l");
            if (optJSONArray != null) {
                int[] iArr = new int[optJSONArray.length()];
                for (i = 0; i < optJSONArray.length(); i++) {
                    iArr[i] = optJSONArray.optInt(i);
                }
                nVar.setBounds(iArr);
            }
            optJSONArray = optJSONObject.optJSONArray("s");
            if (optJSONArray != null) {
                nVar.setSlicesLength(optJSONArray.length());
                for (i = 0; i < optJSONArray.length(); i++) {
                    JSONArray optJSONArray2 = optJSONArray.optJSONArray(i);
                    nVar.addSlice(optJSONArray2.optInt(0), optJSONArray2.optInt(1), optJSONArray2.optInt(2));
                }
            }
            if (optJSONObject.has("so")) {
                nVar.setStabilizationDataFrameOffset(optJSONObject.optInt("so"));
            }
            nVar.setHoriz(optJSONObject.optInt("m") > 0);
            nVar.setRotation_mode(optJSONObject.optInt("r"));
            if (nVar.getHoriz()) {
                nVar.setWidth(optJSONObject.optInt("w"));
                nVar.setHeight(optJSONObject.optInt("h"));
            } else {
                nVar.setWidth(optJSONObject.optInt("h"));
                nVar.setHeight(optJSONObject.optInt("w"));
            }
            if (optJSONObject.has("cw") && optJSONObject.has("ch")) {
                nVar.setCameraWidth(optJSONObject.optInt("cw"));
                nVar.setCameraHeight(optJSONObject.optInt("ch"));
            }
            boolean has = optJSONObject.has("a");
            boolean has2 = optJSONObject.has("pw");
            boolean has3 = optJSONObject.has("ph");
            if (has) {
                nVar.setThumbSlice(optJSONObject.optInt("a"));
                if (has2) {
                    hVar.setPreviewWidth(optJSONObject.optInt("pw"));
                }
                if (has3) {
                    hVar.setPreviewHeight(optJSONObject.optInt("ph"));
                }
            }
            if (has && has2) {
                if (!has3) {
                }
                hVar.setHasLowResolutionSlice(z);
                hVar.setMagic(nVar);
                if (nVar.getRotationMode() == 2) {
                    nVar.setGravityY(0.0f);
                    if (nVar.isFromFrontCamera()) {
                        if (nVar.getRotationMode() == 0) {
                            f = GroundOverlayOptions.NO_DIMENSION;
                        }
                        nVar.setGravityX(f);
                    } else {
                        nVar.setGravityX(nVar.getRotationMode() == 0 ? GroundOverlayOptions.NO_DIMENSION : WMElement.CAMERASIZEVALUE1B1);
                    }
                } else {
                    nVar.setGravityX(0.0f);
                    if (nVar.getHoriz()) {
                        nVar.setGravityY(WMElement.CAMERASIZEVALUE1B1);
                    } else {
                        nVar.setGravityY(GroundOverlayOptions.NO_DIMENSION);
                    }
                }
            }
            z = false;
            hVar.setHasLowResolutionSlice(z);
            hVar.setMagic(nVar);
            if (nVar.getRotationMode() == 2) {
                nVar.setGravityX(0.0f);
                if (nVar.getHoriz()) {
                    nVar.setGravityY(WMElement.CAMERASIZEVALUE1B1);
                } else {
                    nVar.setGravityY(GroundOverlayOptions.NO_DIMENSION);
                }
            } else {
                nVar.setGravityY(0.0f);
                if (nVar.isFromFrontCamera()) {
                    if (nVar.getRotationMode() == 0) {
                    }
                    nVar.setGravityX(nVar.getRotationMode() == 0 ? GroundOverlayOptions.NO_DIMENSION : WMElement.CAMERASIZEVALUE1B1);
                } else {
                    if (nVar.getRotationMode() == 0) {
                        f = GroundOverlayOptions.NO_DIMENSION;
                    }
                    nVar.setGravityX(f);
                }
            }
        }
        if (jSONObject.has("[")) {
            hVar.setBlurImage(b.a(jSONObject.optString("[")));
        }
        return hVar;
    }

    public static h a(JSONObject jSONObject) throws JSONException {
        return a(new h(), new n(), jSONObject);
    }
}
