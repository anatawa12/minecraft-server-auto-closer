package com.anatawa12.mcAutoCloser.for1710;

import com.anatawa12.mcAutoCloser.Common;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

@Mod(modid = "server-auto-closer")
@SuppressWarnings("unused")
public class McAutoCloser extends Common implements ICommandSender {
    private static final Logger LOGGER = LogManager.getLogger("McAutoCloser");

    // single side mod
    @NetworkCheckHandler
    public boolean checkRemote(Map<?, ?> _map, Side _side) {
        return true;
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent _event) {
        if (FMLCommonHandler.instance().getSide() != Side.SERVER) return;
        if (!(FMLCommonHandler.instance().getMinecraftServerInstance() instanceof DedicatedServer)) return;
        onServerStarted();
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent _event) {
        if (_event.phase != TickEvent.Phase.START) return;
        onTick();
    }

    protected void sendStop() {
        MinecraftServer instance = FMLCommonHandler.instance().getMinecraftServerInstance();
        ((DedicatedServer) instance).addPendingCommand("stop", this);
    }

    @Override
    protected void startReceiveTicks() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    protected void stopReceiveTicks() {
        FMLCommonHandler.instance().bus().unregister(this);
    }

    @Override
    protected void infoLog(String msg, Object... args) {
        LOGGER.info(msg, args);
    }

    @Override
    public String getCommandSenderName() {
        return "McAutoCloser";
    }

    @Override
    public IChatComponent func_145748_c_() {
        return new ChatComponentText(getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent component) {
        LOGGER.info("McAutoCloser: {}", component.getUnformattedText());
    }

    @Override
    public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) {
        return true;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(0, 0, 0);
    }

    @Override
    public World getEntityWorld() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
    }
}
