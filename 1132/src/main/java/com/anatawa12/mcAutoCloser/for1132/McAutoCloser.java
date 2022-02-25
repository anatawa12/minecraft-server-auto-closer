package com.anatawa12.mcAutoCloser.for1132;

import com.anatawa12.mcAutoCloser.Common;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

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

    protected static class SenderImpl {
        protected static SenderImpl INSTANCE = new SenderImpl();

        protected void sendStop() {
            callMethod(DedicatedServer.class, server,
                    new String[]{"handleConsoleInput", "func_195581_a", "m_139645_"},
                    String.class, "stop",
                    commandSource, getCommandSource());
        }

        Class<?> commandSource = findClass("net.minecraft.command.CommandSource", 
                "net.minecraft.commands.CommandSourceStack");

        public Object getCommandSource() {
            Class<?> iCommandSource = findClass("net.minecraft.command.ICommandSource",
                    "net.minecraft.commands.CommandSource");
            Class<?> vec3dClass = findClass("net.minecraft.util.math.Vec3d",
                    "net.minecraft.util.math.vector.Vector3d",
                    "net.minecraft.world.phys.Vec3");
            Class<?> vec2fClass = findClass("net.minecraft.util.math.Vec2f",
                    "net.minecraft.util.math.vector.Vector2f",
                    "net.minecraft.world.phys.Vec2");
            Class<?> worldServerClass = findClass("net.minecraft.world.WorldServer",
                    "net.minecraft.world.server.ServerWorld",
                    "net.minecraft.server.level.ServerLevel");
            // the class to explain type of dimention
            Class<?> dimensionKeyClass = findClass("net.minecraft.world.dimension.DimensionType",
                    "net.minecraft.util.RegistryKey",
                    "net.minecraft.resources.ResourceKey");
            // the class has dimention type name
            Class<?> dimensionTypeClass = findClass("net.minecraft.world.dimension.DimensionType",
                    "net.minecraft.world.DimensionType",
                    "net.minecraft.world.level.dimension.DimensionType");
            Class<?> iTextComponent = findClass("net.minecraft.util.text.ITextComponent",
                    "net.minecraft.network.chat.Component");
            Class<?> textComponentString = findClass("net.minecraft.util.text.TextComponentString",
                    "net.minecraft.util.text.StringTextComponent",
                    "net.minecraft.network.chat.TextComponent");
            Class<?> entity = findClass("net.minecraft.entity.Entity",
                    "net.minecraft.world.entity.Entity");

            Object sourceImpl = Proxy.newProxyInstance(iCommandSource.getClassLoader(), new Class[]{iCommandSource},
                    new InvocationHandlerImpl());

            return createInstance(commandSource,
                    iCommandSource, sourceImpl,
                    vec3dClass, getField(vec3dClass, null, "ZERO", "field_186680_a", "f_82478_"),
                    vec2fClass, getField(vec2fClass, null, "ZERO", "field_189974_a", "f_82462_"),
                    worldServerClass, callMethod(DedicatedServer.class, server, new String[]{
                                    "getWorld", "func_71218_a",
                                    "getLevel", "func_71218_a", "m_129880_"},
                            dimensionKeyClass, getField(dimensionTypeClass, null,
                                    "OVERWORLD", "field_223227_a_",
                                    "OVERWORLD_LOCATION", "field_235999_c_", "f_63845_")),
                    int.class, 4,
                    String.class, "McAutoCloser",
                    iTextComponent, createInstance(textComponentString, String.class, "McAutoCloser"),
                    MinecraftServer.class, server,
                    entity, null);
        }

        private class InvocationHandlerImpl implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    Class<?>[] classes = new Class<?>[method.getParameterCount()];
                    Arrays.fill(classes, Object.class);
                    return SenderImpl.class.getMethod(method.getName(), classes)
                            .invoke(SenderImpl.this, args);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw t;
                }
            }
        }

        // sendMessage
        @SuppressWarnings("unchecked")
        public void func_145747_a(@Nonnull Object component) {
            LOGGER.info("McAutoCloser: {}",
                    (Object) callMethod((Class<Object>) component.getClass(), component, new String[]{"getString"}));
        }

        // 1.16
        public void func_145747_a(Object component, @SuppressWarnings("ForwardCompatibility") Object _) {
            func_145747_a(component);
        }

        // 1.17 sendMessage
        public void m_6352_(Object component, @SuppressWarnings("ForwardCompatibility") Object _) {
            func_145747_a(component);
        }

        // shouldReceiveFeedback
        public boolean func_195039_a() {
            return true;
        }

        // 1.17: acceptsSuccess
        public boolean m_6999_() {
            return true;
        }

        // shouldReceiveErrors
        public boolean func_195040_b() {
            return true;
        }

        // 1.17: acceptsFailure
        public boolean m_7028_() {
            return true;
        }

        // allowLogging
        public boolean func_195041_r_() {
            return true;
        }

        // 1.17: shouldInformAdmins
        public boolean m_6102_() {
            return true;
        }

        // 1.17: alwaysAccepts
        public boolean m_142559_() {
            return true;
        }
    }
}
