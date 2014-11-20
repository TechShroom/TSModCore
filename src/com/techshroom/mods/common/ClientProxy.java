package com.techshroom.mods.common;

import com.techshroom.mods.common.proxybuilders.RegisterableObject;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client version of Proxy.
 * 
 * @author Kenzie Togami
 */
@SideOnly(Side.CLIENT)
public class ClientProxy
        extends Proxy {
    @Override
    protected void regObjHook(RegisterableObject<?> regObj) throws Throwable {
        super.regObjHook(regObj);
        regObj.registerClient();
    }
}