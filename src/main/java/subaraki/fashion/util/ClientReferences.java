package subaraki.fashion.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.mixin.accessor.EntityRenderDispatcherAccessor;
import subaraki.fashion.mixin.accessor.LivingEntityRendererAccessor;

import java.util.List;
import java.util.UUID;

public class ClientReferences {

    /**
     * Used in the packet when logging in to get the list of all layers before the
     * player is actually rendered
     */
    public static List<RenderLayer<?, ?>> tryList() {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? extends Player> renderer = ((EntityRenderDispatcherAccessor) dispatcher).getPlayerRenderers().get("default");
        return ((LivingEntityRendererAccessor) renderer).getLayers();
    }

    public static Player getClientPlayerByUUID(UUID uuid) {
        return Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }

    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

}
