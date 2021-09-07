package subaraki.fashion.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.mixin.accessor.LivingEntityRendererAccessor;
import subaraki.fashion.render.EnumFashionSlot;
import subaraki.fashion.util.ClientReferences;

import java.util.List;
import java.util.UUID;

public class ClientReferencesPacket {
    public static void handle(ResourceLocation[] ids, boolean isActive, UUID sender, List<String> layers) {

        Player distantPlayer = ClientReferences.getClientPlayerByUUID(sender);
        FashionData distFashion = FashionData.get(distantPlayer);


        for (EnumFashionSlot slot : EnumFashionSlot.values())
            distFashion.updateFashionSlot(ids[slot.ordinal()], slot);
        distFashion.setRenderFashion(isActive);

        EntityRenderer<? super Player> distantPlayerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(distantPlayer);

        List<RenderLayer<?, ?>> ob = ((LivingEntityRendererAccessor) distantPlayerRenderer).getLayers();
        distFashion.resetKeepLayerForDistantPlayer();
        if (layers != null && !layers.isEmpty() && ob != null) {
            for (RenderLayer<?, ?> content : ob) {
                for (String name : layers)
                    if (content.getClass().getSimpleName().equals(name))
                        distFashion.addLayerToKeep(name);
            }
        }
        distFashion.fashionLayers.clear();
    }
}
