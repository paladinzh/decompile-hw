package junit.runner;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class ClassPathTestCollector implements TestCollector {
    static final int SUFFIX_LENGTH = ".class".length();

    public Enumeration collectTests() {
        return collectFilesInPath(System.getProperty("java.class.path")).elements();
    }

    public Hashtable collectFilesInPath(String classPath) {
        return collectFilesInRoots(splitClassPath(classPath));
    }

    Hashtable collectFilesInRoots(Vector roots) {
        Hashtable result = new Hashtable(100);
        Enumeration e = roots.elements();
        while (e.hasMoreElements()) {
            gatherFiles(new File((String) e.nextElement()), "", result);
        }
        return result;
    }

    void gatherFiles(File classRoot, String classFileName, Hashtable result) {
        File thisRoot = new File(classRoot, classFileName);
        if (thisRoot.isFile()) {
            if (isTestClass(classFileName)) {
                String className = classNameFromFile(classFileName);
                result.put(className, className);
            }
            return;
        }
        String[] contents = thisRoot.list();
        if (contents != null) {
            for (String str : contents) {
                gatherFiles(classRoot, classFileName + File.separatorChar + str, result);
            }
        }
    }

    Vector splitClassPath(String classPath) {
        Vector result = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            result.addElement(tokenizer.nextToken());
        }
        return result;
    }

    protected boolean isTestClass(String classFileName) {
        if (!classFileName.endsWith(".class") || classFileName.indexOf(36) >= 0 || classFileName.indexOf("Test") <= 0) {
            return false;
        }
        return true;
    }

    protected String classNameFromFile(String classFileName) {
        String s2 = classFileName.substring(0, classFileName.length() - SUFFIX_LENGTH).replace(File.separatorChar, '.');
        if (s2.startsWith(".")) {
            return s2.substring(1);
        }
        return s2;
    }
}
