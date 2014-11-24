package com.techshroom.mods.common.proxybuilders;

import com.techshroom.mods.common.Proxy;
import com.techshroom.mods.common.Proxy.State;

/**
 * Prints every state phase.
 * 
 * @author Kenzie Togami
 */
public final class PhasePrinter {
    /**
     * Attach a new PhasePrinter to the proxy.
     * 
     * @param proxy - proxy to attach to
     * @param modid - mod ID to use
     * @return the attached phase printer
     */
    public static PhasePrinter addPrinter(Proxy proxy, String modid) {
        PhasePrinter pp = new PhasePrinter(modid);
        pp.attachToProxy(proxy);
        return pp;
    }

    private final String modID;

    private PhasePrinter(String modid) {
        modID = modid;
    }

    /**
     * Attach this printer to the given proxy.
     * 
     * @param proxy
     *            - proxy to bind to
     */
    public void attachToProxy(Proxy proxy) {
        for (State state : State.values()) {
            if (state.linkedState().hasEvent())
                proxy.registerRegisterableObject(printerROForState(state));
        }
    }

    private RegisterableObject<Void> printerROForState(final State state) {
        return new RegisterableObject<Void>() {
            @Override
            public State registerState() {
                return state;
            }

            @Override
            public Void create() throws Throwable {
                return null;
            }

            @Override
            public void register() {
                Proxy.getHeadOfActiveStack().tryForModLog(modID);
                Proxy.getHeadOfActiveStack()
                        .getLogger()
                        .info("State " + state.linkedState().name() + " for "
                                      + modID);
            }

            @Override
            public void registerClient() {
            }
        };
    }
}
