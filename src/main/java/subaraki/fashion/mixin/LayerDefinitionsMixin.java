package subaraki.fashion.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import subaraki.fashion.render.FashionModels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
    @Inject(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addLayerDefinitions(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir, ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        Map<ModelLayerLocation, Supplier<LayerDefinition>> layerDefinitions = new HashMap<>();
        FashionModels.registerLayerDefinition(layerDefinitions);
        layerDefinitions.forEach((k, v) -> builder.put(k, v.get()));
    }
}
