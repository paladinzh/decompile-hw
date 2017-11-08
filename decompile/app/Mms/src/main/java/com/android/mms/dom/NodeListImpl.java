package com.android.mms.dom;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListImpl implements NodeList {
    private boolean mDeepSearch;
    private Node mRootNode;
    private ArrayList<Node> mSearchNodes;
    private ArrayList<Node> mStaticNodes;
    private String mTagName;

    public NodeListImpl(Node rootNode, String tagName, boolean deepSearch) {
        this.mRootNode = rootNode;
        this.mTagName = tagName;
        this.mDeepSearch = deepSearch;
    }

    public NodeListImpl(ArrayList<Node> nodes) {
        this.mStaticNodes = nodes;
    }

    public int getLength() {
        if (this.mStaticNodes != null) {
            return this.mStaticNodes.size();
        }
        fillList(this.mRootNode);
        return this.mSearchNodes.size();
    }

    public Node item(int index) {
        Node node = null;
        if (this.mStaticNodes == null) {
            fillList(this.mRootNode);
            try {
                return (Node) this.mSearchNodes.get(index);
            } catch (IndexOutOfBoundsException e) {
                return node;
            }
        }
        try {
            return (Node) this.mStaticNodes.get(index);
        } catch (IndexOutOfBoundsException e2) {
            return node;
        }
    }

    private void fillList(Node node) {
        if (node == this.mRootNode) {
            this.mSearchNodes = new ArrayList();
        } else if (this.mTagName == null || node.getNodeName().equals(this.mTagName)) {
            this.mSearchNodes.add(node);
        }
        node = node.getFirstChild();
        while (node != null) {
            if (this.mDeepSearch) {
                fillList(node);
            } else if (this.mTagName == null || node.getNodeName().equals(this.mTagName)) {
                this.mSearchNodes.add(node);
            }
            node = node.getNextSibling();
        }
    }
}
