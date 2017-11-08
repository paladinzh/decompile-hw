package com.huawei.powergenie.core.modulesmanager;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.XmlHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

public final class ModuleLifecycle {
    private final HashMap<Integer, ModuleProperty> mModuleProps = new HashMap();
    private final HashMap<Integer, RunState> mModuleState = new HashMap();
    private final ArrayList<BaseModule> sModules = new ArrayList();

    private static final class ModuleProperty {
        public boolean mEnable;
        public final String mEntryCls;
        public final int mModId;
        public final boolean mPersist;

        public ModuleProperty(String cls, int modId, boolean enable, boolean persist) {
            this.mEntryCls = cls;
            this.mModId = modId;
            this.mEnable = enable;
            this.mPersist = persist;
        }
    }

    enum RunState {
        INIT,
        RUNNING,
        STOPPED
    }

    protected ModuleLifecycle(Context context) {
        loadModuleList(context);
    }

    private BaseModule loadModule(int modId, ICoreContext context) {
        ModuleProperty prop = (ModuleProperty) this.mModuleProps.get(Integer.valueOf(modId));
        if (prop == null) {
            Log.e("ModuleLifecycle", "modId: " + modId + " is unknown.");
            return null;
        } else if (!prop.mEnable) {
            Log.d("ModuleLifecycle", "modId: " + modId + " disabled, not loaded.");
            return null;
        } else if (TextUtils.isEmpty(prop.mEntryCls)) {
            Log.e("ModuleLifecycle", "modId: " + modId + ", entry cls is not config.");
            return null;
        } else if (context.isQcommPlatform() || !prop.mEntryCls.equals("fastrrc.PacketsControl")) {
            BaseModule module = newPGModule(prop.mEntryCls, context, modId);
            if (module != null) {
                module.onCreate();
                this.sModules.add(module);
                this.mModuleState.put(Integer.valueOf(modId), RunState.INIT);
            } else {
                Log.e("ModuleLifecycle", "modId: " + modId + " created failure.");
            }
            return module;
        } else {
            Log.i("ModuleLifecycle", "not qcomm platform,not load fastrrc.PacketsControl");
            return null;
        }
    }

    protected ArrayList<BaseModule> loadAll(ICoreContext context) {
        if (this.sModules.isEmpty()) {
            for (Integer modId : this.mModuleProps.keySet()) {
                loadModule(modId.intValue(), context);
            }
        } else {
            Log.w("ModuleLifecycle", "no any moudles.");
        }
        return this.sModules;
    }

    private boolean startModule(BaseModule module) {
        int modId = module.getModId();
        if (this.mModuleState.get(Integer.valueOf(modId)) == RunState.RUNNING) {
            Log.w("ModuleLifecycle", "modId: " + modId + " is running.");
            return true;
        } else if (((ModuleProperty) this.mModuleProps.get(Integer.valueOf(modId))).mEnable) {
            module.onStart();
            this.mModuleState.put(Integer.valueOf(modId), RunState.RUNNING);
            return true;
        } else {
            Log.i("ModuleLifecycle", "modId: " + modId + " is disable and not start.");
            return false;
        }
    }

    protected void handleStart() {
        for (BaseModule module : this.sModules) {
            startModule(module);
        }
    }

    private String getKeyName(int modId) {
        return null;
    }

    protected ArrayList<BaseModule> getRunningModules() {
        ArrayList<BaseModule> runningModules = new ArrayList();
        for (BaseModule module : this.sModules) {
            if (this.mModuleState.get(Integer.valueOf(module.getModId())) == RunState.RUNNING) {
                runningModules.add(module);
            }
        }
        return runningModules;
    }

    private BaseModule newPGModule(String classPath, ICoreContext context, int modId) {
        BaseModule baseModule = null;
        try {
            baseModule = (BaseModule) Class.forName("com.huawei.powergenie.modules." + classPath).newInstance();
            baseModule.attach(context, modId);
            return baseModule;
        } catch (Exception e) {
            e.printStackTrace();
            return baseModule;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadModuleList(Context context) {
        boolean ret = false;
        XmlResourceParser xmlResourceParser = null;
        try {
            String tag;
            xmlResourceParser = context.getResources().getXml(R.xml.modules_config);
            XmlHelper.beginDocument(xmlResourceParser, "module_list");
            String entryCls = null;
            int modId = 0;
            boolean enable = true;
            boolean persist = false;
            while (true) {
                XmlHelper.nextElement(xmlResourceParser);
                tag = xmlResourceParser.getName();
                if (tag != null) {
                    if (!"module".equals(tag)) {
                        if (!"item".equals(tag)) {
                            break;
                        }
                        String type = xmlResourceParser.getAttributeValue(0);
                        String text = null;
                        if (xmlResourceParser.next() == 4) {
                            text = xmlResourceParser.getText();
                        }
                        if ("enable".equals(type)) {
                            enable = "true".equalsIgnoreCase(text);
                            if (getKeyName(modId) != null) {
                            }
                        } else if ("persist".equals(type)) {
                            persist = "true".equalsIgnoreCase(text);
                        } else {
                            Log.w("ModuleLifecycle", "type: " + type + " is unknown. ");
                        }
                    } else {
                        if (!(entryCls == null || modId == 0)) {
                            this.mModuleProps.put(Integer.valueOf(modId), new ModuleProperty(entryCls, modId, enable, persist));
                        }
                        entryCls = xmlResourceParser.getAttributeValue(0);
                        modId = Integer.parseInt(xmlResourceParser.getAttributeValue(1));
                        if (TextUtils.isEmpty(entryCls)) {
                            Log.w("ModuleLifecycle", "Mod: " + modId + " is not config.");
                            entryCls = null;
                            modId = 0;
                        }
                    }
                } else {
                    break;
                }
                ret = true;
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return ret;
            }
            Log.e("ModuleLifecycle", "tag: " + tag + " is unknown. ");
            ret = true;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (IOException e4) {
            e4.printStackTrace();
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
        return ret;
    }
}
