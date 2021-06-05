package com.anatawa12.mcAutoCloser.for1122.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transformer implements IClassTransformer {
    // 1.12 -> 1.8~11 mapping
    Map<String, String> classes = new HashMap<>();

    public Transformer() {
        String mcVersion = (String) FMLInjectionData.data()[4];
        Matcher matcher = Pattern.compile("\\d+\\.(\\d+)(\\.\\d+)?").matcher(mcVersion);
        if (!matcher.matches())
            throw new IllegalStateException("invalid mc version");
        int majorVersion = Integer.parseInt(matcher.group(1));
        switch (majorVersion) {
            case 8:
                // https://github.com/ModCoderPack/MCPBot-Issues/issues/110
                classes.putIfAbsent("net/minecraft/util/text/ITextComponent", "net/minecraft/util/IChatComponent");
                classes.putIfAbsent("net/minecraft/util/text/TextComponentString", "net/minecraft/util/ChatComponentText");
                // https://github.com/ModCoderPack/MCPBot-Issues/issues/113
                classes.putIfAbsent("net/minecraft/util/math/BlockPos", "net/minecraft/util/BlockPos");
                // also https://github.com/ModCoderPack/MCPBot-Issues/issues/114
                classes.putIfAbsent("net/minecraft/util/math/Vec3d", "net/minecraft/util/Vec3");
                //noinspection fallthrough
            case 9:
            case 10:
            case 11:
            case 12:
                break;
        }
    }

    // 1.8 compatibility
    @SuppressWarnings("deprecation")
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (classes.isEmpty()) return basicClass;
        if (!name.startsWith("com.anatawa12.mcAutoCloser.for1122")) return basicClass;
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = cw;
        cv = new RemappingClassAdapter(cv, new SimpleRemapper(classes));
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
