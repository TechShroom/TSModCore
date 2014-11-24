package com.techshroom.mods.common.test;

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import com.techshroom.mods.common.Proxy.State;

import cpw.mods.fml.common.LoaderState;

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
    private Set<LoaderState> allLinkedCheck() {
        Set<LoaderState> check = EnumSet.allOf(LoaderState.class);
        for (State state : State.values()) {
            check.remove(state.linkedState());
        }
        return check;
    }

    /**
     * Ensure that we've linked every {@link LoaderState}.
     */
    @Test
    public void allModStatesLinked() {
        Set<LoaderState> check = allLinkedCheck();
        assertTrue("Need to implement " + check, check.isEmpty());
    }

    /**
     * Ensure that we've ordered everything according to {@link LoaderState}.
     */
    @Test
    public void orderedLikeModState() {
        LoaderState lastLink = null;
        for (State state : State.values()) {
            LoaderState stateLink = state.linkedState();
            if (lastLink == null) {
                // ordered by loader state if null
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
