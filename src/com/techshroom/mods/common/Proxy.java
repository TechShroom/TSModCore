package com.techshroom.mods.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.techshroom.mods.common.proxybuilders.RegisterableObject;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderState.ModState;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
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
        STARTUP(ModState.UNLOADED),
        /**
         * Load state
         */
        LOAD(ModState.LOADED),
        /**
         * Construct state
         */
        CONSTRUCT(ModState.CONSTRUCTED),
        /**
         * Pre-init state
         */
        PREINIT(ModState.PREINITIALIZED),
        /**
         * Initialization state
         */
        INIT(ModState.INITIALIZED),
        /**
         * Post-init state
         */
        POSTINIT(ModState.POSTINITIALIZED),
        /**
         * Available for use state
         */
        USEABLE(ModState.AVAILABLE),
        /**
         * Disable state
         */
        DISABLE(ModState.DISABLED),
        /**
         * Error state
         */
        ERROR(ModState.ERRORED);

        /**
         * ModState -> State mapping.
         */
        public static final ImmutableMap<ModState, State> modStateToStateMap;
        static {
            Map<ModState, State> tmp = Maps.newEnumMap(ModState.class);
            for (State state : values()) {
                tmp.put(state.linkedState, state);
            }
            modStateToStateMap = ImmutableMap.copyOf(tmp);
        }

        /**
         * Convert a ModState to a State.
         * 
         * @param state
         *            - the ModState to map from
         * @return the corresponding state
         */
        public static State from(ModState state) {
            return modStateToStateMap.get(state);
        }

        private final ModState linkedState;

        private State(ModState link) {
            linkedState = checkNotNull(link, "null link");
        }

        /**
         * Get the {@link ModState} that corresponds with this State.
         * 
         * @return the linked ModState
         */
        public ModState linkedState() {
            return linkedState;
        }

        @Override
        public String toString() {
            return name() + "(ModState." + linkedState.name() + ")";
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
        currentState = State.from(state.getModState());
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
