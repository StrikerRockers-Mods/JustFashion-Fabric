package subaraki.fashion.render;

import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.mixin.accessor.LivingEntityRendererAccessor;
import subaraki.fashion.mod.FashionClient;

import java.util.List;

public class HandleRenderSwap {

    private List<RenderLayer<?, ?>> swapListLayerRenders;

    public void swapRenders(Player player, PlayerRenderer renderer) {
        resetAllBeforeResourceReload(player, renderer);
        FashionData data = FashionData.get(player);

        swapListLayerRenders = ((LivingEntityRendererAccessor) renderer).getLayers();

        // save mod list. not volatile !
        if (data.getSavedLayers().isEmpty()) {
            data.saveOriginalList(swapListLayerRenders);
        }

        // if you need fashion rendered
        if (data.shouldRenderFashion()) {
            // and the cached list (original vanilla list + all exterior mod items) is empty
            if (data.cachedOriginalRenderList == null) {
                // copy the vanilla list over
                data.cachedOriginalRenderList = swapListLayerRenders;

                // if all cached fashion is empty (previously not wearing any
                if (data.fashionLayers.isEmpty()) {

                    // add cached layers for fashion : items and armor
                    for (RenderLayer<?, ?> fashionLayer : FashionClient.fashionMap.keySet()) {
                        if (FashionClient.fashionMap.get(fashionLayer).equals(renderer))
                            data.fashionLayers.add(fashionLayer);
                    }

                    // if the list of layers to keep is not empty (aka layers are selected)
                    if (!data.hasOtherModLayersToRender()) {
                        // add those layers to our fashion list
                        data.fashionLayers.addAll(data.getLayersToKeep());
                    }

                    // add all vanilla layers back , except for items and armor
                    for (RenderLayer<?, ?> layersFromVanilla : data.getVanillaLayersList()) {
                        if (layersFromVanilla instanceof HumanoidArmorLayer || layersFromVanilla instanceof ItemInHandLayer)
                            continue;
                        data.fashionLayers.add(layersFromVanilla);
                    }
                }

                // swap renderers
                ((LivingEntityRendererAccessor) renderer).setLayers(data.fashionLayers);
            }
        } else {
            // if fashion does not need to be rendered , we restore the field to the
            // original list we saved
            if (data.cachedOriginalRenderList != null) {
                ((LivingEntityRendererAccessor) renderer).setLayers(data.cachedOriginalRenderList);
                data.cachedOriginalRenderList = null;
            }
        }
    }

    public void resetRenders(Player player, PlayerRenderer renderer) {
        resetAllBeforeResourceReload(player, renderer);
        FashionData data = FashionData.get(player);

        swapListLayerRenders = ((LivingEntityRendererAccessor) renderer).getLayers();
        // reset rendering to the cached list of all layers when the game started
        if (data.cachedOriginalRenderList != null) {
            ((LivingEntityRendererAccessor) renderer).setLayers(data.cachedOriginalRenderList);
            data.cachedOriginalRenderList = null;
        }
    }

    public void resetAllBeforeResourceReload(Player player, PlayerRenderer playerRenderer) {
        if (player != null) {
            swapListLayerRenders = null;
            FashionData.get(player).checkResourceReloadAndReset(playerRenderer);
        }
    }
}