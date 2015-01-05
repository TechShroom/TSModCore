package com.techshroom.mods.common.test;

import static com.techshroom.mods.common.Generics.emptyArray;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import net.minecraftforge.fml.common.event.*;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.techshroom.mods.common.ClientProxy;
import com.techshroom.mods.common.Proxy;
import com.techshroom.mods.common.Proxy.State;
import com.techshroom.mods.common.java8.function.Consumer;
import com.techshroom.mods.common.proxybuilders.PhasePrinter;
import com.techshroom.mods.common.proxybuilders.RegisterableObject;

/**
 * Tests for proxy classes.
 * 
 * @author Kenzie Togami
 */
public class ProxyTests {
    private ProxyModContainer container, containerClient;
    private Proxy regular, client;

    /**
     * Set test Proxy class.
     */
    @Before
    public void setProxy() {
        System.setProperty(Proxy.AUTO_BIND_PROP_KEY, Boolean.FALSE.toString());
        regular = new Proxy();
        container = new ProxyModContainer(regular);
        PhasePrinter.addPrinter(regular, container.getModId());
        client = new ClientProxy();
        containerClient = new ProxyModContainer(client);
        PhasePrinter.addPrinter(client, containerClient.getModId());
    }

    /**
     * Checks that the {@code QUALNAME} fields are correct.
     * 
     * @throws Exception
     *             exceptions propagate
     */
    @Test
    public void refactorCheck() throws Exception {
        assertEquals(Proxy.class.getName(), Proxy.QUALNAME);
        assertEquals(ClientProxy.class.getName(), ClientProxy.QUALNAME);
    }

    /**
     * Register the proxies on event busses.
     * 
     * @throws Exception
     *             exceptions propagate
     */
    @Test
    public void tryEventBusRegister() throws Exception {
        EventBus bus = new EventBus("proxy");
        bus.register(regular);
        bus.register(client);
    }

    private final boolean[] flagbase = new boolean[3];
    private final int CREATE = 0, REG = 1, REGCLIENT = 2;

    /**
     * Proper firing on registerable objects by proxies.
     * 
     * @throws Exception
     *             exceptions propagate
     */
    @Test
    public void correctRegister() throws Exception {
        forEachState(new Consumer<List<Object>>() {
            @Override
            public void accept(List<Object> input) {
                State state = (State) input.get(0);
                State[] pre = (State[]) input.get(1);
                correctRegProcedure(state, pre);
            }
        });
    }

    /**
     * Proper firing on registerable objects by proxies for edge case.
     * 
     * @throws Exception
     *             exceptions propagate
     */
    @Test
    public void edgeRegister() throws Exception {
        forEachState(new Consumer<List<Object>>() {
            @Override
            public void accept(List<Object> input) {
                State state = (State) input.get(0);
                State[] pre = (State[]) input.get(1);
                edgeRegProcedure(state, pre);
            }
        });
    }

    /**
     * Proper firing on registerable objects by proxies for incorrect case.
     * 
     * @throws Exception
     *             exceptions propagate
     */
    @Test
    public void badRegister() throws Exception {
        forEachState(new Consumer<List<Object>>() {
            @Override
            public void accept(List<Object> input) {
                State state = (State) input.get(0);
                State[] pre = (State[]) input.get(1);
                incorrectRegProcedure(state, pre);
            }
        });
    }

    private void forEachState(Consumer<List<Object>> doThis) {
        List<State> pre = Lists.newArrayListWithCapacity(State.values().length);
        for (State state : State.values()) {
            if (state != State.STARTUP) {
                State[] prevals = pre.toArray(emptyArray(State.class));
                doThis.accept(ImmutableList.<Object> of(state, prevals));
            }
            pre.add(state);
        }
    }

    private void correctRegProcedure(State state, State[] preStates) {
        setProxy();
        doCorrect(state, preStates, regular);
        doCorrect(state, preStates, client);
    }

    private void doCorrect(State state, State[] preStates, Proxy proxy) {
        boolean isClient = proxy.isClient();
        boolean[] flags = flagbase.clone();
        proxy.registerRegisterableObject(regObjTester(state, flags));
        checkNotRegistered(flags, state, isClient);
        for (State s : preStates) {
            if (!fireCorrespondingEvent(proxy, s)) {
                continue;
            }
            checkNotRegistered(flags, s, isClient);
        }
        if (!fireCorrespondingEvent(proxy, state)) {
            // no event -> doesn't matter
            return;
        }
        checkRegistered(flags, state, isClient);
    }

    private void edgeRegProcedure(State state, State[] preStates) {
        setProxy();
        doEdge(state, preStates, regular);
        doEdge(state, preStates, client);
    }

    private void doEdge(final State state, State[] preStates, Proxy proxy) {
        boolean isClient = proxy.isClient();
        final boolean[] flags = flagbase.clone();
        Runnable register = new Runnable() {
            @Override
            public void run() {
                Proxy.getHeadOfActiveStack()
                        .registerRegisterableObject(regObjTester(state, flags));
            }
        };
        proxy.registerRegisterableObject(regObjTester(state, flagbase.clone(),
                                                      null, register, null));
        checkNotRegistered(flags, state, isClient);
        for (State s : preStates) {
            if (!fireCorrespondingEvent(proxy, s)) {
                continue;
            }
            checkNotRegistered(flags, s, isClient);
        }
        if (!fireCorrespondingEvent(proxy, state)) {
            // no event -> doesn't matter
            return;
        }
        checkRegistered(flags, state, isClient);
    }

    private void incorrectRegProcedure(State state, State[] preStates) {
        setProxy();
        doIncorrect(state, preStates, regular);
        doIncorrect(state, preStates, client);
    }

    private void doIncorrect(State state, State[] preStates, Proxy proxy) {
        boolean isClient = proxy.isClient();
        boolean[] flags = flagbase.clone();
        checkNotRegistered(flags, state, isClient);
        for (State s : preStates) {
            if (!fireCorrespondingEvent(proxy, s)) {
                continue;
            }
            checkNotRegistered(flags, s, isClient);
        }
        if (!fireCorrespondingEvent(proxy, state)) {
            // no event -> doesn't matter
            return;
        }
        try {
            proxy.registerRegisterableObject(regObjTester(state, flags));
            fail("Should not have proceeded, expected exception (state="
                    + state + ",flags=" + Arrays.toString(flags));
        } catch (Exception e) {
            // System.err.println("caught exception of " + e.getClass());
        }
        checkNotRegistered(flags, state, isClient);
    }

    private void checkNotRegistered(boolean[] rf, State state, boolean client) {
        assertFalse("registered before state " + state, rf[REG]);
        if (client) {
            assertFalse("registered before state " + state, rf[REGCLIENT]);
        }
    }

    private void checkRegistered(boolean[] rf, State state, boolean client) {
        assertTrue("not registered after state " + state, rf[REG]);
        if (client) {
            assertTrue("not registered after state " + state, rf[REGCLIENT]);
        }
    }

    private boolean fireCorrespondingEvent(Proxy proxy, State state) {
        switch (state) {
            case STARTUP:
            case LOAD:
            case ERROR:
                // nothing
                return false;
            case CONSTRUCT:
                proxy.construct(new FMLConstructionEvent(null, null, null));
                break;
            case PREINIT:
                FMLPreInitializationEvent preInit =
                        new FMLPreInitializationEvent(null, null);
                preInit.applyModContainer(new ProxyModContainer(proxy));
                proxy.preInit(preInit);
                break;
            case INIT:
                proxy.init(new FMLInitializationEvent());
                break;
            case POSTINIT:
                proxy.postInit(new FMLPostInitializationEvent());
                break;
            case USEABLE:
                proxy.avalible(new FMLLoadCompleteEvent());
                break;
            case SERVER_ABOUT_TO_START:
                proxy.aboutToStart(new FMLServerAboutToStartEvent((Object) null));
                break;
            case SERVER_START_BEGIN:
                proxy.startBegin(new FMLServerStartingEvent((Object) null));
                break;
            case SERVER_START_END:
                proxy.startEnd(new FMLServerStartedEvent());
                break;
            case SERVER_STOP_BEGIN:
                proxy.stopBegin(new FMLServerStoppingEvent());
                break;
            case SERVER_STOP_END:
                proxy.stopEnd(new FMLServerStoppedEvent());
                break;
            default:
                throw new IllegalStateException("state not implemented: "
                        + state);
        }
        return true;
    }

    private RegisterableObject<Void> regObjTester(final State state,
            final boolean[] flags) {
        return regObjTester(state, flags, null, null, null);
    }

    private RegisterableObject<Void> regObjTester(final State state,
            final boolean[] flags, final Runnable create,
            final Runnable register, final Runnable rClient) {
        return new RegisterableObject<Void>() {
            @Override
            public State registerState() {
                return state;
            }

            @Override
            public Void create() throws Throwable {
                if (create != null)
                    create.run();
                flags[CREATE] = true;
                return null;
            }

            @Override
            public void register() {
                if (register != null)
                    register.run();
                flags[REG] = true;
            }

            @Override
            public void registerClient() {
                if (rClient != null)
                    rClient.run();
                flags[REGCLIENT] = true;
            }
        };
    }
}
