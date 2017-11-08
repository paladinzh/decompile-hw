package com.fyusion.sdk.viewer.internal;

import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.viewer.internal.f.b;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class a {
    public static i a(i iVar, p pVar, JSONObject jSONObject) throws JSONException {
        float f = 1.0f;
        boolean z = true;
        iVar.setId(jSONObject.optString("a"));
        if (jSONObject.has("z")) {
            iVar.setName(jSONObject.getJSONObject("z").optString("f"));
            iVar.setUserName(jSONObject.getJSONObject("z").optString("e"));
        }
        iVar.setTimeStamp((long) jSONObject.optInt("i"));
        iVar.setUrl(jSONObject.optString("c", null));
        JSONObject optJSONObject = jSONObject.optJSONObject("fy");
        if (optJSONObject != null) {
            int i;
            pVar.setDirectionX((float) optJSONObject.optDouble("dx"));
            pVar.setDirectionY((float) optJSONObject.optDouble("dy"));
            pVar.setNumProcessedFrames(optJSONObject.optInt("f"));
            pVar.setCurvature((float) optJSONObject.optDouble("cv"));
            pVar.setThumbnailIndex(optJSONObject.optInt("t"));
            pVar.setFrontCamera(optJSONObject.optInt("fr"));
            if (optJSONObject.has("o") && optJSONObject.optInt("o") > 0) {
                pVar.setAndroid(true);
            }
            if (optJSONObject.has("p")) {
                pVar.setFlags(optJSONObject.optInt("p"));
            } else {
                pVar.setFlags(0);
            }
            JSONArray optJSONArray = optJSONObject.optJSONArray("l");
            if (optJSONArray != null) {
                int[] iArr = new int[optJSONArray.length()];
                for (i = 0; i < optJSONArray.length(); i++) {
                    iArr[i] = optJSONArray.optInt(i);
                }
                pVar.setBounds(iArr);
            }
            optJSONArray = optJSONObject.optJSONArray("s");
            if (optJSONArray != null) {
                pVar.setSlicesLength(optJSONArray.length());
                for (i = 0; i < optJSONArray.length(); i++) {
                    JSONArray optJSONArray2 = optJSONArray.optJSONArray(i);
                    pVar.addSlice(optJSONArray2.optInt(0), optJSONArray2.optInt(1), optJSONArray2.optInt(2));
                }
            }
            if (optJSONObject.has("so")) {
                pVar.setStabilizationDataFrameOffset(optJSONObject.optInt("so"));
            }
            pVar.setHoriz(optJSONObject.optInt("m") > 0);
            pVar.setRotation_mode(optJSONObject.optInt("r"));
            if (pVar.getHoriz()) {
                pVar.setWidth(optJSONObject.optInt("w"));
                pVar.setHeight(optJSONObject.optInt("h"));
            } else {
                pVar.setWidth(optJSONObject.optInt("h"));
                pVar.setHeight(optJSONObject.optInt("w"));
            }
            if (optJSONObject.has("cw") && optJSONObject.has("ch")) {
                pVar.setCameraWidth(optJSONObject.optInt("cw"));
                pVar.setCameraHeight(optJSONObject.optInt("ch"));
            }
            boolean has = optJSONObject.has("a");
            boolean has2 = optJSONObject.has("pw");
            boolean has3 = optJSONObject.has("ph");
            if (has) {
                pVar.setThumbSlice(optJSONObject.optInt("a"));
                if (has2) {
                    iVar.setPreviewWidth(optJSONObject.optInt("pw"));
                }
                if (has3) {
                    iVar.setPreviewHeight(optJSONObject.optInt("ph"));
                }
            }
            if (has && has2) {
                if (!has3) {
                }
                iVar.setHasLowResolutionSlice(z);
                iVar.setMagic(pVar);
                if (pVar.getRotationMode() == 2) {
                    pVar.setGravityY(0.0f);
                    if (pVar.isFromFrontCamera()) {
                        if (pVar.getRotationMode() == 0) {
                            f = -1.0f;
                        }
                        pVar.setGravityX(f);
                    } else {
                        pVar.setGravityX(pVar.getRotationMode() == 0 ? -1.0f : 1.0f);
                    }
                } else {
                    pVar.setGravityX(0.0f);
                    if (pVar.getHoriz()) {
                        pVar.setGravityY(1.0f);
                    } else {
                        pVar.setGravityY(-1.0f);
                    }
                }
            }
            z = false;
            iVar.setHasLowResolutionSlice(z);
            iVar.setMagic(pVar);
            if (pVar.getRotationMode() == 2) {
                pVar.setGravityX(0.0f);
                if (pVar.getHoriz()) {
                    pVar.setGravityY(1.0f);
                } else {
                    pVar.setGravityY(-1.0f);
                }
            } else {
                pVar.setGravityY(0.0f);
                if (pVar.isFromFrontCamera()) {
                    if (pVar.getRotationMode() == 0) {
                    }
                    pVar.setGravityX(pVar.getRotationMode() == 0 ? -1.0f : 1.0f);
                } else {
                    if (pVar.getRotationMode() == 0) {
                        f = -1.0f;
                    }
                    pVar.setGravityX(f);
                }
            }
        }
        if (jSONObject.has("[")) {
            iVar.setBlurImage(b.a(jSONObject.optString("[")));
        }
        return iVar;
    }

    public static i a(JSONObject jSONObject) throws JSONException {
        return a(new i(), new p(), jSONObject);
    }
}
