package com.techshroom.mods.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

import com.techshroom.mods.common.proxybuilders.PhasePrinter;

/**
 * TSModCore central mod class.
 * 
 * @author Kenzie Togami
 */
@Mod(modid = TSModCoreMod.ID, name = TSModCoreMod.USER_FRIENDLY_NAME,
        acceptedMinecraftVersions = TSModCoreMod.ACCEPTED_VERSIONS,
        version = TSModCoreMod.VERSION)
public class TSModCoreMod {
    /**
     * TSModCore's mod ID
     */
    public static final String ID = "@MODID@";
    /**
     * Mod version
     */
    public static final String VERSION = "@VERSION@";
    /**
     * Accepted Minecraft versions
     */
    public static final String ACCEPTED_VERSIONS = "@SUPPORTED_VERSIONS@";
    /**
     * End-user name for the mod.
     */
    public static final String USER_FRIENDLY_NAME = "TSModCore";
    /**
     * Proxy reference. Probably not a good idea to even look at.
     */
    @SidedProxy(serverSide = Proxy.QUALNAME, clientSide = ClientProxy.QUALNAME)
    public static Proxy PROXY;

    @SuppressWarnings("javadoc")
    @EventHandler
    public void construct(FMLConstructionEvent e) {
        PhasePrinter.addPrinter(PROXY, ID);
    }
}
