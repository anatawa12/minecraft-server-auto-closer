package com.anatawa12.mcAutoCloser.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("JavaReflectionMemberAccess")
public class HookTransformer implements net.minecraft.launchwrapper.IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("cpw.mods.fml.common.FMLCommonHandler".equals(name) || 
                "net.minecraftforge.fml.common.FMLCommonHandler".equals(name)) {
            ClassReader reader = new ClassReader(basicClass);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassVisitorImpl(writer), 0);
            return writer.toByteArray();
        }
        return basicClass;
    }

    private static final int latestAsm;

    static {
        int latestAsm0 = Opcodes.ASM4;
        try {
            latestAsm0 = Opcodes.class.getField("ASM5").getInt(null);
            latestAsm0 = Opcodes.class.getField("ASM6").getInt(null);
            latestAsm0 = Opcodes.class.getField("ASM7").getInt(null);
            latestAsm0 = Opcodes.class.getField("ASM8").getInt(null);
            latestAsm0 = Opcodes.class.getField("ASM9").getInt(null);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        latestAsm = latestAsm0;
    }

    private static class ClassVisitorImpl extends ClassVisitor {
        public ClassVisitorImpl(ClassVisitor cv) {
            super(latestAsm, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("handleServerAboutToStart") && desc.endsWith(")Z"))
                return new MethodVisitorImpl(super.visitMethod(access, name, desc, signature, exceptions));
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static class MethodVisitorImpl extends MethodVisitor {
        public MethodVisitorImpl(MethodVisitor methodVisitor) {
            super(latestAsm, methodVisitor);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.IRETURN) {
                Label return_false = new Label();
                // if false, return false
                super.visitJumpInsn(Opcodes.IFEQ, return_false);
                
                // if true, call com/anatawa12/mcAutoCloser/Common.onAboutToStart:()V
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/anatawa12/mcAutoCloser/Common",
                        "onAboutToStart", "()Z");
                
                // and return false if it returns true
                super.visitJumpInsn(Opcodes.IFNE, return_false);
                
                super.visitInsn(Opcodes.ICONST_1);
                super.visitInsn(Opcodes.IRETURN);
                
                super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                super.visitLabel(return_false);
                super.visitInsn(Opcodes.ICONST_0);
                super.visitInsn(Opcodes.IRETURN);
                return;
            }
            super.visitInsn(opcode);
        }
    }
}
