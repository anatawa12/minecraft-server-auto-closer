package com.anatawa12.mcAutoCloser;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.Map;

@Mod(modid = "server-auto-closer")
@SuppressWarnings("unused")
public class McAutoCloser implements ICommandSender {
    // single side mod
    @NetworkCheckHandler
    public boolean checkRemote(Map<?, ?> _map, Side _side) {
        return true;
    }

    @EventHandler
    public void init(FMLServerStartedEvent _event) {
        MinecraftServer instance = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (instance instanceof DedicatedServer) {
            ((DedicatedServer) instance).addPendingCommand("stop", this);
        }
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
        System.out.println("McAutoCloser: " + component.getUnformattedText());
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
