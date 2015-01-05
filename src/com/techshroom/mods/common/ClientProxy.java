package com.techshroom.mods.common;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.techshroom.mods.common.proxybuilders.RegisterableObject;

/**
 * Client version of Proxy.
 * 
 * @author Kenzie Togami
 */
@SideOnly(Side.CLIENT)
public class ClientProxy
        extends Proxy {
    /**
     * Qualified name of this class.
     */
    public static final String QUALNAME =
            "com.techshroom.mods.common.ClientProxy";

    @Override
    protected void regObjHook(RegisterableObject<?> regObj) throws Throwable {
        super.regObjHook(regObj);
        regObj.registerClient();
    }

    @Override
    public boolean isClient() {
        return true;
    }
}