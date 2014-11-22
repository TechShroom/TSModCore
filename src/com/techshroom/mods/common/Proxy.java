package com.techshroom.mods.common;

import java.util.Set;

import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.techshroom.mods.common.proxybuilders.RegisterableObject;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Core proxy class for mods to extend.
 * 
 * @author Kenzie Togami
 *
 */
public class Proxy {
    /**
     * Qualified name of this class.
     */
    public static final String QUALNAME = "com.techshroom.mods.common.Proxy";

    /**
     * This property key can be set to {@code false} in the system properties to
     * make the proxy not automatically attach itself to the
     * {@link MinecraftForge#EVENT_BUS event bus}.
     */
    public static final String AUTO_BIND_PROP_KEY = QUALNAME + ".attach";

    /**
     * Different states correlating to the current proxy state.
     * 
     * @author Kenzie Togami
     */
    public static enum State {
        /**
         * Initial state
         */
        STARTUP,
        /**
         * Pre-init state
         */
        PREINIT,
        /**
         * Initialization state
         */
        INIT,
        /**
         * Post-init state
         */
        POSTINIT;
    }

    /**
     * Attach a proxy to {@link MinecraftForge#EVENT_BUS}. Called by
     * {@code Proxy.<init>}.
     * 
     * @param p
     */
    private final static void attachProxy(Proxy p) {
        MinecraftForge.EVENT_BUS.register(p);
    }

    {
        boolean attach =
                Boolean.parseBoolean(System.getProperty(AUTO_BIND_PROP_KEY,
                                                        "true"));
        if (attach) {
            attachProxy(this);
        }
    }

    private final Multimap<State, RegisterableObject<?>> builders =
            HashMultimap.create();
    private final Set<RegisterableObject<?>> duringStateBuilders = Sets
            .newHashSet();
    private State currentState = State.STARTUP;
    private State lastPassedState = State.STARTUP;

    /**
     * @return the current state of the proxy
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * @return the state that was passed last. Almost always differs from
     *         {@link #getCurrentState()}.
     */
    public State getLastPassedState() {
        return lastPassedState;
    }

    private void enter(State state) {
        currentState = state;
    }

    private void leave() {
        if (currentState == null) {
            throw new IllegalStateException(
                    "cannot call leave() without calling enter()");
        }
        // now possible to run extra builders registered by other builders
        for (RegisterableObject<?> regObj : duringStateBuilders) {
            exceptionCatchingRegObjHook(regObj);
        }
        duringStateBuilders.clear();
        lastPassedState = currentState;
        currentState = null;
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void postInit(FMLPostInitializationEvent postInit) {
        enter(State.POSTINIT);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void preInit(FMLPreInitializationEvent preInit) {
        enter(State.PREINIT);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void init(FMLInitializationEvent init) {
        enter(State.INIT);
        runRegObjHook();
        leave();
    }

    /**
     * Add an object to register later.
     * 
     * @param regObj
     *            - a registerable object to register later
     */
    public final void registerRegisterableObject(RegisterableObject<?> regObj) {
        int compareTo = regObj.registerState().compareTo(currentState);
        if (compareTo < 0) {
            throw new IllegalStateException(
                    String.format("tried to register builder after its "
                                          + "register state (%s/%s > %s/%s)",
                                  regObj.registerState(), regObj
                                          .registerState().ordinal(),
                                  currentState, currentState.ordinal()));
        } else if (compareTo == 0) {
            duringStateBuilders.add(regObj);
            return;
        }
        builders.put(regObj.registerState(), regObj);
    }

    private void runRegObjHook() {
        for (RegisterableObject<?> regObj : builders.get(currentState)) {
            exceptionCatchingRegObjHook(regObj);
        }
    }

    private void exceptionCatchingRegObjHook(RegisterableObject<?> regObj) {
        try {
            regObjHook(regObj);
        } catch (Throwable e) {
            FMLLog.getLogger().error("RegisterableObject<?> " + regObj
                                             + " failed register hook", e);
        }
    }

    protected void regObjHook(RegisterableObject<?> rbBuilder) throws Throwable {
        rbBuilder.create();
        rbBuilder.register();
    }
}
