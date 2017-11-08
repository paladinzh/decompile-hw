package com.android.server.rms.test;

import android.content.Context;
import android.util.Pair;
import com.android.server.rms.statistic.HwResRecord;
import com.android.server.rms.statistic.HwResRecord.Aspect;
import java.io.PrintWriter;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestHwResRecord extends Assert {
    public static final String TAG = "TestHwResRecord";

    public static final void testResRecord(PrintWriter pw, Context context) {
        try {
            testHwResRecord();
            pw.println("<I> testHwResRecord --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> testHwResRecord --fail " + e);
        }
        try {
            testGetGroupName();
            pw.println("<I> getGroupName --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> getGroupName --fail " + e2);
        }
        try {
            testGetSubTypeName();
            pw.println("<I> getSubTypeName --pass");
        } catch (AssertionFailedError e22) {
            pw.println("<I> getSubTypeName --fail " + e22);
        }
        try {
            testGetLevel();
            pw.println("<I> getLevel --pass");
        } catch (AssertionFailedError e222) {
            pw.println("<I> getLevel --fail " + e222);
        }
        try {
            testHandleAspectData();
            pw.println("<I> getAspectData --pass");
            pw.println("<I> updateAspectData --pass");
            pw.println("<I> resetAspectData --pass");
        } catch (AssertionFailedError e2222) {
            pw.println("<I> handleAspectData --fail " + e2222);
        }
    }

    private static final void testHwResRecord() {
        assertNotNull("case 1", new HwResRecord("testGroup", "testSubType", 1));
        assertNotNull("case 2", new HwResRecord(null, null, -1));
    }

    private static final void testGetGroupName() {
        assertEquals("case 1", "testGroup", new HwResRecord("testGroup", "testSubType", 1).getGroupName());
        assertEquals("case 2", null, new HwResRecord(null, null, -1).getGroupName());
    }

    private static final void testGetSubTypeName() {
        assertEquals("case 1", "testSubType", new HwResRecord("testGroup", "testSubType", 1).getSubTypeName());
        assertEquals("case 2", null, new HwResRecord(null, null, -1).getSubTypeName());
    }

    private static final void testGetLevel() {
        assertEquals("case 1", 1, new HwResRecord("testGroup", "testSubType", 1).getLevel());
        assertEquals("case 2", -1, new HwResRecord(null, null, -1).getLevel());
    }

    private static final void testHandleAspectData() {
        HwResRecord rr = new HwResRecord("testGroup", "testSubType", 1);
        assertEquals("case 1", 0, rr.getAspectData().size());
        rr.updateAspectData(null);
        assertEquals("case 2", 0, rr.getAspectData().size());
        ArrayList<Pair<String, Integer>> data = new ArrayList();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag2", Integer.valueOf(2)));
        ArrayList<Pair<String, Integer>> expect = new ArrayList();
        expect.add(Pair.create("tag1", Integer.valueOf(1)));
        expect.add(Pair.create("tag2", Integer.valueOf(2)));
        rr.updateAspectData(data);
        assertEquals("case 3", 2, rr.getAspectData().size());
        assertAspectDataEquals("case 4", expect, rr.getAspectData());
        data.clear();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag2", Integer.valueOf(2)));
        data.add(Pair.create("tag3", Integer.valueOf(3)));
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(2)));
        expect.add(Pair.create("tag2", Integer.valueOf(4)));
        expect.add(Pair.create("tag3", Integer.valueOf(3)));
        rr.updateAspectData(data);
        assertEquals("case 5", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 6", expect, rr.getAspectData());
        data.clear();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag8", Integer.valueOf(2)));
        data.add(Pair.create("tag3", Integer.valueOf(3)));
        data.add(Pair.create("tag4", Integer.valueOf(-1)));
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(3)));
        expect.add(Pair.create("tag2", Integer.valueOf(4)));
        expect.add(Pair.create("tag3", Integer.valueOf(6)));
        rr.updateAspectData(data);
        assertEquals("case 7", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 8", expect, rr.getAspectData());
        data.clear();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag2", Integer.valueOf(2)));
        data.add(Pair.create("tag3", Integer.valueOf(3)));
        data.add(new Pair(null, Integer.valueOf(1)));
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(4)));
        expect.add(Pair.create("tag2", Integer.valueOf(6)));
        expect.add(Pair.create("tag3", Integer.valueOf(9)));
        rr.updateAspectData(data);
        assertEquals("case 9", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 10", expect, rr.getAspectData());
        data.clear();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag2", Integer.valueOf(-1)));
        data.add(Pair.create("tag3", Integer.valueOf(3)));
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(5)));
        expect.add(Pair.create("tag2", Integer.valueOf(6)));
        expect.add(Pair.create("tag3", Integer.valueOf(12)));
        rr.updateAspectData(data);
        assertEquals("case 11", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 12", expect, rr.getAspectData());
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(-1)));
        expect.add(Pair.create("tag2", Integer.valueOf(-1)));
        expect.add(Pair.create("tag3", Integer.valueOf(-1)));
        rr.resetAspectData();
        assertEquals("case 13", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 14", expect, rr.getAspectData());
        data.clear();
        data.add(Pair.create("tag1", Integer.valueOf(1)));
        data.add(Pair.create("tag2", Integer.valueOf(2)));
        data.add(Pair.create("tag3", Integer.valueOf(3)));
        expect.clear();
        expect.add(Pair.create("tag1", Integer.valueOf(1)));
        expect.add(Pair.create("tag2", Integer.valueOf(2)));
        expect.add(Pair.create("tag3", Integer.valueOf(3)));
        rr.updateAspectData(data);
        assertEquals("case 15", 3, rr.getAspectData().size());
        assertAspectDataEquals("case 16", expect, rr.getAspectData());
    }

    private static final void assertAspectDataEquals(String msg, ArrayList<Pair<String, Integer>> expect, ArrayList<Aspect> actual) {
        for (int i = 0; i < expect.size(); i++) {
            assertEquals(msg, (String) ((Pair) expect.get(i)).first, ((Aspect) actual.get(i)).name);
            assertEquals(msg, ((Integer) ((Pair) expect.get(i)).second).intValue(), ((Aspect) actual.get(i)).value);
        }
    }
}
