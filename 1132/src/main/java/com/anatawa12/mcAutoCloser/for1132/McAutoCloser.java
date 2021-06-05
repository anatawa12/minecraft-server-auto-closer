package com.anatawa12.mcAutoCloser.for1132;

import com.anatawa12.mcAutoCloser.Common;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.UUID;

@Mod("server-auto-closer")
public class McAutoCloser extends Common implements ICommandSource {
    private static final Logger LOGGER = LogManager.getLogger("McAutoCloser");
    private static boolean isServer;
    private static DedicatedServer server;

    public McAutoCloser() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::dedicatedServerSetup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
    }

    public void dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        isServer = true;
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    public void serverStarted(FMLServerStartedEvent event) {
        if (!isServer) return;
        if (!(event.getServer() instanceof DedicatedServer)) return;
        server = (DedicatedServer) event.getServer();
        onServerStarted();
    }

    public void serverStopping(FMLServerStoppedEvent event) {
        server = null;
    }

    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        onTick();
    }

    @Override
    protected void sendStop() {
        server.handleConsoleInput("stop", getCommandSource());
    }

    @Override
    protected void startReceiveTicks() {
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }

    @Override
    protected void stopReceiveTicks() {
    }

    @Override
    protected void infoLog(String msg, Object... args) {
        LOGGER.info(msg, args);
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
                                "OVERWORLD", 
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
    public void func_145747_a(ITextComponent component, UUID _) {
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
