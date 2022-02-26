package cpw.mods.fml.relauncher;

// for compatibility with 1.12
public interface IFMLLoadingPlugin {
    String[] getASMTransformerClass();
    String getModContainerClass();
    String getSetupClass();
    void injectData(java.util.Map<String, Object> data);
    String getAccessTransformerClass();
}
