package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.AbstractBox;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyBoxParserImpl extends AbstractBoxParser {
    Properties mapping;
    Pattern p = Pattern.compile("(.*)\\((.*?)\\)");

    private class FourCcToBox {
        private String clazzName;
        private String[] param;
        private String parent;
        private String type;
        private byte[] userType;

        public FourCcToBox(String type, byte[] userType, String parent) {
            this.type = type;
            this.parent = parent;
            this.userType = userType;
        }

        public String getClazzName() {
            return this.clazzName;
        }

        public String[] getParam() {
            return this.param;
        }

        public FourCcToBox invoke() {
            String constructor;
            if (this.userType == null) {
                constructor = PropertyBoxParserImpl.this.mapping.getProperty(this.parent + "-" + this.type);
                if (constructor == null) {
                    constructor = PropertyBoxParserImpl.this.mapping.getProperty(this.type);
                }
            } else if ("uuid".equals(this.type)) {
                constructor = PropertyBoxParserImpl.this.mapping.getProperty(this.parent + "-uuid[" + Hex.encodeHex(this.userType).toUpperCase() + "]");
                if (constructor == null) {
                    constructor = PropertyBoxParserImpl.this.mapping.getProperty("uuid[" + Hex.encodeHex(this.userType).toUpperCase() + "]");
                }
                if (constructor == null) {
                    constructor = PropertyBoxParserImpl.this.mapping.getProperty("uuid");
                }
            } else {
                throw new RuntimeException("we have a userType but no uuid box type. Something's wrong");
            }
            if (constructor == null) {
                constructor = PropertyBoxParserImpl.this.mapping.getProperty("default");
            }
            if (constructor == null) {
                throw new RuntimeException("No box object found for " + this.type);
            }
            Matcher m = PropertyBoxParserImpl.this.p.matcher(constructor);
            if (m.matches()) {
                this.clazzName = m.group(1);
                this.param = m.group(2).split(",");
                return this;
            }
            throw new RuntimeException("Cannot work with that constructor: " + constructor);
        }
    }

    public PropertyBoxParserImpl(String... customProperties) {
        InputStream is = new BufferedInputStream(getClass().getResourceAsStream("/isoparser-default.properties"));
        InputStream customIS;
        try {
            this.mapping = new Properties();
            this.mapping.load(is);
            Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader().getResources("isoparser-custom.properties");
            while (enumeration.hasMoreElements()) {
                customIS = new BufferedInputStream(((URL) enumeration.nextElement()).openStream());
                this.mapping.load(customIS);
                customIS.close();
            }
            for (String customProperty : customProperties) {
                this.mapping.load(new BufferedInputStream(getClass().getResourceAsStream(customProperty)));
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        } catch (Throwable th) {
            try {
                is.close();
            } catch (IOException e22) {
                e22.printStackTrace();
            }
        }
    }

    public Box createBox(String type, byte[] userType, String parent) {
        FourCcToBox fourCcToBox = new FourCcToBox(type, userType, parent).invoke();
        String[] param = fourCcToBox.getParam();
        String clazzName = fourCcToBox.getClazzName();
        try {
            Constructor<AbstractBox> constructorObject;
            if (param[0].trim().length() == 0) {
                param = new String[0];
            }
            Class clazz = Class.forName(clazzName);
            Class[] constructorArgsClazz = new Class[param.length];
            Object[] constructorArgs = new Object[param.length];
            for (int i = 0; i < param.length; i++) {
                if ("userType".equals(param[i])) {
                    constructorArgs[i] = userType;
                    constructorArgsClazz[i] = byte[].class;
                } else if ("type".equals(param[i])) {
                    constructorArgs[i] = type;
                    constructorArgsClazz[i] = String.class;
                } else if ("parent".equals(param[i])) {
                    constructorArgs[i] = parent;
                    constructorArgsClazz[i] = String.class;
                } else {
                    throw new InternalError("No such param: " + param[i]);
                }
            }
            if (param.length > 0) {
                constructorObject = clazz.getConstructor(constructorArgsClazz);
            } else {
                constructorObject = clazz.getConstructor(new Class[0]);
            }
            return (Box) constructorObject.newInstance(constructorArgs);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2);
        } catch (InstantiationException e3) {
            throw new RuntimeException(e3);
        } catch (IllegalAccessException e4) {
            throw new RuntimeException(e4);
        } catch (ClassNotFoundException e5) {
            throw new RuntimeException(e5);
        }
    }
}
