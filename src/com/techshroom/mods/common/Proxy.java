package com.techshroom.mods.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.techshroom.mods.common.proxybuilders.RegisterableObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.common.event.*;
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
     * Different states correlating to {@link ModState}.
     * 
     * @author Kenzie Togami
     */
    public static enum State {
        /**
         * Initial state
         */
        STARTUP(LoaderState.NOINIT),
        /**
         * Load state
         */
        LOAD(LoaderState.LOADING),
        /**
         * Construct state
         */
        CONSTRUCT(LoaderState.CONSTRUCTING),
        /**
         * Pre-init state
         */
        PREINIT(LoaderState.PREINITIALIZATION),
        /**
         * Initialization state
         */
        INIT(LoaderState.INITIALIZATION),
        /**
         * Post-init state
         */
        POSTINIT(LoaderState.POSTINITIALIZATION),
        /**
         * Available for use state
         */
        USEABLE(LoaderState.AVAILABLE),
        /**
         * Server about to start state
         */
        SERVER_ABOUT_TO_START(LoaderState.SERVER_ABOUT_TO_START),
        /**
         * Server start begin state
         */
        SERVER_START_BEGIN(LoaderState.SERVER_STARTING),
        /**
         * Server start end state
         */
        SERVER_START_END(LoaderState.SERVER_STARTED),
        /**
         * Server stop begin state
         */
        SERVER_STOP_BEGIN(LoaderState.SERVER_STOPPING),
        /**
         * Server stop end state
         */
        SERVER_STOP_END(LoaderState.SERVER_STOPPED),
        /**
         * Error state
         */
        ERROR(LoaderState.ERRORED);

        /**
         * LoaderState -> State mapping.
         */
        public static final ImmutableMap<LoaderState, State> loaderStateMap;
        static {
            Map<LoaderState, State> tmp = Maps.newEnumMap(LoaderState.class);
            for (State state : values()) {
                tmp.put(state.linkedState, state);
            }
            loaderStateMap = ImmutableMap.copyOf(tmp);
        }

        private static final Field LOADERSTATE_EVENTCLASS;
        static {
            Field tmp = null;
            try {
                tmp = LoaderState.class.getDeclaredField("eventClass");
                tmp.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (tmp == null) {
                    FMLCommonHandler.instance().exitJava(1, false);
                }
            }
            LOADERSTATE_EVENTCLASS = tmp;
        }
        private static final ImmutableMap<Class<FMLStateEvent>, LoaderState> eventMapping;

        @SuppressWarnings("unchecked")
        private static Class<FMLStateEvent> getStateEventClass(LoaderState ls) {
            try {
                return FMLStateEvent.class.getClass()
                        .cast(LOADERSTATE_EVENTCLASS.get(ls));
            } catch (Exception e) {
                e.printStackTrace();
                FMLCommonHandler.instance().exitJava(1, false);
                return null;
            }
        }

        static {
            Map<Class<FMLStateEvent>, LoaderState> tmp =
                    Maps.newHashMapWithExpectedSize(LoaderState.values().length);
            for (LoaderState ls : LoaderState.values()) {
                if (!ls.hasEvent()) {
                    continue;
                }
                tmp.put(getStateEventClass(ls), ls);
            }
            eventMapping = ImmutableMap.copyOf(tmp);
        }

        /**
         * Convert a LoaderState to a State.
         * 
         * @param state
         *            - the LoaderState to map from
         * @return the corresponding state
         */
        public static State from(LoaderState state) {
            return loaderStateMap.get(state);
        }

        /**
         * Convert a FMLStateEvent to a State.
         * 
         * @param stateEvent
         *            - the FMLStateEvent to map from
         * @return the corresponding state
         */
        public static State from(FMLStateEvent stateEvent) {
            return loaderStateMap.get(eventMapping.get(stateEvent.getClass()));
        }

        private final LoaderState linkedState;

        private State(LoaderState link) {
            linkedState = checkNotNull(link, "null link");
        }

        /**
         * Get the {@link LoaderState} that corresponds with this State.
         * 
         * @return the linked LoaderState
         */
        public LoaderState linkedState() {
            return linkedState;
        }

        @Override
        public String toString() {
            return name() + "(LoaderState." + linkedState.name() + ")";
        }
    }

    private static final ThreadLocal<Deque<Proxy>> activeProxies =
            new ThreadLocal<Deque<Proxy>>() {
                @Override
                protected Deque<Proxy> initialValue() {
                    return Lists.newLinkedList();
                };
            };

    /**
     * Get the first Proxy on the active stack. If there are none, then the
     * result is defined by {@link Deque#poll()}.
     * 
     * @return the result of calling {@code Deque.poll()} on the active proxy
     *         stack.
     * @see #getActiveStack()
     */
    public static Proxy getHeadOfActiveStack() {
        return activeProxies.get().peek();
    }

    /**
     * Get the active proxy stack. Different for each thread.
     * 
     * @return the stack of active proxies.
     * @see #markInUse()
     * @see #markDone()
     */
    public static Deque<Proxy> getActiveStack() {
        return activeProxies.get();
    }

    /**
     * Attach a proxy to {@link MinecraftForge#EVENT_BUS}. Called by
     * {@code Proxy.<init>}.
     * 
     * @param p
     *            - proxy
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
     * Mark this proxy as an active proxy. This allows outside objects to ask
     * for the current proxy that is calling them.
     * 
     * @see #markDone()
     */
    public void markInUse() {
        activeProxies.get().push(this);
    }

    /**
     * Mark this proxy as no longer active. If this proxy is not at the top of
     * the active stack, an exception will be thrown.
     * 
     * @throws IllegalStateException
     *             if this proxy is not at the top of the active stack
     * @see #markInUse()
     */
    public void markDone() {
        Deque<Proxy> proxies = activeProxies.get();
        if (!proxies.contains(this)) {
            throw new IllegalStateException("Not in active stack");
        }
        if (proxies.peek() != this) {
            Collection<Proxy> popBefore =
                    Lists.newArrayListWithCapacity(proxies.size());
            for (Proxy proxy : proxies) {
                if (proxy == this) {
                    break;
                }
                popBefore.add(proxy);
            }
            throw new IllegalStateException(
                    "Not at top of active stack; proxies that must be popped before us: "
                            + popBefore);
        }
        proxies.pop();
    }

    /**
     * Get the State that the proxy is currently in.
     * 
     * @return the current state of the proxy
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Get the State that this proxy last passed.
     * 
     * @return the state that was passed last. Almost always differs from
     *         {@link #getCurrentState()}.
     */
    public State getLastPassedState() {
        return lastPassedState;
    }

    /*
     * Note: this calls markInUse, which should always be a good idea.
     */
    private void enter(FMLStateEvent state) {
        markInUse();
        currentState = State.from(state);
    }

    /*
     * See above.
     */
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
        markDone();
    }

    private Logger logger = LogManager.getLogger();

    /**
     * Get proxy logger for logging things.
     * 
     * @return a logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Try to use a specific logger.
     * 
     * @param modid
     *            - the mod ID to pass
     */
    public void tryForModLog(String modid) {
        logger = LogManager.getLogger(modid);
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void postInit(FMLPostInitializationEvent postInit) {
        enter(postInit);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void preInit(FMLPreInitializationEvent preInit) {
        enter(preInit);
        logger = preInit.getModLog();
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void init(FMLInitializationEvent init) {
        enter(init);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void construct(FMLConstructionEvent construct) {
        enter(construct);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void avalible(FMLLoadCompleteEvent avalible) {
        enter(avalible);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void aboutToStart(FMLServerAboutToStartEvent aboutToStart) {
        enter(aboutToStart);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void startBegin(FMLServerStartingEvent startBegin) {
        enter(startBegin);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void startEnd(FMLServerStartedEvent startEnd) {
        enter(startEnd);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void stopBegin(FMLServerStoppingEvent stopBegin) {
        enter(stopBegin);
        runRegObjHook();
        leave();
    }

    @SuppressWarnings("javadoc")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public final void stopEnd(FMLServerStoppedEvent stopEnd) {
        enter(stopEnd);
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
        int compareTo = regObj.registerState().compareTo(lastPassedState);
        if (compareTo <= 0) {
            throw new IllegalStateException(
                    String.format("tried to register builder after its "
                                          + "register state (%s/%s >= %s/%s)",
                                  regObj.registerState(), regObj
                                          .registerState().ordinal(),
                                  lastPassedState, lastPassedState.ordinal()));
        } else if (currentState != null
                && regObj.registerState().compareTo(currentState) == 0) {
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

    /**
     * Returns {@code true} if this Proxy is on the client side.
     * 
     * @return {@code true} if we are running a client instance.
     */
    public boolean isClient() {
        return false;
    }
}
