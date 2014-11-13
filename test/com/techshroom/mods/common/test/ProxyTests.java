package com.techshroom.mods.common.test;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.techshroom.mods.common.Proxy;

public class ProxyTests {
    private static Proxy underTest;

    @BeforeClass
    public static void setProxy() {
        underTest = new Proxy() {
        };
    }
}
