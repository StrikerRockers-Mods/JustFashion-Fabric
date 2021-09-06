package subaraki.fashion.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface RenderInHandCallback {

    Event<RenderInHandCallback> EVENT = EventFactory.createArrayBacked(RenderInHandCallback.class,
            listeners -> ((hand, poseStack, buffers, light, partialTicks, interpPitch, swingProgress, equipProgress, stack) -> {
                for (RenderInHandCallback listener : listeners) {
                    listener.onRender(hand, poseStack, buffers, light, partialTicks, interpPitch, swingProgress, equipProgress, stack);
                }
            }));

    void onRender(InteractionHand hand, PoseStack poseStack, MultiBufferSource buffers, int light, float partialTicks, float interpPitch, float swingProgress, float equipProgress, ItemStack stack);
}
