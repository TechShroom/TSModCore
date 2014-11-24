package com.techshroom.mods.common.test;

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import com.techshroom.mods.common.Proxy.State;

import cpw.mods.fml.common.LoaderState.ModState;

/**
 * Tests for {@link State}.
 * 
 * @author Kenzie Togami
 */
public class StateTests {
    /**
     * Common check for all linked.
     * 
     * @return
     */
    private Set<ModState> allLinkedCheck() {
        Set<ModState> check = EnumSet.allOf(ModState.class);
        for (State state : State.values()) {
            check.remove(state.linkedState());
        }
        return check;
    }

    /**
     * Ensure that we've linked every {@link ModState}.
     */
    @Test
    public void allModStatesLinked() {
        Set<ModState> check = allLinkedCheck();
        assertTrue("Need to implement " + check, check.isEmpty());
    }

    /**
     * Ensure that we've ordered everything according to {@link ModState}.
     */
    @Test
    public void orderedLikeModState() {
        ModState lastLink = null;
        for (State state : State.values()) {
            ModState stateLink = state.linkedState();
            if (lastLink == null) {
                // ordered by mod state if null
                lastLink = stateLink;
                continue;
            }
            assertFalse(State.from(stateLink) + " should be before "
                                + State.from(lastLink),
                        stateLink.compareTo(lastLink) <= 0);
            lastLink = stateLink;
        }
    }
}
