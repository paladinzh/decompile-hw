package com.google.common.hash;

abstract class AbstractCompositeHashFunction implements HashFunction {
    final HashFunction[] functions;
}
