package com.googlecode.mp4parser.util;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Path {
    static final /* synthetic */ boolean -assertionsDisabled;
    static Pattern component = Pattern.compile("(....|\\.\\.)(\\[(.*)\\])?");

    private Path() {
    }

    static {
        boolean z;
        if (Path.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public static Box getPath(Box box, String path) {
        List<Box> all = getPaths(box, path);
        return all.isEmpty() ? null : (Box) all.get(0);
    }

    public static List<Box> getPaths(Box box, String path) {
        if (path.startsWith("/")) {
            Box isoFile = box;
            while (isoFile.getParent() != null) {
                isoFile = isoFile.getParent();
            }
            if (-assertionsDisabled || (isoFile instanceof IsoFile)) {
                return getPaths(isoFile, path.substring(1));
            }
            throw new AssertionError(isoFile.getType() + " has no parent");
        } else if (path.isEmpty()) {
            return Collections.singletonList(box);
        } else {
            String later;
            String now;
            if (path.contains("/")) {
                later = path.substring(path.indexOf(47) + 1);
                now = path.substring(0, path.indexOf(47));
            } else {
                now = path;
                later = "";
            }
            Matcher m = component.matcher(now);
            if (m.matches()) {
                String type = m.group(1);
                if ("..".equals(type)) {
                    return getPaths(box.getParent(), later);
                }
                int index = -1;
                if (m.group(2) != null) {
                    index = Integer.parseInt(m.group(3));
                }
                List<Box> children = new LinkedList();
                int currentIndex = 0;
                for (Box box1 : ((ContainerBox) box).getBoxes()) {
                    if (box1.getType().matches(type)) {
                        if (index == -1 || index == currentIndex) {
                            children.addAll(getPaths(box1, later));
                        }
                        currentIndex++;
                    }
                }
                return children;
            }
            throw new RuntimeException(now + " is invalid path.");
        }
    }
}
