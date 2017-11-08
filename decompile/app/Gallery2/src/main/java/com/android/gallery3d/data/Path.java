package com.android.gallery3d.data;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.IdentityCache;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Path {
    private static Path sRoot = new Path(null, "ROOT");
    private IdentityCache<String, Path> mChildren;
    private WeakReference<MediaObject> mObject;
    private final Path mParent;
    private final String mSegment;

    private Path(Path parent, String segment) {
        this.mParent = parent;
        this.mSegment = segment;
    }

    public Path getChild(String segment) {
        synchronized (Path.class) {
            Path p;
            if (this.mChildren == null) {
                this.mChildren = new IdentityCache();
            } else {
                p = (Path) this.mChildren.get(segment);
                if (p != null) {
                    return p;
                }
            }
            p = new Path(this, segment);
            this.mChildren.put(segment, p);
            return p;
        }
    }

    public Path getParent() {
        Path path;
        synchronized (Path.class) {
            path = this.mParent;
        }
        return path;
    }

    public Path getChild(int segment) {
        return getChild(String.valueOf(segment));
    }

    public Path getChild(long segment) {
        return getChild(String.valueOf(segment));
    }

    public void setObject(MediaObject object) {
        boolean z = true;
        synchronized (Path.class) {
            if (!(this.mObject == null || this.mObject.get() == null)) {
                z = false;
            }
            Utils.assertTrue(z);
            this.mObject = new WeakReference(object);
        }
    }

    public void clearObject() {
        synchronized (Path.class) {
            this.mObject = null;
        }
    }

    public MediaObject getObject() {
        MediaObject mediaObject = null;
        synchronized (Path.class) {
            if (this.mObject != null) {
                mediaObject = (MediaObject) this.mObject.get();
            }
        }
        return mediaObject;
    }

    public String toString() {
        StringBuilder sb;
        synchronized (Path.class) {
            sb = new StringBuilder();
            String[] segments = split();
            for (String append : segments) {
                sb.append("/");
                sb.append(append);
            }
        }
        return sb.toString();
    }

    public boolean equalsIgnoreCase(String p) {
        return toString().equalsIgnoreCase(p);
    }

    public boolean equalsIgnoreCase(MediaObject mo) {
        if (mo == null || mo.getPath() == null) {
            return false;
        }
        return equalsIgnoreCase(mo.getPath().toString());
    }

    public static Path fromString(String s) {
        Path current;
        synchronized (Path.class) {
            String[] segments = split(s);
            current = sRoot;
            for (String child : segments) {
                current = current.getChild(child);
            }
        }
        return current;
    }

    public String[] split() {
        String[] segments;
        synchronized (Path.class) {
            Path p;
            int n = 0;
            for (p = this; p != sRoot; p = p.mParent) {
                n++;
            }
            segments = new String[n];
            p = this;
            int i = n - 1;
            while (p != sRoot) {
                int i2 = i - 1;
                segments[i] = p.mSegment;
                p = p.mParent;
                i = i2;
            }
        }
        return segments;
    }

    public static String[] split(String s) {
        int n = s.length();
        if (n == 0) {
            return new String[0];
        }
        if (s.charAt(0) != '/') {
            throw new RuntimeException("malformed path:" + s);
        }
        ArrayList<String> segments = new ArrayList();
        int i = 1;
        while (i < n) {
            int brace = 0;
            int j = i;
            while (j < n) {
                char c = s.charAt(j);
                if (c != '{') {
                    if (c != '}') {
                        if (brace == 0 && c == '/') {
                            break;
                        }
                    }
                    brace--;
                } else {
                    brace++;
                }
                j++;
            }
            if (brace != 0) {
                throw new RuntimeException("unbalanced brace in path:" + s);
            }
            segments.add(s.substring(i, j));
            i = j + 1;
        }
        String[] result = new String[segments.size()];
        segments.toArray(result);
        return result;
    }

    public static String[] splitSequence(String s) {
        int n = s.length();
        if (s.charAt(0) == '{' && s.charAt(n - 1) == '}') {
            ArrayList<String> segments = new ArrayList();
            int j;
            for (int i = 1; i < n - 1; i = j + 1) {
                int brace = 0;
                j = i;
                while (j < n - 1) {
                    char c = s.charAt(j);
                    if (c != '{') {
                        if (c != '}') {
                            if (brace == 0 && c == ',') {
                                break;
                            }
                        }
                        brace--;
                    } else {
                        brace++;
                    }
                    j++;
                }
                if (brace != 0) {
                    throw new RuntimeException("unbalanced brace in path:" + s);
                }
                segments.add(s.substring(i, j));
            }
            String[] result = new String[segments.size()];
            segments.toArray(result);
            return result;
        }
        throw new RuntimeException("bad sequence: " + s);
    }

    public String getPrefix() {
        if (this == sRoot) {
            return "";
        }
        return getPrefixPath().mSegment;
    }

    public Path getPrefixPath() {
        Path current;
        synchronized (Path.class) {
            current = this;
            if (this == sRoot) {
                throw new IllegalStateException();
            }
            while (current.mParent != sRoot) {
                current = current.mParent;
            }
        }
        return current;
    }

    public String getSuffix() {
        return this.mSegment;
    }
}
