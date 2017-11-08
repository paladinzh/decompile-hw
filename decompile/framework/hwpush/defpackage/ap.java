package defpackage;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.bd.Reporter;
import java.io.File;
import org.json.JSONObject;

/* renamed from: ap */
public class ap {
    public static synchronized void i(Context context, String str) {
        synchronized (ap.class) {
            try {
                aw.d("PushLog2841", "enter ModifyConfigs modify jsonStr is : " + str);
                ar j = ap.j(str);
                if (j != null) {
                    if (!TextUtils.isEmpty(j.bx)) {
                        switch (j.by) {
                            case Reporter.ACTIVITY_CREATE /*1*/:
                                if (!TextUtils.isEmpty(j.mName)) {
                                    if (!new bt(context, j.bx).c(j.mName, j.bz)) {
                                        aw.e("PushLog2841", "enter ModifyConfigs saveString failed!");
                                        break;
                                    } else {
                                        aw.d("PushLog2841", "enter ModifyConfigs saveString sucessfully! filename is " + j.bx + ",itemName is " + j.mName + ",itemValue is " + j.bz);
                                        break;
                                    }
                                }
                                aw.e("PushLog2841", "enter ModifyConfigs saveString failed! mName or mVal is null");
                                break;
                            case Reporter.ACTIVITY_RESUME /*2*/:
                                if (!TextUtils.isEmpty(j.mName)) {
                                    File file = new File("/data/misc/hwpush" + File.separator + j.bx + ".xml");
                                    if (file.isFile() && file.exists()) {
                                        if (!new bt(context, j.bx).z(j.mName)) {
                                            aw.e("PushLog2841", "enter ModifyConfigs removeKey failed, maybe the key is not exist!");
                                            break;
                                        } else {
                                            aw.d("PushLog2841", "enter ModifyConfigs removeKey sucessfully! the fileName is " + j.bx + ",the key is " + j.mName);
                                            break;
                                        }
                                    }
                                    aw.e("PushLog2841", "the file is not exist! file path is" + file);
                                    break;
                                }
                                aw.e("PushLog2841", "enter ModifyConfigs removeKey failed! mName is null");
                                break;
                                break;
                            case Reporter.ACTIVITY_PAUSE /*3*/:
                                String str2 = "/data/misc/hwpush" + File.separator + j.bx + ".xml";
                                File file2 = new File(str2);
                                if (file2.isFile() && file2.exists()) {
                                    if (!file2.delete()) {
                                        aw.e("PushLog2841", "delete failed! file path is " + str2);
                                        break;
                                    } else {
                                        aw.d("PushLog2841", "delete success! file path is " + str2);
                                        break;
                                    }
                                }
                                aw.e("PushLog2841", "the file is not exist! file path is" + str2);
                                break;
                                break;
                            default:
                                aw.e("PushLog2841", "the modifyType:" + j.by + " is not supported! ");
                                break;
                        }
                    }
                    aw.e("PushLog2841", "enter ModifyConfigs struct failed to create sharepreference file!");
                } else {
                    aw.e("PushLog2841", "enter ModifyConfigs struct is null !");
                }
            } catch (Throwable e) {
                aw.d("PushLog2841", e.toString(), e);
            }
        }
    }

    private static ar j(String str) {
        ar arVar = new ar();
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("file")) {
                arVar.bx = jSONObject.getString("file");
                aw.d("PushLog2841", "ModifyStruct mFileName is " + arVar.bx);
            }
            if (jSONObject.has("type")) {
                arVar.by = jSONObject.getInt("type");
                aw.d("PushLog2841", "ModifyStruct mModifyType is " + arVar.by);
            }
            if (jSONObject.has("name")) {
                arVar.mName = jSONObject.getString("name");
                aw.d("PushLog2841", "ModifyStruct mName is " + arVar.mName);
            }
            if (!jSONObject.has("val")) {
                return arVar;
            }
            arVar.bz = jSONObject.get("val");
            aw.d("PushLog2841", "ModifyStruct mVal is " + arVar.bz);
            return arVar;
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
            return null;
        }
    }
}
