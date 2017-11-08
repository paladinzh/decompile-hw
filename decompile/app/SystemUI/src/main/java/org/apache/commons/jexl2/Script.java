package org.apache.commons.jexl2;

public interface Script {
    Object execute(JexlContext jexlContext, Object... objArr);
}
