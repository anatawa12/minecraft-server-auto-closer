package com.anatawa12.mcAutoCloser.asm;

import java.util.Map;

public class CoreMod implements net.minecraftforge.fml.relauncher.IFMLLoadingPlugin, cpw.mods.fml.relauncher.IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        try {
            Class.forName("cpw.mods.fml.relauncher.CoreModManager");
            return new String[]{
                    "com.anatawa12.mcAutoCloser.asm.HookTransformer",
            };
        } catch (ClassNotFoundException e) {
            // it's 1.12.2
            return new String[]{
                    "com.anatawa12.mcAutoCloser.asm.HookTransformer",
                    "com.anatawa12.mcAutoCloser.for1122.asm.Transformer",
            };
        }
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
