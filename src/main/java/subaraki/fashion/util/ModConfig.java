package subaraki.fashion.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import subaraki.fashion.mod.Fashion;

@Config(name = Fashion.MODID)
public class ModConfig implements ConfigData {
    public boolean bigger_model = false;
    public boolean face_mirror = true;
}
