package net.minecraftforge.fml.relauncher;

// for compatibility with 1.7
public interface IFMLLoadingPlugin {
    String[] getASMTransformerClass();
    String getModContainerClass();
    String getSetupClass();
    void injectData(java.util.Map<String, Object> data);
    String getAccessTransformerClass();
}
