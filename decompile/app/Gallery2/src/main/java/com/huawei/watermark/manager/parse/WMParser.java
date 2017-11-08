package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.view.InflateException;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.WM24HourImage;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.WM24JieQiImage;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.WMMorningOrNightImage;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.WMThreeMealsImage;
import com.huawei.watermark.manager.parse.unit.decidebytime.view.WMThreeMealsText;
import com.huawei.watermark.manager.parse.unit.time.layout.WMMonthContentsLayout;
import com.huawei.watermark.manager.parse.unit.time.layout.WMWeekContentsLayout;
import com.huawei.watermark.manager.parse.unit.time.layout.WMYearContentsLayout;
import com.huawei.watermark.manager.parse.unit.time.view.WMMonthValueTextView;
import com.huawei.watermark.manager.parse.unit.time.view.WMWeekDayTitleTextView;
import com.huawei.watermark.manager.parse.unit.time.view.WMWeekDayValueTextView;
import com.huawei.watermark.manager.parse.util.ParseJson;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmutil.WMAssertUtil;
import com.huawei.watermark.wmutil.WMFileUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

public class WMParser {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMParser.class.getSimpleName());
    private static final Map<String, Class<? extends WMElement>> sTagClasses = new HashMap();

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"REC_CATCH_EXCEPTION"})
    private com.huawei.watermark.manager.parse.WaterMark _parse(java.io.InputStream r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = this;
        r8 = 0;
        r7 = 0;
        r2 = android.util.Xml.newPullParser();
        r3 = new java.util.Stack;
        r3.<init>();
        r6 = "UTF-8";	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        r2.setInput(r10, r6);	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        r1 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        r4 = r2.next();	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x0016:
        r6 = 1;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        if (r4 == r6) goto L_0x0040;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x0019:
        r6 = 2;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        if (r4 != r6) goto L_0x002e;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x001c:
        r1 = r9.newTag(r2);	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        if (r1 == 0) goto L_0x0029;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x0022:
        r6 = r9.newTag(r2);	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        r3.push(r6);	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x0029:
        r4 = r2.next();	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        goto L_0x0016;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x002e:
        r6 = 3;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        if (r4 != r6) goto L_0x0029;	 Catch:{ Exception -> 0x0039, all -> 0x004c }
    L_0x0031:
        r6 = r2.getName();	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        r9.fillTag(r3, r6);	 Catch:{ Exception -> 0x0039, all -> 0x004c }
        goto L_0x0029;
    L_0x0039:
        r0 = move-exception;
        if (r10 == 0) goto L_0x003f;
    L_0x003c:
        com.huawei.watermark.wmutil.WMFileUtil.closeSilently(r10);
    L_0x003f:
        return r7;
    L_0x0040:
        if (r10 == 0) goto L_0x0045;
    L_0x0042:
        com.huawei.watermark.wmutil.WMFileUtil.closeSilently(r10);
    L_0x0045:
        r6 = r3.size();
        if (r6 != 0) goto L_0x0053;
    L_0x004b:
        return r7;
    L_0x004c:
        r6 = move-exception;
        if (r10 == 0) goto L_0x0052;
    L_0x004f:
        com.huawei.watermark.wmutil.WMFileUtil.closeSilently(r10);
    L_0x0052:
        throw r6;
    L_0x0053:
        r5 = r3.get(r8);
        r5 = (com.huawei.watermark.manager.parse.WaterMark) r5;
        r5.setPath(r11);
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.watermark.manager.parse.WMParser._parse(java.io.InputStream, java.lang.String):com.huawei.watermark.manager.parse.WaterMark");
    }

    static {
        sTagClasses.put("wm", WaterMark.class);
        sTagClasses.put("wmconfig", WMConfig.class);
        sTagClasses.put("image", WMImage.class);
        sTagClasses.put("linearlayout", WMLinearLayout.class);
        sTagClasses.put("relativelayout", WMRelativeLayout.class);
        sTagClasses.put("date", WMDate.class);
        sTagClasses.put("dateimage", WMDateImage.class);
        sTagClasses.put("weekimage", WMWeekImage.class);
        sTagClasses.put(WMEditor.TYPETEXT, WMText.class);
        sTagClasses.put("edittext", WMEditText.class);
        sTagClasses.put("location", WMLocation.class);
        sTagClasses.put(ParseJson.KEY_WEATHER, WMWeather.class);
        sTagClasses.put("latiimage", WMLatiImage.class);
        sTagClasses.put("longiimage", WMLongiImage.class);
        sTagClasses.put("latititletext", WMLatiTitleText.class);
        sTagClasses.put("longititletext", WMLongiTitleText.class);
        sTagClasses.put("altitude", WMAltitude.class);
        sTagClasses.put("altitudeImage", WMAlititudeImage.class);
        sTagClasses.put("steptext", WMHealthStep.class);
        sTagClasses.put("caloriestext", WMCalories.class);
        sTagClasses.put("yeaercontentslayout", WMYearContentsLayout.class);
        sTagClasses.put("monthcontentslayout", WMMonthContentsLayout.class);
        sTagClasses.put("weekcontentslayout", WMWeekContentsLayout.class);
        sTagClasses.put("monthvaluetextview", WMMonthValueTextView.class);
        sTagClasses.put("weekdaytitletextview", WMWeekDayTitleTextView.class);
        sTagClasses.put("weekdayvaluetextview", WMWeekDayValueTextView.class);
        sTagClasses.put("threemealstext", WMThreeMealsText.class);
        sTagClasses.put("threemealsimage", WMThreeMealsImage.class);
        sTagClasses.put("morningornightimage", WMMorningOrNightImage.class);
        sTagClasses.put("wm24jieqiimage", WM24JieQiImage.class);
        sTagClasses.put("seasonimage", WMSeasonImage.class);
        sTagClasses.put("wm24hourimage", WM24HourImage.class);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<WaterMark> parse(Context context, String path) {
        boolean z;
        if (WMStringUtil.isEmptyString(path)) {
            z = false;
        } else {
            z = true;
        }
        WMAssertUtil.Assert(z, String.format("wm path cannot be null，path： %s", new Object[]{path}));
        Closeable closeable = null;
        ArrayList<WaterMark> mWaterMarkList = new ArrayList();
        try {
            ZipFile zis = WMFileProcessor.getInstance().openZipFile(path);
            if (zis != null) {
                Enumeration<? extends ZipEntry> entries = zis.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (entry.getName().contains(".xml")) {
                        closeable = zis.getInputStream(entry);
                        WaterMark wm = _parse(closeable, path);
                        if (wm != null) {
                            mWaterMarkList.add(wm);
                        }
                        WMFileUtil.closeSilently(closeable);
                    }
                }
            }
            WMFileUtil.closeSilently(closeable);
            WMFileUtil.closeSilently(zis);
        } catch (Exception e) {
            WMLog.e(TAG, "parse xml got an exception", e);
        } catch (Throwable th) {
            WMFileUtil.closeSilently(null);
            WMFileUtil.closeSilently(null);
        }
        return mWaterMarkList;
    }

    private void fillTag(Stack<WMElement> tags, String tagName) {
        if (tags.size() > 1) {
            WMElement currentTag = (WMElement) tags.pop();
            WMElement lastTag = (WMElement) tags.peek();
            try {
                Method method = getMethod(lastTag.getClass(), "addElement");
                if (method == null) {
                    WMLog.e(TAG, String.format("cannot find method : %s in class %s", new Object[]{"addElement", clazz.toString()}));
                    return;
                }
                method.invoke(lastTag, new Object[]{currentTag});
            } catch (Exception e) {
                throw new InflateException("fillTag got exception.", e);
            }
        }
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    @SuppressWarnings({"REC_CATCH_EXCEPTION"})
    private WMElement newTag(XmlPullParser parser) {
        try {
            return (WMElement) ((Class) sTagClasses.get(parser.getName())).getConstructor(new Class[]{XmlPullParser.class}).newInstance(new Object[]{parser});
        } catch (Exception e) {
            WMLog.e(TAG, "Parse WM got a exception", e);
            return null;
        }
    }
}
