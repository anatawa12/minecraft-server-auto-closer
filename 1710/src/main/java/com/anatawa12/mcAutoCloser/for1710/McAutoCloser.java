package com.anatawa12.mcAutoCloser.for1710;

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
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = "server-auto-closer")
@SuppressWarnings("unused")
public class McAutoCloser implements ICommandSender {
    private static Logger LOGGER = LogManager.getLogger("McAutoCloser");
    private static int waitTicks = 0;
    private static int waitSeconds = 0;
    private static File modFile;

    // single side mod
    @NetworkCheckHandler
    public boolean checkRemote(Map<?, ?> _map, Side _side) {
        return true;
    }

    @EventHandler
    public void init(FMLServerStartedEvent _event) {
        if (FMLCommonHandler.instance().getSide() != Side.SERVER) return;
        if (!(FMLCommonHandler.instance().getMinecraftServerInstance() instanceof DedicatedServer)) return;
        waitTicks = 0;
        waitSeconds = 0;
        findWait();
        if (waitTicks == 0 && waitSeconds == 0) {
            LOGGER.info("no delay found so shutdown the server now!");
            sendStop();
        } else if (waitTicks == 0) {
            LOGGER.info("delay {} seconds so starting thread", waitSeconds);
            // this means second based waiting.
            new Thread() {
                {
                    setName("McAutoCloser");
                    setDaemon(true);
                }
                @Override
                public void run() {
                    try {
                        sleep(waitSeconds * 1000L);
                        sendStop();
                    } catch (InterruptedException ignored) {
                    }
                }
            }.start();
        } else {
            LOGGER.info("delay {} ticks", waitTicks);
            // this means tick based waiting.
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        modFile = event.getSourceFile();
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent _event) {
        if (_event.phase != TickEvent.Phase.START) return;
        if (--waitTicks == -1) {
            sendStop();
            FMLCommonHandler.instance().bus().unregister(this);
        }
    }

    private void sendStop() {
        MinecraftServer instance = FMLCommonHandler.instance().getMinecraftServerInstance();
        ((DedicatedServer) instance).addPendingCommand("stop", this);
    }

    @SuppressWarnings("UnnecessarySemicolon")
    private void findWait() {
        if (findWaitFile(new File("./config/minecraft-server-auto-closer.txt"))) return;
        if (findWaitFile(new File("./mods/minecraft-server-auto-closer.txt"))) return;
        if (findWaitFile(new File("./mods/" + modFile.getName() + ".txt"))) return;
        if (findInFileName(modFile.getName())) return;
        ;
    }

    private boolean findWaitFile(File file) {
        Scanner sc = null;
        try {
            sc = new Scanner(file);
            int count = sc.nextInt();
            if (count < 0) return false;
            String token = sc.next();
            if ("seconds".equals(token)) {
                waitSeconds = count;
                return true;
            }
            if ("ticks".equals(token)) {
                waitTicks = count;
                return true;
            }
            return false;
        } catch (FileNotFoundException e) {
            return false;
        } catch (NoSuchElementException e) {
            return false;
        } finally {
            if (sc != null) sc.close();
        }
    }

    private boolean findInFileName(String name) {
        Pattern pattern = Pattern.compile("stop-after-(\\d+)-(seconds|ticks)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            if (matcher.group(2).equals("seconds")) {
                waitSeconds = value;
            } else {
                waitTicks = value;
            }
            return true;
        }
        return false;
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
