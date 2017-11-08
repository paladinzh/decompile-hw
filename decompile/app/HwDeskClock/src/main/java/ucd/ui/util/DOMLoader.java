package ucd.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Xml;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DOMLoader {
    protected static final String TAG = DOMLoader.class.getSimpleName();
    private static ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);

    public interface OnLoadCallback {
        void onLoad(List<Dom> list);

        void onLoadDom(Dom dom, Dom dom2);
    }

    /* renamed from: ucd.ui.util.DOMLoader$1 */
    class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ String val$preLayerCode;
        private final /* synthetic */ int val$preLayerDepth;
        private final /* synthetic */ List val$topRoot;
        private final /* synthetic */ OnLoadCallback val$topTreeCallback;
        private final /* synthetic */ OnLoadCallback val$treeCallback;
        private final /* synthetic */ AtomicInteger[] val$treeInflateCount;
        private final /* synthetic */ String val$url;

        AnonymousClass1(Context context, String str, OnLoadCallback onLoadCallback, OnLoadCallback onLoadCallback2, AtomicInteger[] atomicIntegerArr, List list, int i, String str2) {
            this.val$context = context;
            this.val$url = str;
            this.val$treeCallback = onLoadCallback;
            this.val$topTreeCallback = onLoadCallback2;
            this.val$treeInflateCount = atomicIntegerArr;
            this.val$topRoot = list;
            this.val$preLayerDepth = i;
            this.val$preLayerCode = str2;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            List root = null;
            Exception e;
            try {
                XmlPullParser parser = Xml.newPullParser();
                InputStream is = Downloader.getDownloadStream(this.val$context, this.val$url);
                if (is != null) {
                    List<Dom> root2;
                    parser.setInput(is, "UTF-8");
                    int eventType = parser.getEventType();
                    List stack = null;
                    List<Dom> root3 = null;
                    while (eventType != 1) {
                        Dom parent;
                        switch (eventType) {
                            case 0:
                                root = new ArrayList();
                                stack = new ArrayList();
                                continue;
                            case 2:
                                try {
                                    String name = parser.getName();
                                    XMLAttributeSet attrs = DOMLoader.setAttrs(parser);
                                    parent = null;
                                    if (stack.size() > 0) {
                                        parent = (Dom) stack.get(stack.size() - 1);
                                    }
                                    Dom current = new Dom(parent, name, attrs);
                                    current.mDepth = parser.getDepth();
                                    current.content = parser.getText();
                                    if (parent != null) {
                                        current.mIndexInParent = parent.getChildrenCount();
                                        current.createCode(parent.mDepth, parent.mCode.toString());
                                        if (current.getTagName().equalsIgnoreCase("inflater")) {
                                            DOMLoader.createSubDomTree(this.val$context, current, this.val$topRoot, this.val$topTreeCallback, this.val$treeInflateCount);
                                        }
                                        parent.add(current);
                                    } else {
                                        current.mIndexInParent = root3.size();
                                        current.createCode(this.val$preLayerDepth, this.val$preLayerCode);
                                        if (current.getTagName().equalsIgnoreCase("inflater")) {
                                            DOMLoader.createSubDomTree(this.val$context, current, this.val$topRoot, this.val$topTreeCallback, this.val$treeInflateCount);
                                        }
                                        if (this.val$preLayerDepth != 0) {
                                            root3.add(current);
                                        } else {
                                            this.val$topRoot.add(current);
                                        }
                                    }
                                    if (this.val$treeCallback == null) {
                                        this.val$topTreeCallback.onLoadDom(parent, current);
                                    } else {
                                        this.val$treeCallback.onLoadDom(parent, current);
                                    }
                                    stack.add(current);
                                    root2 = root3;
                                    continue;
                                } catch (XmlPullParserException e2) {
                                    e = e2;
                                    root2 = root3;
                                    break;
                                }
                            case 3:
                                if (stack.size() > 0) {
                                    stack.remove(stack.size() - 1);
                                }
                                if (stack.size() > 0) {
                                    parent = (Dom) stack.get(stack.size() - 1);
                                    root2 = root3;
                                    break;
                                }
                                root2 = root3;
                                continue;
                            default:
                                root2 = root3;
                                continue;
                        }
                        e.printStackTrace();
                        if (this.val$treeCallback == null) {
                            this.val$treeCallback.onLoad(root);
                        } else if (this.val$topTreeCallback != null && this.val$treeInflateCount[0].decrementAndGet() == 0) {
                            this.val$topTreeCallback.onLoad(this.val$topRoot);
                        }
                        return;
                    }
                    is.close();
                    root2 = root3;
                    if (this.val$treeCallback == null) {
                        this.val$treeCallback.onLoad(root);
                    } else {
                        this.val$topTreeCallback.onLoad(this.val$topRoot);
                    }
                    return;
                }
                if (this.val$treeCallback != null) {
                    this.val$treeCallback.onLoad(null);
                } else if (this.val$topTreeCallback != null) {
                    if (this.val$treeInflateCount[0].decrementAndGet() == 0) {
                        this.val$topTreeCallback.onLoad(this.val$topRoot);
                    }
                }
            } catch (XmlPullParserException e3) {
                e = e3;
            }
        }
    }

    /* renamed from: ucd.ui.util.DOMLoader$2 */
    class AnonymousClass2 implements OnLoadCallback {
        private final /* synthetic */ Dom val$item;
        private final /* synthetic */ List val$topRoot;
        private final /* synthetic */ OnLoadCallback val$topTreeCallback;
        private final /* synthetic */ AtomicInteger[] val$treeInflateCount;

        AnonymousClass2(Dom dom, OnLoadCallback onLoadCallback, AtomicInteger[] atomicIntegerArr, List list) {
            this.val$item = dom;
            this.val$topTreeCallback = onLoadCallback;
            this.val$treeInflateCount = atomicIntegerArr;
            this.val$topRoot = list;
        }

        public void onLoadDom(Dom parent, Dom current) {
            if (parent == null) {
                current.parent = this.val$item;
            }
            if (this.val$topTreeCallback != null) {
                this.val$topTreeCallback.onLoadDom(current.parent, current);
            }
        }

        public void onLoad(List<Dom> subRoot) {
            if (subRoot != null) {
                synchronized (this.val$item.getChildList()) {
                    DOMLoader.insertSubRootToTree(subRoot, this.val$item.getChildList());
                }
            }
            if (this.val$treeInflateCount[0].decrementAndGet() == 0 && this.val$topTreeCallback != null) {
                this.val$topTreeCallback.onLoad(this.val$topRoot);
            }
        }
    }

    public static class Dom {
        public XMLAttributeSet attrs;
        private String content;
        private List<Dom> list = new ArrayList();
        public StringBuilder mCode = new StringBuilder();
        public int mDepth = -1;
        public int mIndexInParent = -1;
        private Dom parent;
        private String tagName;

        public static class XMLAttributeSet {
            private HashMap<String, Object> set = new HashMap();

            public Object get(String key) {
                return this.set.get(key);
            }

            public void put(String key, Object v) {
                this.set.put(key, v);
            }
        }

        public Dom(Dom parent, String tagName, XMLAttributeSet attrs) {
            this.parent = parent;
            this.tagName = tagName;
            this.attrs = attrs;
        }

        public Object getAttr(String key) {
            if (this.attrs == null || this.attrs.set == null || this.attrs.set.isEmpty()) {
                return null;
            }
            return this.attrs.get(key);
        }

        public String getTagName() {
            return this.tagName;
        }

        protected void add(Dom obj) {
            this.list.add(obj);
        }

        public int getChildrenCount() {
            return this.list.size();
        }

        public XMLAttributeSet getAttrs() {
            return this.attrs;
        }

        public void createCode(int preLayerDepth, String preLayerCode) {
            if (this.mIndexInParent >= 0 && this.mDepth >= 0) {
                this.mCode.append(preLayerCode);
                this.mDepth = preLayerDepth + 1;
                if (this.mDepth < 10) {
                    this.mCode.append("0");
                }
                this.mCode.append(this.mDepth);
                if (this.mIndexInParent < 10) {
                    this.mCode.append("0");
                }
                this.mCode.append(this.mIndexInParent);
            }
        }

        public List<Dom> getChildList() {
            return this.list;
        }
    }

    public static void read(Context context, String url, OnLoadCallback topTreeCallback) {
        if (url != null && url.length() > 0) {
            String str = "";
            Context context2 = context;
            String str2 = url;
            OnLoadCallback onLoadCallback = topTreeCallback;
            read(context2, str2, 0, str, null, onLoadCallback, new AtomicInteger[]{new AtomicInteger(0)}, new ArrayList());
        }
    }

    private static void read(Context context, String url, int preLayerDepth, String preLayerCode, OnLoadCallback treeCallback, OnLoadCallback topTreeCallback, AtomicInteger[] treeInflateCount, List<Dom> topRoot) {
        treeInflateCount[0].incrementAndGet();
        cachedThreadPool.execute(new AnonymousClass1(context, url, treeCallback, topTreeCallback, treeInflateCount, topRoot, preLayerDepth, preLayerCode));
    }

    private static void createSubDomTree(Context context, Dom item, List<Dom> topRoot, OnLoadCallback topTreeCallback, AtomicInteger[] treeInflateCount) {
        String url = (String) item.getAttr("src");
        if (url != null) {
            read(context, url, item.mDepth, item.mCode.toString(), new AnonymousClass2(item, topTreeCallback, treeInflateCount, topRoot), topTreeCallback, treeInflateCount, topRoot);
        }
    }

    @SuppressLint({"DefaultLocale"})
    private static XMLAttributeSet setAttrs(XmlPullParser parser) {
        XMLAttributeSet attrs = null;
        int count = parser.getAttributeCount();
        if (count > 0) {
            attrs = new XMLAttributeSet();
            for (int i = 0; i < count; i++) {
                Object v = parser.getAttributeValue(i);
                if (isInteger(v)) {
                    v = Integer.valueOf(Integer.parseInt((String) v));
                }
                attrs.put(parser.getAttributeName(i).toLowerCase(Locale.getDefault()), v);
            }
        }
        return attrs;
    }

    private static void insertSubRootToTree(List<Dom> subRoot, List<Dom> root) {
        if (subRoot != null && root != null) {
            int subRootCount = subRoot.size();
            int rootCount = root.size();
            int i = 0;
            while (i < rootCount && ((Dom) root.get(i)).mCode.toString().compareTo(((Dom) subRoot.get(0)).mCode.toString()) <= 0) {
                i++;
            }
            for (int j = subRootCount - 1; j >= 0; j--) {
                root.add(i, (Dom) subRoot.get(j));
            }
        }
    }

    protected static boolean isInteger(Object value) {
        try {
            Integer.parseInt((String) value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
