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

    @Test
    public void proxyThrowOkay() throws Exception {
        try {
            underTest.testThrow();
        } catch (RuntimeException expected) {
            String[] split = Proxy.class.getName().split("\\.");
            assertTrue(expected.getMessage()
                    .startsWith(Proxy.class.getName() + ".testThrow("
                                        + split[split.length - 1] + ".java:"));
        }
    }
}
