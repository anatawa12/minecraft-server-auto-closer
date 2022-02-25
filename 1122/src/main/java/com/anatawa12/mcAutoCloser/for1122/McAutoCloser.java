package com.anatawa12.mcAutoCloser.for1122;

import com.anatawa12.mcAutoCloser.Common;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Mod(modid = "server-auto-closer", value = "server-auto-closer-dummy")
@SuppressWarnings({"unused", "NullableProblems"})
public class McAutoCloser extends Common {
    private static final Logger LOGGER = LogManager.getLogger("McAutoCloser");

    // single side mod
    @NetworkCheckHandler
    public boolean checkRemote(Map<?, ?> _map, Side _side) {
        return true;
    }

    @EventHandler
    public void init(FMLServerStartedEvent _event) {
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
        SenderImpl.INSTANCE.sendStop();
    }

    @Override
    protected void startReceiveTicks() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void stopReceiveTicks() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void infoLog(String msg, Object... args) {
        LOGGER.info(msg, args);
    }

    final static class SenderImpl implements ICommandSender {
        static SenderImpl INSTANCE = new SenderImpl();

        void sendStop() {
            MinecraftServer instance = FMLCommonHandler.instance().getMinecraftServerInstance();
            ((DedicatedServer) instance).addPendingCommand("stop", this);
        }

        @Override
        @Nonnull
        public String getName() {
            return "McAutoCloser";
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TextComponentString(this.getName());
        }

        @Override
        public void sendMessage(ITextComponent component) {
            // 1.8 compatibility
            LOGGER.info("McAutoCloser: {}", new Object[]{component.getUnformattedText()});
        }

        public void setCommandStat(CommandResultStats.Type type, int amount) {
        }

        @Override
        public boolean canUseCommand(int permLevel, @Nonnull String commandName) {
            return true;
        }

        @Override
        public BlockPos getPosition() {
            return BlockPos.ORIGIN;
        }

        @Override
        public Vec3d getPositionVector() {
            return Vec3d.ZERO;
        }

        @Override
        @Nonnull
        public World getEntityWorld() {
            return FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
        }

        @Nullable
        @Override
        public Entity getCommandSenderEntity() {
            return null;
        }

        @Override
        public boolean sendCommandFeedback() {
            return false;
        }

        @Nullable
        @Override
        public MinecraftServer getServer() {
            return FMLCommonHandler.instance().getMinecraftServerInstance();
        }
    }
}
