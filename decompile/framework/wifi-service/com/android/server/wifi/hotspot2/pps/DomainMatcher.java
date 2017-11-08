package com.android.server.wifi.hotspot2.pps;

import com.android.server.wifi.hotspot2.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DomainMatcher {
    private static final String[] TestDomains = new String[]{"garbage.apple.com", "apple.com", "com", "jan.android.google.com.", "jan.android.google.com", "android.google.com", "google.com", "jan.android.google.net.", "jan.android.google.net", "android.google.net", "google.net", "net.", "."};
    private final Label mRoot = new Label(Match.None);

    private static class Label {
        private final Match mMatch;
        private final Map<String, Label> mSubDomains;

        private Label(Match match) {
            this.mMatch = match;
            this.mSubDomains = match == Match.None ? new HashMap() : null;
        }

        private void addDomain(Iterator<String> labels, Match match) {
            String labelName = (String) labels.next();
            if (labels.hasNext()) {
                Label subLabel = new Label(Match.None);
                this.mSubDomains.put(labelName, subLabel);
                subLabel.addDomain(labels, match);
                return;
            }
            this.mSubDomains.put(labelName, new Label(match));
        }

        private Label getSubLabel(String labelString) {
            return (Label) this.mSubDomains.get(labelString);
        }

        public Match getMatch() {
            return this.mMatch;
        }

        private void toString(StringBuilder sb) {
            if (this.mSubDomains != null) {
                sb.append(".{");
                for (Entry<String, Label> entry : this.mSubDomains.entrySet()) {
                    sb.append((String) entry.getKey());
                    ((Label) entry.getValue()).toString(sb);
                }
                sb.append('}');
                return;
            }
            sb.append('=').append(this.mMatch);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
    }

    public enum Match {
        None,
        Primary,
        Secondary
    }

    public DomainMatcher(List<String> primary, List<List<String>> secondary) {
        for (List<String> secondaryLabel : secondary) {
            this.mRoot.addDomain(secondaryLabel.iterator(), Match.Secondary);
        }
        this.mRoot.addDomain(primary.iterator(), Match.Primary);
    }

    public Match isSubDomain(List<String> domain) {
        Label label = this.mRoot;
        for (String labelString : domain) {
            label = label.getSubLabel(labelString);
            if (label == null) {
                return Match.None;
            }
            if (label.getMatch() != Match.None) {
                return label.getMatch();
            }
        }
        return Match.None;
    }

    public static boolean arg2SubdomainOfArg1(List<String> arg1, List<String> arg2) {
        if (arg2.size() < arg1.size()) {
            return false;
        }
        Iterator<String> l2 = arg2.iterator();
        for (String equals : arg1) {
            if (!equals.equals(l2.next())) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "Domain matcher " + this.mRoot;
    }

    public static void main(String[] args) {
        int i = 0;
        DomainMatcher dm1 = new DomainMatcher(Utils.splitDomain("android.google.com"), Collections.emptyList());
        for (String domain : TestDomains) {
            String domain2;
            System.out.println(domain2 + ": " + dm1.isSubDomain(Utils.splitDomain(domain2)));
        }
        List<List<String>> secondaries = new ArrayList();
        secondaries.add(Utils.splitDomain("apple.com"));
        secondaries.add(Utils.splitDomain("net"));
        DomainMatcher dm2 = new DomainMatcher(Utils.splitDomain("android.google.com"), secondaries);
        String[] strArr = TestDomains;
        int length = strArr.length;
        while (i < length) {
            domain2 = strArr[i];
            System.out.println(domain2 + ": " + dm2.isSubDomain(Utils.splitDomain(domain2)));
            i++;
        }
    }
}
