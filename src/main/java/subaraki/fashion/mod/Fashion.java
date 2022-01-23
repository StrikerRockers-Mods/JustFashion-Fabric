package subaraki.fashion.mod;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.event.Events;
import subaraki.fashion.network.ServerBoundPackets;
import subaraki.fashion.render.HandleRenderSwap;
import subaraki.fashion.util.ModConfig;

import java.util.ArrayList;
import java.util.List;

public class Fashion implements ModInitializer, EntityComponentInitializer {

    public static final String MODID = "fashion";
    public static final HandleRenderSwap SWAPPER = new HandleRenderSwap();
    public static final ComponentKey<FashionData> FASHION_DATA = ComponentRegistryV3.INSTANCE.getOrCreate(new ResourceLocation(MODID, MODID), FashionData.class);
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ModConfig config;
    public static List<ResourceLocation> specialModels = new ArrayList<>();

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        specialModels.add(new ResourceLocation(MODID, "wardrobe"));
        ServerBoundPackets.register();
        Events.register();
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FASHION_DATA, player -> new FashionData(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
