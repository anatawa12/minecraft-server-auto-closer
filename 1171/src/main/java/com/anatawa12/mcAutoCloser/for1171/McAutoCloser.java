package com.anatawa12.mcAutoCloser.for1171;

import com.anatawa12.mcAutoCloser.Common;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class McAutoCloser extends Common {
    private static final Logger LOGGER = LogManager.getLogger("McAutoCloser");
    private static boolean isServer;
    private static DedicatedServer server;
    private static Listeners listeners;

    public McAutoCloser() {
        listeners = new Listeners();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(listeners::dedicatedServerSetup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                listeners.classFMLServerStartedEvent, listeners::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                listeners.classFMLServerStoppedEvent, listeners::serverStopping);
    }


    final class Listeners {
        public void dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
            isServer = true;
        }

        @SuppressWarnings("unchecked")
        private final Class<Event> classFMLServerStartedEvent = (Class<Event>) findClass(
                "net.minecraftforge.fmlserverevents.FMLServerStartedEvent",
                "net.minecraftforge.event.server.ServerStartedEvent");

        @SuppressWarnings("unchecked")
        private final Class<Event> classFMLServerStoppedEvent = (Class<Event>) findClass(
                "net.minecraftforge.fmlserverevents.FMLServerStoppedEvent",
                "net.minecraftforge.event.server.ServerStoppedEvent");

        // You can use SubscribeEvent and let the Event Bus discover methods to call
        public void serverStarted(Event event) {
            if (!isServer) return;
            Object mayServer = callMethod(classFMLServerStartedEvent, event, new String[]{"getServer"});
            if (!(mayServer instanceof DedicatedServer)) return;
            server = (DedicatedServer) mayServer;
            onServerStarted();
        }

        public void serverStopping(Event event) {
            server = null;
        }

        public void tick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            onTick();
        }
    }

    @Override
    protected void sendStop() {
        SenderImpl.INSTANCE.sendStop();
    }

    @Override
    protected void startReceiveTicks() {
        MinecraftForge.EVENT_BUS.addListener(listeners::tick);
    }

    @Override
    protected void stopReceiveTicks() {
        MinecraftForge.EVENT_BUS.addListener(listeners::tick);
    }

    @Override
    protected void infoLog(String msg, Object... args) {
        LOGGER.info(msg, args);
    }

    protected static class SenderImpl implements CommandSource {
        protected static SenderImpl INSTANCE = new SenderImpl();

        protected void sendStop() {
            server.handleConsoleInput("stop", getCommandSource());
        }

        @SuppressWarnings("ConstantConditions")
        public CommandSourceStack getCommandSource() {
            return new CommandSourceStack(this, Vec3.ZERO, Vec2.ZERO, server.getLevel(Level.OVERWORLD), 
                    4, "McAutoCloser", new TextComponent("McAutoCloser"), server, null);
        }

        // 1.17 sendMessage
        @Override
        @SuppressWarnings("NullableProblems")
        public void sendMessage(Component component, UUID _unused) {
            LOGGER.info("McAutoCloser: {}", component.getString());
        }

        // 1.17: acceptsSuccess
        public boolean acceptsSuccess() {
            return true;
        }

        // 1.17: acceptsFailure
        public boolean acceptsFailure() {
            return true;
        }

        // allowLogging
        public boolean shouldInformAdmins() {
            return true;
        }

        // 1.17: alwaysAccepts
        public boolean alwaysAccepts() {
            return true;
        }
    }
}
