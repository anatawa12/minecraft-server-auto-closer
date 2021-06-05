package com.anatawa12.mcAutoCloser;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Common {
    private static final File modFile = getClassSourceOf(Common.class);
    private static int waitTicks = 0;
    private static int waitSeconds = 0;

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
        if (waitTicks < 0) return;
        if (--waitTicks == -1) {
            sendStop();
            stopReceiveTicks();
        }
    }

    @SuppressWarnings("UnnecessarySemicolon")
    private void findWait() {
        if (findWaitFile(new File("./config/minecraft-server-auto-closer.txt"))) return;
        if (findWaitFile(new File("./mods/minecraft-server-auto-closer.txt"))) return;
        if (modFile != null) {
            if (findWaitFile(new File("./mods/" + modFile.getName() + ".txt"))) return;
            if (findInFileName(modFile.getName())) return;
        }
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

    private static File getClassSourceOf(@SuppressWarnings("SameParameterValue") Class<?> clazz) {
        try {
            String uri = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            if (uri.startsWith("jar:"))
                uri = uri.substring("jar:".length(), uri.toString().indexOf("!/"));
            String classNamePath = clazz.getName().replace('.', '/') + ".class";
            if (uri.endsWith(classNamePath)) {
                // remove package/ClassName.class suffix
                uri = uri.substring(0, uri.length() - classNamePath.length());
            }
            return new File(new java.net.URI(uri));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static Class<?> findClass(String... names) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names");

        List<Throwable> exceptions = null;
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            }
        }
        throw new RuntimeException("Cannot find class: " + exceptions);
    }

    protected static <O, T> T getField(Class<O> ownerClass, O owner, String... names) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names");

        List<Throwable> exceptions = null;
        for (String name : names) {
            try {
                Field field = ownerClass.getField(name);
                //noinspection unchecked
                return (T) field.get(owner);
            } catch (IllegalAccessException e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            } catch (NoSuchFieldException e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            }
        }
        throw new RuntimeException("Cannot find field: " + exceptions);
    }

    protected static Method findMethod(Class<?> ownerClass, String[] names, Class<?>... types) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names");

        List<Throwable> exceptions = null;
        for (String name : names) {
            try {
                return ownerClass.getMethod(name, types);
            } catch (NoSuchMethodException e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            }
        }
        throw new RuntimeException("Cannot find method: " + exceptions);
    }

    protected static <O, T> T callMethod(Class<O> ownerClass, O owner, String[] names, Object... typeAndParams) {
        if (typeAndParams == null || typeAndParams.length % 2 != 0)
            throw new IllegalArgumentException("names");
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("names");

        Class<?>[] types = new Class<?>[typeAndParams.length / 2];
        Object[] values = new Object[typeAndParams.length / 2];

        for (int i = 0; i < types.length; i++) {
            types[i] = (Class<?>) typeAndParams[i * 2];
            values[i] = typeAndParams[i * 2 + 1];
        }

        List<Throwable> exceptions = null;
        for (String name : names) {
            try {
                Method method = ownerClass.getMethod(name, types);
                //noinspection unchecked
                return (T) method.invoke(owner, values);
            } catch (NoSuchMethodException e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            } catch (IllegalAccessException e) {
                if (exceptions == null) exceptions = new ArrayList<Throwable>();
                exceptions.add(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Cannot find method: " + exceptions);
    }

    protected static <T> T createInstance(Class<T> ownerClass, Object... typeAndParams) {
        if (typeAndParams == null || typeAndParams.length % 2 != 0)
            throw new IllegalArgumentException("names");
        Class<?>[] types = new Class<?>[typeAndParams.length / 2];
        Object[] values = new Object[typeAndParams.length / 2];

        for (int i = 0; i < types.length; i++) {
            types[i] = (Class<?>) typeAndParams[i * 2];
            values[i] = typeAndParams[i * 2 + 1];
        }

        try {
            Constructor<T> constructor = ownerClass.getConstructor(types);
            return constructor.newInstance(values);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
