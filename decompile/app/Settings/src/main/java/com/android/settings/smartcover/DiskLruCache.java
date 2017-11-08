package com.android.settings.smartcover;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DiskLruCache implements Closeable {
    private final int appVersion;
    private final Callable<Void> cleanupCallable = new Callable<Void>() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Void call() throws Exception {
            synchronized (DiskLruCache.this) {
                if (DiskLruCache.this.journalWriter == null) {
                    return null;
                }
                DiskLruCache.this.trimToSize();
                if (DiskLruCache.this.journalRebuildRequired()) {
                    DiskLruCache.this.rebuildJournal();
                    DiskLruCache.this.redundantOpCount = 0;
                }
            }
        }
    };
    private final File directory;
    private final ExecutorService executorService = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    private final File journalFile;
    private final File journalFileTmp;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap(0, 0.75f, true);
    private final long maxSize;
    private long nextSequenceNumber = 0;
    private int redundantOpCount;
    private long size = 0;
    private final int valueCount;

    public final class Editor {
        private final Entry entry;
        private boolean hasErrors;

        private class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }

            public void write(int oneByte) {
                try {
                    this.out.write(oneByte);
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void write(byte[] buffer, int offset, int length) {
                try {
                    this.out.write(buffer, offset, length);
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void close() {
                try {
                    this.out.close();
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void flush() {
                try {
                    this.out.flush();
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }
        }

        private Editor(Entry entry) {
            this.entry = entry;
        }

        public OutputStream newOutputStream(int index) throws IOException {
            OutputStream faultHidingOutputStream;
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor != this) {
                    throw new IllegalStateException();
                }
                faultHidingOutputStream = new FaultHidingOutputStream(new FileOutputStream(this.entry.getDirtyFile(index)));
            }
            return faultHidingOutputStream;
        }

        public void commit() throws IOException {
            if (this.hasErrors) {
                DiskLruCache.this.completeEdit(this, false);
                DiskLruCache.this.remove(this.entry.key);
                return;
            }
            DiskLruCache.this.completeEdit(this, true);
        }

        public void abort() throws IOException {
            DiskLruCache.this.completeEdit(this, false);
        }
    }

    private final class Entry {
        private Editor currentEditor;
        private final String key;
        private final long[] lengths;
        private boolean readable;
        private long sequenceNumber;

        private Entry(String key) {
            this.key = key;
            this.lengths = new long[DiskLruCache.this.valueCount];
        }

        public String getLengths() throws IOException {
            StringBuilder result = new StringBuilder();
            for (long size : this.lengths) {
                result.append(' ').append(size);
            }
            return result.toString();
        }

        private void setLengths(String[] strings) throws IOException {
            if (strings.length != DiskLruCache.this.valueCount) {
                throw invalidLengths(strings);
            }
            int i = 0;
            while (i < strings.length) {
                try {
                    this.lengths[i] = Long.parseLong(strings[i]);
                    i++;
                } catch (NumberFormatException e) {
                    throw invalidLengths(strings);
                }
            }
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        public File getCleanFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "." + i);
        }

        public File getDirtyFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "." + i + ".tmp");
        }
    }

    public final class Snapshot implements Closeable {
        private final InputStream[] ins;
        private final String key;
        private final long sequenceNumber;

        private Snapshot(String key, long sequenceNumber, InputStream[] ins) {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.ins = ins;
        }

        public InputStream getInputStream(int index) {
            return this.ins[index];
        }

        public void close() {
            for (InputStream in : this.ins) {
                DiskLruCache.closeQuietly(in);
            }
        }
    }

    private static <T> T[] copyOfRange(T[] original, int start, int end) {
        if (original == null) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start > end) {
            throw new IllegalArgumentException();
        } else if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int resultLength = end - start;
            Object[] result = (Object[]) Array.newInstance(original.getClass().getComponentType(), resultLength);
            System.arraycopy(original, start, result, 0, Math.min(resultLength, originalLength - start));
            return result;
        }
    }

    public static String readAsciiLine(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(80);
        while (true) {
            int c = in.read();
            if (c == -1) {
                throw new EOFException();
            } else if (c == 10) {
                break;
            } else {
                result.append((char) c);
            }
        }
        int length = result.length();
        if (length > 0 && result.charAt(length - 1) == '\r') {
            result.setLength(length - 1);
        }
        return result.toString();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException e) {
                Log.e("DiskLruCache", "close quietly in error");
            } catch (Exception e2) {
            }
        }
    }

    public static void deleteContents(File dir) throws IOException {
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                int i = 0;
                int length = files.length;
                while (i < length) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        deleteContents(file);
                    }
                    if (file.delete()) {
                        i++;
                    } else {
                        throw new IOException("failed to delete file: " + file);
                    }
                }
            }
        }
    }

    private DiskLruCache(File directory, int appVersion, int valueCount, long maxSize) {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, "journal");
        this.journalFileTmp = new File(directory, "journal.tmp");
        this.valueCount = valueCount;
        this.maxSize = maxSize;
    }

    public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize) throws IOException {
        Throwable th;
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (valueCount <= 0) {
            throw new IllegalArgumentException("valueCount <= 0");
        } else {
            DiskLruCache cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
            synchronized (cache) {
                DiskLruCache diskLruCache;
                try {
                    if (cache.journalFile.exists()) {
                        cache.readJournal();
                        cache.processJournal();
                        cache.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache.journalFile, true), "UTF-8"));
                        return cache;
                    }
                } catch (IOException e) {
                    cache.delete();
                } catch (Throwable th2) {
                    th = th2;
                    diskLruCache = cache;
                    throw th;
                }
                diskLruCache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
                try {
                    diskLruCache.rebuildJournal();
                    return diskLruCache;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    private void readJournal() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(this.journalFile), 8192);
        try {
            String magic = readAsciiLine(in);
            String version = readAsciiLine(in);
            String appVersionString = readAsciiLine(in);
            String valueCountString = readAsciiLine(in);
            String blank = readAsciiLine(in);
            if ("libcore.io.DiskLruCache".equals(magic) && "1".equals(version) && Integer.toString(this.appVersion).equals(appVersionString) && Integer.toString(this.valueCount).equals(valueCountString) && "".equals(blank)) {
                while (true) {
                    try {
                        readJournalLine(readAsciiLine(in));
                    } catch (EOFException e) {
                        closeQuietly(in);
                        return;
                    }
                }
            }
            throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
        } catch (Throwable th) {
            closeQuietly(in);
        }
    }

    private void readJournalLine(String line) throws IOException {
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new IOException("unexpected journal line: " + line);
        }
        String key = parts[1];
        if (parts[0].equals("REMOVE") && parts.length == 2) {
            this.lruEntries.remove(key);
            return;
        }
        Entry entry = (Entry) this.lruEntries.get(key);
        if (entry == null) {
            entry = new Entry(key);
            this.lruEntries.put(key, entry);
        }
        if (parts[0].equals("CLEAN") && parts.length == this.valueCount + 2) {
            entry.readable = true;
            entry.currentEditor = null;
            entry.setLengths((String[]) copyOfRange(parts, 2, parts.length));
        } else if (parts[0].equals("DIRTY") && parts.length == 2) {
            entry.currentEditor = new Editor(entry);
        } else if (!(parts[0].equals("READ") && parts.length == 2)) {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    private synchronized void processJournal() throws IOException {
        deleteIfExists(this.journalFileTmp);
        Iterator<Entry> i = this.lruEntries.values().iterator();
        while (i.hasNext()) {
            Entry entry = (Entry) i.next();
            int t;
            if (entry.currentEditor == null) {
                for (t = 0; t < this.valueCount; t++) {
                    this.size += entry.lengths[t];
                }
            } else {
                entry.currentEditor = null;
                for (t = 0; t < this.valueCount; t++) {
                    deleteIfExists(entry.getCleanFile(t));
                    deleteIfExists(entry.getDirtyFile(t));
                }
                i.remove();
            }
        }
    }

    private synchronized void rebuildJournal() throws IOException {
        if (this.journalWriter != null) {
            this.journalWriter.close();
        }
        Writer writer = null;
        try {
            Writer writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFileTmp), "UTF-8"), 8192);
            try {
                writer2.write("libcore.io.DiskLruCache");
                writer2.write("\n");
                writer2.write("1");
                writer2.write("\n");
                writer2.write(Integer.toString(this.appVersion));
                writer2.write("\n");
                writer2.write(Integer.toString(this.valueCount));
                writer2.write("\n");
                writer2.write("\n");
                for (Entry entry : this.lruEntries.values()) {
                    if (entry.currentEditor != null) {
                        writer2.write("DIRTY " + entry.key + '\n');
                    } else {
                        writer2.write("CLEAN " + entry.key + entry.getLengths() + '\n');
                    }
                }
                if (writer2 != null) {
                    writer2.close();
                }
            } catch (RuntimeException e) {
                writer = writer2;
            } catch (Exception e2) {
                writer = writer2;
            } catch (Throwable th) {
                Throwable th2 = th;
                writer = writer2;
            }
        } catch (RuntimeException e3) {
            try {
                Log.e("DiskLruCache", "rebuild Journal in error");
                if (writer != null) {
                    writer.close();
                }
                if (!this.journalFileTmp.renameTo(this.journalFile)) {
                    Log.e("DiskLruCache", "journalFileTmp rename fail");
                }
                this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), "UTF-8"), 8192);
            } catch (Throwable th3) {
                th2 = th3;
                if (writer != null) {
                    writer.close();
                }
                throw th2;
            }
        } catch (Exception e4) {
            Log.e("DiskLruCache", "rebuild Journal in error");
            if (writer != null) {
                writer.close();
            }
            if (this.journalFileTmp.renameTo(this.journalFile)) {
                Log.e("DiskLruCache", "journalFileTmp rename fail");
            }
            this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), "UTF-8"), 8192);
        }
        if (this.journalFileTmp.renameTo(this.journalFile)) {
            Log.e("DiskLruCache", "journalFileTmp rename fail");
        }
        this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), "UTF-8"), 8192);
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    public synchronized Snapshot get(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = (Entry) this.lruEntries.get(key);
        if (entry == null) {
            return null;
        }
        if (!entry.readable) {
            return null;
        }
        InputStream[] ins = new InputStream[this.valueCount];
        int i = 0;
        while (i < this.valueCount) {
            try {
                ins[i] = new FileInputStream(entry.getCleanFile(i));
                i++;
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        this.redundantOpCount++;
        this.journalWriter.append("READ " + key + '\n');
        return new Snapshot(key, entry.sequenceNumber, ins);
    }

    public Editor edit(String key) throws IOException {
        return edit(key, -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = (Entry) this.lruEntries.get(key);
        if (expectedSequenceNumber == -1 || (entry != null && entry.sequenceNumber == expectedSequenceNumber)) {
            if (entry == null) {
                entry = new Entry(key);
                this.lruEntries.put(key, entry);
            } else if (entry.currentEditor != null) {
                return null;
            }
            Editor editor = new Editor(entry);
            entry.currentEditor = editor;
            this.journalWriter.write("DIRTY " + key + '\n');
            this.journalWriter.flush();
            return editor;
        }
    }

    private synchronized void completeEdit(Editor editor, boolean success) throws IOException {
        Entry entry = editor.entry;
        if (entry.currentEditor != editor) {
            throw new IllegalStateException();
        }
        int i;
        if (success) {
            if (!entry.readable) {
                i = 0;
                while (i < this.valueCount) {
                    if (entry.getDirtyFile(i).exists()) {
                        i++;
                    } else {
                        editor.abort();
                        throw new IllegalStateException("edit didn't create file " + i);
                    }
                }
            }
        }
        for (i = 0; i < this.valueCount; i++) {
            File dirty = entry.getDirtyFile(i);
            if (!success) {
                deleteIfExists(dirty);
            } else if (dirty.exists()) {
                File clean = entry.getCleanFile(i);
                long oldLength = entry.lengths[i];
                long newLength = clean.length();
                entry.lengths[i] = newLength;
                this.size = (this.size - oldLength) + newLength;
            }
        }
        this.redundantOpCount++;
        entry.currentEditor = null;
        if ((entry.readable | success) != 0) {
            entry.readable = true;
            this.journalWriter.write("CLEAN " + entry.key + entry.getLengths() + '\n');
            if (success) {
                long j = this.nextSequenceNumber;
                this.nextSequenceNumber = 1 + j;
                entry.sequenceNumber = j;
            }
        } else {
            this.lruEntries.remove(entry.key);
            this.journalWriter.write("REMOVE " + entry.key + '\n');
        }
        if ((this.size > this.maxSize || journalRebuildRequired()) && this.executorService.submit(this.cleanupCallable).isDone()) {
            Log.d("DiskLruCache", "executor has been done");
        }
    }

    private boolean journalRebuildRequired() {
        if (this.redundantOpCount < 2000 || this.redundantOpCount < this.lruEntries.size()) {
            return false;
        }
        return true;
    }

    public synchronized boolean remove(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = (Entry) this.lruEntries.get(key);
        if (entry == null || entry.currentEditor != null) {
            return false;
        }
        int i = 0;
        while (i < this.valueCount) {
            File file = entry.getCleanFile(i);
            if (file.delete()) {
                this.size -= entry.lengths[i];
                entry.lengths[i] = 0;
                i++;
            } else {
                throw new IOException("failed to delete " + file);
            }
        }
        this.redundantOpCount++;
        this.journalWriter.append("REMOVE " + key + '\n');
        return true;
    }

    public synchronized boolean isClosed() {
        return this.journalWriter == null;
    }

    private void checkNotClosed() {
        if (this.journalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void flush() throws IOException {
        checkNotClosed();
        trimToSize();
        this.journalWriter.flush();
    }

    public synchronized void close() throws IOException {
        if (this.journalWriter != null) {
            for (Entry entry : new ArrayList(this.lruEntries.values())) {
                if (entry.currentEditor != null) {
                    entry.currentEditor.abort();
                }
            }
            trimToSize();
            this.journalWriter.close();
            this.journalWriter = null;
        }
    }

    private void trimToSize() throws IOException {
        while (this.size > this.maxSize) {
            remove((String) ((java.util.Map.Entry) this.lruEntries.entrySet().iterator().next()).getKey());
        }
    }

    public void delete() throws IOException {
        close();
        if (this.directory != null && this.directory.isDirectory()) {
            deleteContents(this.directory);
        }
    }

    private void validateKey(String key) {
        if (key.contains(" ") || key.contains("\n") || key.contains("\r")) {
            throw new IllegalArgumentException("keys must not contain spaces or newlines: \"" + key + "\"");
        }
    }
}
