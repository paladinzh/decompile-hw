package com.huawei.mms.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class VCalParser {
    private static int mIndex = 0;

    public static class Component {
        private LinkedList<Component> mChildren = null;
        private final String mName;
        private final Component mParent;
        private final LinkedHashMap<String, ArrayList<Property>> mPropsMap = new LinkedHashMap();

        public Component(String name, Component parent) {
            this.mName = name;
            this.mParent = parent;
        }

        public String getName() {
            return this.mName;
        }

        public Component getParent() {
            return this.mParent;
        }

        protected LinkedList<Component> getOrCreateChildren() {
            if (this.mChildren == null) {
                this.mChildren = new LinkedList();
            }
            return this.mChildren;
        }

        public void addChild(Component child) {
            getOrCreateChildren().add(child);
        }

        public List<Component> getComponents() {
            return this.mChildren;
        }

        public void addProperty(Property prop) {
            String name = prop.getName();
            ArrayList<Property> props = (ArrayList) this.mPropsMap.get(name);
            if (props == null) {
                props = new ArrayList();
                this.mPropsMap.put(name, props);
            }
            props.add(prop);
        }

        public Set<String> getPropertyNames() {
            return this.mPropsMap.keySet();
        }

        public List<Property> getProperties(String name) {
            return (List) this.mPropsMap.get(name);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            sb.append("\n");
            return sb.toString();
        }

        public void toString(StringBuilder sb) {
            sb.append("BEGIN");
            sb.append(":");
            sb.append(this.mName);
            sb.append("\n");
            for (String propertyName : getPropertyNames()) {
                for (Property property : getProperties(propertyName)) {
                    property.toString(sb);
                    sb.append("\n");
                }
            }
            if (this.mChildren != null) {
                for (Component component : this.mChildren) {
                    component.toString(sb);
                    sb.append("\n");
                }
            }
            sb.append("END");
            sb.append(":");
            sb.append(this.mName);
        }
    }

    public static class FormatException extends Exception {
        public FormatException(String msg) {
            super(msg);
        }
    }

    public static class Parameter {
        public String name;
        public String value;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }

        public void toString(StringBuilder sb) {
            sb.append(this.name);
            sb.append("=");
            sb.append(this.value);
        }
    }

    private static final class ParserState {
        public int index;
        public String line;

        private ParserState() {
        }
    }

    public static class Property {
        private final String mName;
        private LinkedHashMap<String, ArrayList<Parameter>> mParamsMap = new LinkedHashMap();
        private String mValue;

        public Property(String name) {
            this.mName = name;
        }

        public String getName() {
            return this.mName;
        }

        public String getValue() {
            return this.mValue;
        }

        public void setValue(String value) {
            this.mValue = value;
        }

        public void addParameter(Parameter param) {
            ArrayList<Parameter> params = (ArrayList) this.mParamsMap.get(param.name);
            if (params == null) {
                params = new ArrayList();
                this.mParamsMap.put(param.name, params);
            }
            params.add(param);
        }

        public Set<String> getParameterNames() {
            return this.mParamsMap.keySet();
        }

        public List<Parameter> getParameters(String name) {
            return (List) this.mParamsMap.get(name);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }

        public void toString(StringBuilder sb) {
            sb.append(this.mName);
            for (String parameterName : getParameterNames()) {
                for (Parameter param : getParameters(parameterName)) {
                    sb.append(";");
                    param.toString(sb);
                }
            }
            sb.append(":");
            sb.append(this.mValue);
        }
    }

    private VCalParser() {
    }

    private static String normalizeText(String text) {
        return text.replaceAll("\r\n", "\n").replaceAll("\r", "\n").replaceAll("= ", "=  ").replaceAll("=\t", "=\t\t").replaceAll("=\n ", "= \n ").replaceAll("=\n\t", "=\t\n\t").replaceAll("\n ", "").replaceAll("\n\t", "");
    }

    public static Component parseComponentImpl(Component component, String text) throws FormatException {
        Component current = component;
        ParserState state = new ParserState();
        state.index = 0;
        String[] lines = text.split("\n");
        mIndex = 0;
        while (mIndex < lines.length) {
            try {
                current = parseLine(lines[mIndex], state, current, lines);
                if (component == null) {
                    component = current;
                }
            } catch (FormatException e) {
                mIndex++;
            }
        }
        return component;
    }

    private static Component parseLine(String line, ParserState state, Component component, String[] lines) throws FormatException {
        state.line = line;
        int len = state.line.length();
        char c = '\u0000';
        state.index = 0;
        while (state.index < len) {
            c = line.charAt(state.index);
            if (c == ';' || c == ':') {
                break;
            }
            state.index++;
        }
        String name = line.substring(0, state.index);
        if (component == null && !"BEGIN".equals(name)) {
            throw new FormatException("Expected BEGIN");
        } else if ("BEGIN".equals(name)) {
            Component child = new Component(extractValue(state), component);
            if (component != null) {
                component.addChild(child);
            }
            mIndex++;
            return child;
        } else if ("END".equals(name)) {
            String componentName = extractValue(state);
            if (component == null || !componentName.equals(component.getName())) {
                throw new FormatException("Unexpected END " + componentName);
            }
            mIndex++;
            return component.getParent();
        } else {
            Property property = new Property(name);
            if (c == ';') {
                while (true) {
                    Parameter parameter = extractParameter(state);
                    if (parameter == null) {
                        break;
                    }
                    property.addParameter(parameter);
                }
            }
            String value = extractValue(state);
            mIndex++;
            if (isQOPEncoded(property)) {
                StringBuilder valueBuilder = new StringBuilder();
                parseEqualsSpaceSequence(valueBuilder, value);
                if (!isEndOfValue(value)) {
                    extractQOPValue(lines, valueBuilder);
                }
                value = valueBuilder.toString().replaceAll("=0D=0A", "=0A");
            } else {
                value = value.replaceAll("= ", "\r\n").replaceAll("=\t", "\r\n").replace("\r\n", "=");
            }
            property.setValue(value);
            if (component != null) {
                component.addProperty(property);
            }
            return component;
        }
    }

    private static String extractValue(ParserState state) throws FormatException {
        String line = state.line;
        if (state.index >= line.length() || line.charAt(state.index) != ':') {
            throw new FormatException("Expected ':' before end of line in " + line);
        }
        String value = line.substring(state.index + 1);
        state.index = line.length() - 1;
        return value;
    }

    private static Parameter extractParameter(ParserState state) throws FormatException {
        String text = state.line;
        int len = text.length();
        Parameter parameter = null;
        int startIndex = -1;
        int equalIndex = -1;
        while (state.index < len) {
            char c = text.charAt(state.index);
            if (c == ':') {
                if (parameter != null) {
                    if (equalIndex == -1) {
                        throw new FormatException("Expected '=' within parameter in " + text);
                    }
                    parameter.value = text.substring(equalIndex + 1, state.index);
                }
                return parameter;
            }
            if (c == ';') {
                if (parameter == null) {
                    parameter = new Parameter();
                    startIndex = state.index;
                } else if (equalIndex == -1) {
                    throw new FormatException("Expected '=' within parameter in " + text);
                } else {
                    parameter.value = text.substring(equalIndex + 1, state.index);
                    return parameter;
                }
            } else if (c == '=') {
                equalIndex = state.index;
                if (parameter == null || startIndex == -1) {
                    throw new FormatException("Expected ';' before '=' in " + text);
                }
                parameter.name = text.substring(startIndex + 1, equalIndex);
            } else if (c == '\"') {
                if (parameter == null) {
                    throw new FormatException("Expected parameter before '\"' in " + text);
                } else if (equalIndex == -1) {
                    throw new FormatException("Expected '=' within parameter in " + text);
                } else if (state.index > equalIndex + 1) {
                    throw new FormatException("Parameter value cannot contain a '\"' in " + text);
                } else {
                    int endQuote = text.indexOf(34, state.index + 1);
                    if (endQuote < 0) {
                        throw new FormatException("Expected closing '\"' in " + text);
                    }
                    parameter.value = text.substring(state.index + 1, endQuote);
                    state.index = endQuote + 1;
                    return parameter;
                }
            }
            state.index++;
        }
        throw new FormatException("Expected ':' before end of line in " + text);
    }

    public static Component parseCalendar(String text) throws FormatException {
        Component calendar = parseComponent(null, text);
        if (calendar != null && "VCALENDAR".equals(calendar.getName())) {
            return calendar;
        }
        throw new FormatException("Expected VCALENDAR");
    }

    public static Component parseComponent(Component component, String text) throws FormatException {
        return parseComponentImpl(component, normalizeText(text));
    }

    public static boolean isEndOfValue(String value) {
        if (value.endsWith("=")) {
            return false;
        }
        return true;
    }

    public static boolean isQOPEncoded(Property property) {
        List<Parameter> encoding = property.getParameters("ENCODING");
        if (encoding == null || encoding.size() <= 0 || !"QUOTED-PRINTABLE".equalsIgnoreCase(((Parameter) encoding.get(0)).value)) {
            return false;
        }
        return true;
    }

    public static void extractQOPValue(String[] lines, StringBuilder valueBuilder) {
        while (mIndex < lines.length) {
            String value = lines[mIndex];
            parseEqualsSpaceSequence(valueBuilder, value);
            mIndex++;
            if (isEndOfValue(value)) {
                return;
            }
        }
    }

    private static void parseEqualsSpaceSequence(StringBuilder valueBuilder, String value) {
        for (String str : value.replaceAll("= ", "=\n ").replaceAll("=\t", "=\n\t").split("\n")) {
            if (isEndOfValue(str)) {
                valueBuilder.append(str);
            } else {
                valueBuilder.append(str).append("\r\n");
            }
        }
    }
}
