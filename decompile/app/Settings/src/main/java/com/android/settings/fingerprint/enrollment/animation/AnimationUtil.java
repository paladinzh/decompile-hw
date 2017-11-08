package com.android.settings.fingerprint.enrollment.animation;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;

public class AnimationUtil {
    private static float mScalePathData;
    private static SparseArray<ArrayList<TouchShowPath>> mSectionData = new SparseArray();
    private static XmlResourceParser xmlParser;

    public static SparseArray<ArrayList<TouchShowPath>> getmSectionData() {
        return mSectionData;
    }

    public static float getmScalePathData() {
        return mScalePathData;
    }

    public static void createSectionData(ArrayList<TouchShowPath> datas) {
        Log.i("fingerprint.AnimationUtil", "createSectionData");
        if (datas != null && datas.size() != 0 && mSectionData != null) {
            mSectionData.clear();
            int stage = -1;
            int group = -1;
            ArrayList stagePaths = null;
            TouchShowPath beforePath = null;
            for (int i = 0; i < datas.size(); i++) {
                TouchShowPath currentPath = (TouchShowPath) datas.get(i);
                if (currentPath != null) {
                    currentPath.calculateSquare();
                    int currentStage = currentPath.getStage();
                    int currentGroup = currentPath.getGroup();
                    if (stage != currentStage) {
                        stage = currentStage;
                        group = currentGroup;
                        stagePaths = new ArrayList();
                        stagePaths.add(currentPath);
                        beforePath = currentPath;
                        mSectionData.put(mSectionData.size(), stagePaths);
                    } else {
                        if (group != currentGroup && stagePaths != null) {
                            stagePaths.add(currentPath);
                            group = currentGroup;
                        } else if (beforePath != null) {
                            beforePath.mNext = currentPath;
                        }
                        beforePath = currentPath;
                    }
                }
            }
        }
    }

    public static void calculateScale(Context context) {
        mScalePathData = (context.getResources().getDimension(2131559153) / 960.0f) * 2.0f;
        Log.i("fingerprint.AnimationUtil", "" + mScalePathData);
    }

    private static void creatXmlParser(Context context, int valueId) {
        try {
            if (xmlParser != null) {
                xmlParser.close();
            }
            xmlParser = context.getResources().getXml(valueId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<TouchShowPath> getPaths(Context context, int valueId) {
        creatXmlParser(context, valueId);
        String objectName = TouchShowPath.class.getSimpleName();
        ArrayList<TouchShowPath> allData = new ArrayList();
        try {
            for (int event = xmlParser.getEventType(); event != 1; event = xmlParser.next()) {
                switch (event) {
                    case 2:
                        if (!objectName.equals(xmlParser.getName())) {
                            break;
                        }
                        TouchShowPath newInstance = new TouchShowPath();
                        newInstance.setmSweepAngle(Float.valueOf(xmlParser.getAttributeValue(0)).floatValue());
                        newInstance.setmStartAngle(Float.valueOf(xmlParser.getAttributeValue(1)).floatValue());
                        newInstance.setmXLatitute(Float.valueOf(xmlParser.getAttributeValue(2)).floatValue());
                        newInstance.setmYLatitute(Float.valueOf(xmlParser.getAttributeValue(3)).floatValue());
                        newInstance.setmRadius(Float.valueOf(xmlParser.getAttributeValue(4)).floatValue());
                        newInstance.setmStage(Float.valueOf(xmlParser.getAttributeValue(5)).floatValue());
                        newInstance.setmGroup(Float.valueOf(xmlParser.getAttributeValue(6)).floatValue());
                        allData.add(newInstance);
                        break;
                    default:
                        break;
                }
            }
            if (xmlParser != null) {
                try {
                    xmlParser.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (xmlParser != null) {
                try {
                    xmlParser.close();
                } catch (Exception e12) {
                    e12.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (xmlParser != null) {
                try {
                    xmlParser.close();
                } catch (Exception e122) {
                    e122.printStackTrace();
                }
            }
        }
        return allData;
    }
}
