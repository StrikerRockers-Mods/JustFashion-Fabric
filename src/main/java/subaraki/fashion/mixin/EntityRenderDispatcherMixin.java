package subaraki.fashion.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import subaraki.fashion.mixin.accessor.LivingEntityRendererAccessor;
import subaraki.fashion.mod.FashionClient;
import subaraki.fashion.render.layer.LayerAestheticHeldItem;
import subaraki.fashion.render.layer.LayerFashion;
import subaraki.fashion.render.layer.LayerWardrobe;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    private Map<String, EntityRenderer<? extends Player>> playerRenderers;

    @Inject(method = "onResourceManagerReload", at = @At("RETURN"))
    public void addLayers(ResourceManager resourceManager, CallbackInfo ci) {
        FashionClient.fashionMap.clear();

        playerRenderers.keySet().forEach(skinTypeName -> { //default , slim
            if (playerRenderers.get(skinTypeName) instanceof PlayerRenderer renderer) {
                ((LivingEntityRendererAccessor) renderer).getLayers().add(new LayerWardrobe(renderer));
                FashionClient.fashionMap.put(new LayerAestheticHeldItem(renderer), renderer);
                FashionClient.fashionMap.put(new LayerFashion(renderer), renderer);
            }
        });
    }
}
