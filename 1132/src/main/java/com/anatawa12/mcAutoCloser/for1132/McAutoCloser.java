package com.anatawa12.mcAutoCloser.for1132;

import com.anatawa12.mcAutoCloser.Common;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.UUID;

@Mod(value = "server_auto_closer", modid = "server_auto_closer_dummy")
public class McAutoCloser extends Common {
    private static final Logger LOGGER = LogManager.getLogger("McAutoCloser");
    private static boolean isServer;
    private static DedicatedServer server;
    private static Listeners listeners;

    public McAutoCloser() {
        try {
            // 1.12.2 block
            Class.forName("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext");
        } catch (ClassNotFoundException e) {
            return;
        }
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
                "net.minecraftforge.fml.event.server.FMLServerStartedEvent",
                "net.minecraftforge.fmlserverevents.FMLServerStartedEvent",
                "net.minecraftforge.event.server.ServerStartedEvent");

        @SuppressWarnings("unchecked")
        private final Class<Event> classFMLServerStoppedEvent = (Class<Event>) findClass(
                "net.minecraftforge.fml.event.server.FMLServerStoppedEvent",
                "net.minecraftforge.fmlserverevents.FMLServerStoppedEvent",
                "net.minecraftforge.event.server.ServerStoppedEvent");

        @SuppressWarnings("unchecked")
        private final Class<Event> classTickEvent$ServerTickEvent = (Class<Event>) findClass(
                "net.minecraftforge.fml.common.gameevent.TickEvent$ServerTickEvent",
                "net.minecraftforge.event.TickEvent$ServerTickEvent");

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

        public void tick(Event event) {
            if (getField(classTickEvent$ServerTickEvent, event, "phase") != TickEvent.Phase.START) return;
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
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                listeners.classTickEvent$ServerTickEvent, listeners::tick);
    }

    @Override
    protected void infoLog(String msg, Object... args) {
        LOGGER.info(msg, args);
    }

    protected static class SenderImpl implements ICommandSource {
        protected static SenderImpl INSTANCE = new SenderImpl();

        protected void sendStop() {
            server.handleConsoleInput("stop", getCommandSource());
        }

        public CommandSource getCommandSource() {
            Class<?> vec3dClass = findClass("net.minecraft.util.math.Vec3d",
                    "net.minecraft.util.math.vector.Vector3d");
            Class<?> vec2fClass = findClass("net.minecraft.util.math.Vec2f",
                    "net.minecraft.util.math.vector.Vector2f");
            Class<?> worldServerClass = findClass("net.minecraft.world.WorldServer",
                    "net.minecraft.world.server.ServerWorld");
            // the class to explain type of dimention
            Class<?> dimensionKeyClass = findClass("net.minecraft.world.dimension.DimensionType",
                    "net.minecraft.util.RegistryKey");
            // the class has dimention type name
            Class<?> dimensionTypeClass = findClass("net.minecraft.world.dimension.DimensionType",
                    "net.minecraft.world.DimensionType");
            Class<?> textComponentString = findClass("net.minecraft.util.text.TextComponentString",
                    "net.minecraft.util.text.StringTextComponent");

            return createInstance(CommandSource.class,
                    ICommandSource.class, this,
                    vec3dClass, getField(vec3dClass, null, "ZERO", "field_186680_a"),
                    vec2fClass, getField(vec2fClass, null, "ZERO", "field_189974_a"),
                    worldServerClass, callMethod(DedicatedServer.class, server, new String[]{
                                    "getWorld", "func_71218_a",
                                    "getLevel", "func_71218_a"},
                            dimensionKeyClass, getField(dimensionTypeClass, null,
                                    "OVERWORLD", "field_223227_a_",
                                    "OVERWORLD_LOCATION", "field_235999_c_")),
                    int.class, 4,
                    String.class, "McAutoCloser",
                    ITextComponent.class, createInstance(textComponentString, String.class, "McAutoCloser"),
                    MinecraftServer.class, server,
                    Entity.class, null);
        }

        @Override
        public void sendMessage(@Nonnull ITextComponent component) {
            LOGGER.info("McAutoCloser: {}", component.getString());
        }

        // 1.16
        public void func_145747_a(ITextComponent component, @SuppressWarnings("ForwardCompatibility") UUID _) {
            LOGGER.info("McAutoCloser: {}", component.getString());
        }

        @Override
        public boolean shouldReceiveFeedback() {
            return true;
        }

        @Override
        public boolean shouldReceiveErrors() {
            return true;
        }

        @Override
        public boolean allowLogging() {
            return true;
        }
    }
}
