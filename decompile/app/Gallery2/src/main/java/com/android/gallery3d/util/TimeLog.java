package com.android.gallery3d.util;

import java.io.PrintStream;
import java.util.Stack;

public class TimeLog {
    private static final String NAME = TimeLog.class.getName();
    static final ThreadLocal<TimeLog> sThreadLocal = new ThreadLocal();
    private Entry current;
    private Stack<Entry> entrys = new Stack();

    static class Entry {
        String endDesc;
        long endTime;
        String formatStr = "%s enter:%s, exit:%s, run-time:%d";
        String startDesc;
        long startTime = System.currentTimeMillis();

        Entry() {
        }

        void end() {
            this.endTime = System.currentTimeMillis();
        }

        long timeSpan() {
            return this.endTime - this.startTime;
        }

        void print(String tag) {
            PrintStream printStream = System.out;
            String str = this.formatStr;
            Object[] objArr = new Object[4];
            if (tag == null) {
                tag = "debug-info";
            }
            objArr[0] = tag;
            objArr[1] = this.startDesc;
            objArr[2] = this.endDesc;
            objArr[3] = Long.valueOf(timeSpan());
            printStream.println(String.format(str, objArr));
        }
    }

    private static TimeLog myLog() {
        TimeLog me = (TimeLog) sThreadLocal.get();
        if (me != null) {
            return me;
        }
        me = new TimeLog();
        sThreadLocal.set(me);
        return me;
    }

    public static void start() {
        myLog().startInner();
    }

    private void startInner() {
        this.current = new Entry();
        this.current.startDesc = findInvoker();
        this.entrys.add(this.current);
    }

    public static void end(String tag) {
        myLog().endInner(tag);
    }

    private void endInner(String tag) {
        this.current.end();
        this.current.endDesc = findInvoker();
        printAndPop(tag);
    }

    private void printAndPop(String tag) {
        this.current.print(tag);
        this.entrys.pop();
        this.current = this.entrys.empty() ? null : (Entry) this.entrys.peek();
    }

    private static String findInvoker() {
        boolean foundName = false;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int i = 0;
        int len = stack.length;
        while (i < len) {
            if (foundName) {
                return String.format("%s(%s:%d)", new Object[]{stack[i].getMethodName(), stack[i].getFileName(), Integer.valueOf(stack[i].getLineNumber())});
            }
            foundName = NAME.equals(stack[i].getClassName());
            i++;
            if (foundName) {
                i++;
            }
        }
        return null;
    }
}
