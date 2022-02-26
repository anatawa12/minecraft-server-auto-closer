function initializeCoreMod() {
    return {
        cancellable_FMLServerAboutToStartEvent: {
            target: {
                type: 'CLASS',
                names: function () {
                    return [
                        'net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent',
                        'net.minecraftforge.event.server.ServerAboutToStartEvent',
                    ]
                },
            },
            transformer: function(clazz) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var methods = clazz.methods;
                // if there's isCancelable:()Z, do nothing
                for (var i = 0; i < methods.length; i++) {
                    if (methods[i].name === "isCancelable" && methods[i].desc === "()Z")
                        return;
                }
                var mv = clazz.visitMethod(Opcodes.ACC_PUBLIC, "isCancelable", "()Z", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
                return clazz;
            }
        }
    }
}
