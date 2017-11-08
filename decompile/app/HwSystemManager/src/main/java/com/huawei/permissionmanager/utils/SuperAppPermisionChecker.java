package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class SuperAppPermisionChecker {
    private static final String SUPER_ASSEST_PATH = "permission/hsm_permission_super_apps.xml";
    private static final String SUPER_CUST_PATH = "data/cust/xml/hsm/permission/hsm_permission_super_apps.xml";
    private static final String TAG = SuperAppPermisionChecker.class.getSimpleName();
    private static SuperAppPermisionChecker mChecker;
    private Context mContext;
    private final Map<String, List<Integer>> mSuperAppList = new HashMap();

    private class ReadSuperApp extends AsyncTask<String, Void, Map<String, List<Integer>>> {
        private ReadSuperApp() {
        }

        protected Map<String, List<Integer>> doInBackground(String... arg0) {
            Map<String, List<Integer>> xmlData;
            Map<String, List<Integer>> superAppList = new HashMap();
            InputStream inputStream = null;
            try {
                inputStream = SuperAppPermisionChecker.this.mContext.getAssets().open(SuperAppPermisionChecker.SUPER_ASSEST_PATH);
                if (inputStream != null) {
                    xmlData = SuperAppPermisionChecker.readDataFromXML(inputStream);
                    if (xmlData != null) {
                        superAppList.putAll(xmlData);
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e2) {
                HwLog.w(SuperAppPermisionChecker.TAG, "read assests file failed");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (Exception e4) {
                HwLog.w(SuperAppPermisionChecker.TAG, "read assests file failed");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
            }
            InputStream inputStream2 = null;
            try {
                File custFile = new File(SuperAppPermisionChecker.SUPER_CUST_PATH);
                if (custFile.exists()) {
                    inputStream2 = new FileInputStream(custFile);
                }
                if (inputStream2 != null) {
                    xmlData = SuperAppPermisionChecker.readDataFromXML(inputStream2);
                    if (xmlData != null) {
                        superAppList.putAll(xmlData);
                    }
                }
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e5) {
                e5.printStackTrace();
                HwLog.w(SuperAppPermisionChecker.TAG, "read cust file not found");
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
            } catch (Exception e6) {
                e6.printStackTrace();
                HwLog.w(SuperAppPermisionChecker.TAG, "read cust file failed");
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
            } catch (Throwable th2) {
                if (inputStream2 != null) {
                    try {
                        inputStream2.close();
                    } catch (IOException e3222222) {
                        e3222222.printStackTrace();
                    }
                }
            }
            return superAppList;
        }

        protected void onPostExecute(Map<String, List<Integer>> result) {
            super.onPostExecute(result);
            synchronized (this) {
                SuperAppPermisionChecker.this.mSuperAppList.clear();
                SuperAppPermisionChecker.this.mSuperAppList.putAll(result);
            }
            HwLog.i(SuperAppPermisionChecker.TAG, "read xml finished");
        }
    }

    private SuperAppPermisionChecker(Context mContext) {
        this.mContext = mContext;
        if (this.mSuperAppList.isEmpty()) {
            readSuperAppFromXML();
        }
    }

    public static synchronized SuperAppPermisionChecker getInstance(Context mContext) {
        SuperAppPermisionChecker superAppPermisionChecker;
        synchronized (SuperAppPermisionChecker.class) {
            if (mChecker == null) {
                mChecker = new SuperAppPermisionChecker(mContext);
            }
            superAppPermisionChecker = mChecker;
        }
        return superAppPermisionChecker;
    }

    private void readSuperAppFromXML() {
        new ReadSuperApp().execute(new String[0]);
    }

    public synchronized boolean checkIfIsInAppPermissionList(String packageName, int permissionType) {
        if (this.mSuperAppList.isEmpty()) {
            return false;
        }
        if (!this.mSuperAppList.containsKey(packageName)) {
            return false;
        }
        List<Integer> tempInteger = (List) this.mSuperAppList.get(packageName);
        if (tempInteger == null) {
            return false;
        }
        return tempInteger.contains(Integer.valueOf(permissionType));
    }

    public static Map<String, List<Integer>> readDataFromXML(InputStream xml) throws Exception {
        Map<String, List<Integer>> data = null;
        List intList = null;
        String tempName = "";
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(xml, "UTF-8");
        for (int event = parser.getEventType(); event != 1; event = parser.next()) {
            switch (event) {
                case 0:
                    data = new HashMap();
                    break;
                case 2:
                    if (!"package".equals(parser.getName())) {
                        if (!"subPermission".equals(parser.getName())) {
                            break;
                        }
                        if ("name".equals(parser.getAttributeName(0)) && intList != null) {
                            intList.add(Integer.valueOf(parser.getAttributeValue(0)));
                            break;
                        }
                    }
                    intList = new ArrayList();
                    if (!"packageName".equals(parser.getAttributeName(0))) {
                        break;
                    }
                    tempName = parser.getAttributeValue(0);
                    break;
                case 3:
                    if ("package".equals(parser.getName()) && data != null) {
                        data.put(tempName, intList);
                        intList = null;
                        break;
                    }
                default:
                    break;
            }
        }
        return data;
    }
}
