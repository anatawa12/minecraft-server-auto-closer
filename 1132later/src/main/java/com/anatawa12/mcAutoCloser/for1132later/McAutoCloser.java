package com.anatawa12.mcAutoCloser.for1132later;

import net.minecraftforge.fml.common.Mod;

/** stab to handle 1.13-1.16 and 1.17 */
@Mod(value = "server_auto_closer", modid = "server_auto_closer_dummy")
public class McAutoCloser {
    public McAutoCloser() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        switch (McVersion.detect()) {
            case V1_8:
                return;
            case V1_13:
                Class.forName("com.anatawa12.mcAutoCloser.for1132.McAutoCloser").newInstance();
                break;
            case V1_17:
                Class.forName("com.anatawa12.mcAutoCloser.for1171.McAutoCloser").newInstance();
                break;
        }
    }

    enum McVersion {
        V1_8,
        V1_13,
        V1_17,
        ;
        static McVersion detect() {
            try {
                // 1.12.2/older block
                Class.forName("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext");
            } catch (ClassNotFoundException e) {
                return V1_8;
            }
            try {
                // entity.Entity is in official mapping = 1.17/later
                Class.forName("net.minecraft.world.entity.Entity");
                return V1_17;
            } catch (ClassNotFoundException e) {
                return V1_13;
            }
        }
    }
}
