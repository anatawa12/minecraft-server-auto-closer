package com.anatawa12.mcAutoCloser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Common {
    private static int waitTicks = 0;
    private static int waitSeconds = 0;
    protected static File modFile;

    protected abstract void sendStop();
    protected abstract void startReceiveTicks();
    protected abstract void stopReceiveTicks();
    protected abstract void infoLog(String msg, Object... args);

    public void onServerStarted() {
        waitTicks = 0;
        waitSeconds = 0;
        findWait();
        if (waitTicks == 0 && waitSeconds == 0) {
            infoLog("no delay found so shutdown the server now!");
            sendStop();
        } else if (waitTicks == 0) {
            infoLog("delay {} seconds so starting thread", waitSeconds);
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
            infoLog("delay {} ticks", waitTicks);
            // this means tick based waiting.
            startReceiveTicks();
        }
    }

    public void onTick() {
        if (--waitTicks == -1) {
            sendStop();
            stopReceiveTicks();
        }
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
}
