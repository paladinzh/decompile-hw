package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectDescriptorFactory {
    protected static Map<Integer, Map<Integer, Class<? extends BaseDescriptor>>> descriptorRegistry = new HashMap();
    protected static Logger log = Logger.getLogger(ObjectDescriptorFactory.class.getName());

    static {
        Set<Class<? extends BaseDescriptor>> annotated = new HashSet();
        annotated.add(DecoderSpecificInfo.class);
        annotated.add(SLConfigDescriptor.class);
        annotated.add(BaseDescriptor.class);
        annotated.add(ExtensionDescriptor.class);
        annotated.add(ObjectDescriptorBase.class);
        annotated.add(ProfileLevelIndicationDescriptor.class);
        annotated.add(AudioSpecificConfig.class);
        annotated.add(ExtensionProfileLevelDescriptor.class);
        annotated.add(ESDescriptor.class);
        annotated.add(DecoderConfigDescriptor.class);
        for (Class<? extends BaseDescriptor> clazz : annotated) {
            Descriptor descriptor = (Descriptor) clazz.getAnnotation(Descriptor.class);
            int[] tags = descriptor.tags();
            int objectTypeInd = descriptor.objectTypeIndication();
            Map<Integer, Class<? extends BaseDescriptor>> tagMap = (Map) descriptorRegistry.get(Integer.valueOf(objectTypeInd));
            if (tagMap == null) {
                tagMap = new HashMap();
            }
            for (int tag : tags) {
                tagMap.put(Integer.valueOf(tag), clazz);
            }
            descriptorRegistry.put(Integer.valueOf(objectTypeInd), tagMap);
        }
    }

    public static BaseDescriptor createFrom(int objectTypeIndication, ByteBuffer bb) throws IOException {
        BaseDescriptor baseDescriptor;
        int tag = IsoTypeReader.readUInt8(bb);
        Map<Integer, Class<? extends BaseDescriptor>> tagMap = (Map) descriptorRegistry.get(Integer.valueOf(objectTypeIndication));
        if (tagMap == null) {
            tagMap = (Map) descriptorRegistry.get(Integer.valueOf(-1));
        }
        Class<? extends BaseDescriptor> aClass = (Class) tagMap.get(Integer.valueOf(tag));
        if (aClass == null || aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) {
            log.warning("No ObjectDescriptor found for objectTypeIndication " + Integer.toHexString(objectTypeIndication) + " and tag " + Integer.toHexString(tag) + " found: " + aClass);
            baseDescriptor = new UnknownDescriptor();
        } else {
            try {
                baseDescriptor = (BaseDescriptor) aClass.newInstance();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Couldn't instantiate BaseDescriptor class " + aClass + " for objectTypeIndication " + objectTypeIndication + " and tag " + tag, e);
                throw new RuntimeException(e);
            }
        }
        baseDescriptor.parse(tag, bb);
        return baseDescriptor;
    }
}
