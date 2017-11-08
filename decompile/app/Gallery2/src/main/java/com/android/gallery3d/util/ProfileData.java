package com.android.gallery3d.util;

import com.android.gallery3d.common.Utils;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class ProfileData {
    private static final String TAG = "ProfileData";
    private HashMap<String, Integer> mNameToId = new HashMap();
    private int mNextId;
    private DataOutputStream mOut;
    private Node mRoot = new Node(null, -1);
    private byte[] mScratch = new byte[4];

    private static class Node {
        public ArrayList<Node> children;
        public int id;
        public Node parent;
        public int sampleCount;

        public Node(Node parent, int id) {
            this.parent = parent;
            this.id = id;
        }
    }

    public void reset() {
        this.mRoot = new Node(null, -1);
        this.mNameToId.clear();
        this.mNextId = 0;
    }

    private int nameToId(String name) {
        Integer id = (Integer) this.mNameToId.get(name);
        if (id == null) {
            int i = this.mNextId + 1;
            this.mNextId = i;
            id = Integer.valueOf(i);
            this.mNameToId.put(name, id);
        }
        return id.intValue();
    }

    public void addSample(String[] stack) {
        int i;
        int[] ids = new int[stack.length];
        for (i = 0; i < stack.length; i++) {
            ids[i] = nameToId(stack[i]);
        }
        Node node = this.mRoot;
        for (i = stack.length - 1; i >= 0; i--) {
            if (node.children == null) {
                node.children = new ArrayList();
            }
            int id = ids[i];
            ArrayList<Node> children = node.children;
            int j = 0;
            while (j < children.size() && ((Node) children.get(j)).id != id) {
                j++;
            }
            if (j == children.size()) {
                children.add(new Node(node, id));
            }
            node = (Node) children.get(j);
        }
        node.sampleCount++;
    }

    public void dumpToFile(String filename) {
        try {
            this.mOut = new DataOutputStream(new FileOutputStream(filename));
            writeInt(0);
            writeInt(3);
            writeInt(1);
            writeInt(20000);
            writeInt(0);
            writeAllStacks(this.mRoot, 0);
            writeInt(0);
            writeInt(1);
            writeInt(0);
            writeAllSymbols();
        } catch (Throwable ex) {
            GalleryLog.w("Failed to dump to file", ex);
        } finally {
            Utils.closeSilently(this.mOut);
        }
    }

    private void writeOneStack(Node node, int depth) throws IOException {
        writeInt(node.sampleCount);
        writeInt(depth);
        int depth2 = depth;
        while (true) {
            depth = depth2 - 1;
            if (depth2 > 0) {
                writeInt(node.id);
                node = node.parent;
                depth2 = depth;
            } else {
                return;
            }
        }
    }

    private void writeAllStacks(Node node, int depth) throws IOException {
        if (node.sampleCount > 0) {
            writeOneStack(node, depth);
        }
        ArrayList<Node> children = node.children;
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                writeAllStacks((Node) children.get(i), depth + 1);
            }
        }
    }

    private void writeAllSymbols() throws IOException {
        for (Entry<String, Integer> entry : this.mNameToId.entrySet()) {
            this.mOut.writeBytes(String.format("0x%x %s\n", new Object[]{entry.getValue(), entry.getKey()}));
        }
    }

    private void writeInt(int v) throws IOException {
        this.mScratch[0] = (byte) v;
        this.mScratch[1] = (byte) (v >> 8);
        this.mScratch[2] = (byte) (v >> 16);
        this.mScratch[3] = (byte) (v >> 24);
        this.mOut.write(this.mScratch);
    }
}
