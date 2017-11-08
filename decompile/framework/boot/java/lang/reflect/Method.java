package java.lang.reflect;

import com.android.dex.Dex;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import libcore.reflect.Types;

public final class Method extends AbstractMethod implements GenericDeclaration, Member {
    public static final Comparator<Method> ORDER_BY_SIGNATURE = new Comparator<Method>() {
        public int compare(Method a, Method b) {
            if (a == b) {
                return 0;
            }
            int comparison = a.getName().compareTo(b.getName());
            if (comparison == 0) {
                comparison = a.compareParameters(b.getParameterTypes());
                if (comparison == 0) {
                    Class<?> aReturnType = a.getReturnType();
                    Class<?> bReturnType = b.getReturnType();
                    if (aReturnType == bReturnType) {
                        comparison = 0;
                    } else {
                        comparison = aReturnType.getName().compareTo(bReturnType.getName());
                    }
                }
            }
            return comparison;
        }
    };

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native Annotation[][] getParameterAnnotationsNative();

    public native Object getDefaultValue();

    public native Class<?>[] getExceptionTypes();

    public native Object invoke(Object obj, Object... objArr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    private Method() {
    }

    public Class<?> getDeclaringClass() {
        return super.getDeclaringClass();
    }

    public String getName() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheString(dex, dex.nameIndexFromMethodIndex(this.dexMethodIndex));
    }

    public int getModifiers() {
        return super.getModifiers();
    }

    public TypeVariable<Method>[] getTypeParameters() {
        return (TypeVariable[]) getMethodOrConstructorGenericInfo().formalTypeParameters.clone();
    }

    public Class<?> getReturnType() {
        Dex dex = this.declaringClassOfOverriddenMethod.getDex();
        return this.declaringClassOfOverriddenMethod.getDexCacheType(dex, dex.returnTypeIndexFromMethodIndex(this.dexMethodIndex));
    }

    public Type getGenericReturnType() {
        return Types.getType(getMethodOrConstructorGenericInfo().genericReturnType);
    }

    public Class<?>[] getParameterTypes() {
        return super.getParameterTypes();
    }

    public Type[] getGenericParameterTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericParameterTypes, false);
    }

    public Type[] getGenericExceptionTypes() {
        return Types.getTypeArray(getMethodOrConstructorGenericInfo().genericExceptionTypes, false);
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Method)) {
            Method other = (Method) obj;
            if (getDeclaringClass() != other.getDeclaringClass() || getName() != other.getName() || !getReturnType().equals(other.getReturnType())) {
                return false;
            }
            Class<?>[] params1 = getParameterTypes();
            Class<?>[] params2 = other.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            sb.append(Field.getTypeName(getReturnType())).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Class<?>[] params = getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < params.length - 1) {
                    sb.append(',');
                }
            }
            sb.append(')');
            Class<?>[] exceptions = getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < exceptions.length - 1) {
                        sb.append(',');
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first) {
                        sb.append(',');
                    }
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            Type genRetType = getGenericReturnType();
            sb.append(genRetType instanceof Class ? Field.getTypeName((Class) genRetType) : genRetType.toString()).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Type[] params = getGenericParameterTypes();
            int j = 0;
            while (j < params.length) {
                String param;
                if (params[j] instanceof Class) {
                    param = Field.getTypeName((Class) params[j]);
                } else {
                    param = params[j].toString();
                }
                if (isVarArgs() && j == params.length - 1) {
                    param = param.replaceFirst("\\[\\]$", "...");
                }
                sb.append(param);
                if (j < params.length - 1) {
                    sb.append(',');
                }
                j++;
            }
            sb.append(')');
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    String name;
                    if (exceptions[k] instanceof Class) {
                        name = ((Class) exceptions[k]).getName();
                    } else {
                        name = exceptions[k].toString();
                    }
                    sb.append(name);
                    if (k < exceptions.length - 1) {
                        sb.append(',');
                    }
                }
            }
            return sb.toString();
        } catch (Object e) {
            return "<" + e + ">";
        }
    }

    public boolean isBridge() {
        return (getModifiers() & 64) != 0;
    }

    public boolean isVarArgs() {
        return (getModifiers() & 128) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (annotationType != null) {
            return getAnnotationNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
        if (parameterAnnotations != null) {
            return parameterAnnotations;
        }
        return (Annotation[][]) Array.newInstance(Annotation.class, getParameterTypes().length, 0);
    }

    String getSignature() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (Class<?> parameterType : getParameterTypes()) {
            result.append(Types.getSignature(parameterType));
        }
        result.append(')');
        result.append(Types.getSignature(getReturnType()));
        return result.toString();
    }

    boolean equalNameAndParameters(Method m) {
        return getName().equals(m.getName()) ? equalMethodParameters(m.getParameterTypes()) : false;
    }

    public boolean isDefault() {
        return super.isDefault();
    }
}
