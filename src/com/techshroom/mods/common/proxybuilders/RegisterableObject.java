package com.techshroom.mods.common.proxybuilders;

import com.techshroom.mods.common.Proxy.State;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Represents an object that can be registered, probably with Minecraft.
 * 
 * @author Kenzie Togami
 *
 * @param <Type>
 *            - stored type
 */
public interface RegisterableObject<Type> {
    /**
     * The State at which the handler should call {@link #register()}.
     * 
     * @return State to call register().
     */
    State registerState();

    /**
     * Creates the Type bound to this object.
     * 
     * @return the created object of type Type.
     * 
     * @throws Throwable
     *             exceptions propagate
     * @apiNote don't register here
     */
    Type create() throws Throwable;

    /**
     * Common register function.
     */
    void register();

    /**
     * Client register function. Do not call common register function here. Be
     * aware that this function doesn't exist on the server.
     */
    @SideOnly(Side.CLIENT)
    void registerClient();
}
