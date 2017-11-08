package com.android.settings.dashboard.conditional;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Xml;
import com.android.settingslib.Utils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ConditionManager {
    private static final Comparator<Condition> CONDITION_COMPARATOR = new Comparator<Condition>() {
        public int compare(Condition lhs, Condition rhs) {
            return Long.compare(lhs.getLastChange(), rhs.getLastChange());
        }
    };
    private static ConditionManager sInstance;
    private final ArrayList<Condition> mConditions;
    private final Context mContext;
    private final ArrayList<ConditionListener> mListeners = new ArrayList();
    private File mXmlFile;

    public interface ConditionListener {
        void onConditionsChanged();
    }

    private class ConditionLoader extends AsyncTask<Void, Void, ArrayList<Condition>> {
        private ConditionLoader() {
        }

        protected ArrayList<Condition> doInBackground(Void... params) {
            ArrayList<Condition> conditions = new ArrayList();
            ConditionManager.this.mXmlFile = new File(ConditionManager.this.mContext.getFilesDir(), "condition_state.xml");
            if (ConditionManager.this.mXmlFile.exists()) {
                ConditionManager.this.readFromXml(ConditionManager.this.mXmlFile, conditions);
            }
            ConditionManager.this.addMissingConditions(conditions);
            return conditions;
        }

        protected void onPostExecute(ArrayList<Condition> conditions) {
            ConditionManager.this.mConditions.clear();
            ConditionManager.this.mConditions.addAll(conditions);
            int N = ConditionManager.this.mListeners.size();
            for (int i = 0; i < N; i++) {
                ((ConditionListener) ConditionManager.this.mListeners.get(i)).onConditionsChanged();
            }
        }
    }

    private ConditionManager(Context context, boolean loadConditionsNow) {
        this.mContext = context;
        this.mConditions = new ArrayList();
        if (Utils.isEmuiLite()) {
            Log.i("ConditionManager", "emui lite, do not load conditions!");
            return;
        }
        if (loadConditionsNow) {
            ConditionLoader loader = new ConditionLoader();
            loader.onPostExecute(loader.doInBackground(new Void[0]));
        } else {
            new ConditionLoader().execute(new Void[0]);
        }
    }

    public void refreshAll() {
        int N = this.mConditions.size();
        for (int i = 0; i < N; i++) {
            ((Condition) this.mConditions.get(i)).refreshState();
        }
    }

    private void readFromXml(File xmlFile, ArrayList<Condition> conditions) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileReader in = new FileReader(xmlFile);
            parser.setInput(in);
            for (int state = parser.getEventType(); state != 1; state = parser.next()) {
                if ("c".equals(parser.getName())) {
                    int depth = parser.getDepth();
                    String clz = parser.getAttributeValue("", "cls");
                    if (!clz.startsWith("com.android.settings.dashboard.conditional.")) {
                        clz = "com.android.settings.dashboard.conditional." + clz;
                    }
                    Condition condition = createCondition(Class.forName(clz));
                    PersistableBundle bundle = PersistableBundle.restoreFromXml(parser);
                    if (condition != null) {
                        condition.restoreState(bundle);
                        conditions.add(condition);
                    }
                    while (parser.getDepth() > depth) {
                        parser.next();
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            Log.w("ConditionManager", "Problem reading condition_state.xml", e);
        }
    }

    private void saveToXml() {
        try {
            XmlSerializer serializer = Xml.newSerializer();
            FileWriter writer = new FileWriter(this.mXmlFile);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", Boolean.valueOf(true));
            serializer.startTag("", "cs");
            int N = this.mConditions.size();
            for (int i = 0; i < N; i++) {
                PersistableBundle bundle = new PersistableBundle();
                if (((Condition) this.mConditions.get(i)).saveState(bundle)) {
                    serializer.startTag("", "c");
                    serializer.attribute("", "cls", ((Condition) this.mConditions.get(i)).getClass().getSimpleName());
                    bundle.saveToXml(serializer);
                    serializer.endTag("", "c");
                }
            }
            serializer.endTag("", "cs");
            serializer.flush();
            writer.close();
        } catch (Exception e) {
            Log.w("ConditionManager", "Problem writing condition_state.xml", e);
        }
    }

    private void addMissingConditions(ArrayList<Condition> conditions) {
        addIfMissing(HotspotCondition.class, conditions);
        addIfMissing(DndCondition.class, conditions);
        addIfMissing(BatterySaverCondition.class, conditions);
        addIfMissing(CellularDataCondition.class, conditions);
        addIfMissing(BackgroundDataCondition.class, conditions);
        addIfMissing(WorkModeCondition.class, conditions);
        Collections.sort(conditions, CONDITION_COMPARATOR);
    }

    private void addIfMissing(Class<? extends Condition> clz, ArrayList<Condition> conditions) {
        if (getCondition(clz, conditions) == null) {
            conditions.add(createCondition(clz));
        }
    }

    private Condition createCondition(Class<?> clz) {
        if (HotspotCondition.class == clz) {
            return new HotspotCondition(this);
        }
        if (DndCondition.class == clz) {
            return new DndCondition(this);
        }
        if (BatterySaverCondition.class == clz) {
            return new BatterySaverCondition(this);
        }
        if (CellularDataCondition.class == clz) {
            return new CellularDataCondition(this);
        }
        if (BackgroundDataCondition.class == clz) {
            return new BackgroundDataCondition(this);
        }
        if (WorkModeCondition.class == clz) {
            return new WorkModeCondition(this);
        }
        return null;
    }

    Context getContext() {
        return this.mContext;
    }

    public <T extends Condition> T getCondition(Class<T> clz) {
        return getCondition(clz, this.mConditions);
    }

    private <T extends Condition> T getCondition(Class<T> clz, List<Condition> conditions) {
        int N = conditions.size();
        for (int i = 0; i < N; i++) {
            if (clz.equals(((Condition) conditions.get(i)).getClass())) {
                return (Condition) conditions.get(i);
            }
        }
        return null;
    }

    public List<Condition> getConditions() {
        return this.mConditions;
    }

    public void notifyChanged(Condition condition) {
        saveToXml();
        Collections.sort(this.mConditions, CONDITION_COMPARATOR);
        int N = this.mListeners.size();
        for (int i = 0; i < N; i++) {
            ((ConditionListener) this.mListeners.get(i)).onConditionsChanged();
        }
    }

    public void addListener(ConditionListener listener) {
        if (!hasAddListener(listener)) {
            this.mListeners.add(listener);
        }
        listener.onConditionsChanged();
    }

    public void remListener(ConditionListener listener) {
        this.mListeners.remove(listener);
    }

    private boolean hasAddListener(ConditionListener listener) {
        return this.mListeners.contains(listener);
    }

    public static ConditionManager get(Context context) {
        return get(context, true);
    }

    public static ConditionManager get(Context context, boolean loadConditionsNow) {
        if (sInstance == null) {
            sInstance = new ConditionManager(context.getApplicationContext(), loadConditionsNow);
        }
        return sInstance;
    }
}
