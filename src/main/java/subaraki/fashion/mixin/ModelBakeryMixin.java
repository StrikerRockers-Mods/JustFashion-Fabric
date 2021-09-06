package subaraki.fashion.mixin;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import subaraki.fashion.mod.Fashion;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation resourceLocation);

    @Inject(method = "<init>", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            shift = At.Shift.AFTER, args = "ldc=special"))
    public void loadSpecialModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i, CallbackInfo ci) {
        for (ResourceLocation specialModel : Fashion.specialModels) {
            addModelToCache(specialModel);
        }
    }

    private void addModelToCache(ResourceLocation resourceLocation) {
        UnbakedModel unbakedmodel = this.getModel(resourceLocation);
        this.unbakedCache.put(resourceLocation, unbakedmodel);
        this.topLevelModels.put(resourceLocation, unbakedmodel);
    }
}
