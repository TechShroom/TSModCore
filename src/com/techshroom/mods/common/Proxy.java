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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Core proxy class for mods to extend.
 * 
 * @author Kenzie Togami
 *
 */
public abstract class Proxy {
    @SideOnly(Side.CLIENT)
    public static abstract class Client
            extends Proxy {
        @Override
        protected void regObjHook(RegisterableObject<?> regObj)
                throws Throwable {
            super.regObjHook(regObj);
            regObj.registerClient();
        }
    }

    public static enum State {
        STARTUP, PREINIT, INIT, POSTINIT;
    }

    /**
     * Attach a proxy to {@link MinecraftForge#EVENT_BUS}.
     * 
     * @param p
     */
    public static void attachProxy(Proxy p) {
        MinecraftForge.EVENT_BUS.register(p);
    }

    private final Multimap<State, RegisterableObject<?>> builders =
            HashMultimap.create();
    private final Set<RegisterableObject<?>> duringStateBuilders = Sets
            .newHashSet();
    private State currentState = State.STARTUP;
    private State lastPassedState = State.STARTUP;

    public State getCurrentState() {
        return currentState;
    }

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void postInit(FMLPostInitializationEvent postInit) {
        enter(State.POSTINIT);
        runRegObjHook();
        leave();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void preInit(FMLPreInitializationEvent preInit) {
        enter(State.PREINIT);
        runRegObjHook();
        leave();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void init(FMLInitializationEvent init) {
        enter(State.INIT);
        runRegObjHook();
        leave();
    }

    public final void registerRegisterableObject(RegisterableObject<?> regObj) {
        int compareTo = regObj.registerState().compareTo(currentState);
        if (compareTo > 0) {
            throw new IllegalStateException(
                    "tried to register builder after its register state");
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
