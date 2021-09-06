package subaraki.fashion.mod;

import com.mojang.math.Vector3f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.event.RenderInHandCallback;
import subaraki.fashion.render.EnumFashionSlot;
import subaraki.fashion.render.FashionModels;
import subaraki.fashion.screen.WardrobeScreen;

import java.util.HashMap;
import java.util.Map;

public class FashionClient implements ClientModInitializer {
    public static final Map<RenderLayer<?, ?>, PlayerRenderer> fashionMap = new HashMap<>();
    public static KeyMapping keyWardrobe;
    private static PlayerModel body;

    @Override
    public void onInitializeClient() {
        keyWardrobe = KeyBindingHelper.registerKeyBinding(new KeyMapping("Wardrobe", GLFW.GLFW_KEY_W, "Wardrobe"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (FashionClient.keyWardrobe.consumeClick()) {
                FashionData.get(Minecraft.getInstance().player).setInWardrobe(true);
                Minecraft.getInstance().setScreen(new WardrobeScreen());
            }
        });
        RenderInHandCallback.EVENT.register((hand, poseStack, buffers, light, partialTicks, interpPitch, swingProgress, equipProgress, stack) -> {

            Player player = Minecraft.getInstance().player;

            if (!stack.isEmpty() || player.isScoping())
                return;
            FashionData fashionData = FashionData.get(player);

            if (body == null) {
                body = new PlayerModel<AbstractClientPlayer>(Minecraft.getInstance().getEntityModels().bakeLayer(FashionModels.NORMAL_MODEL_LOCATION), false);
                body.setAllVisible(false);
                // body.rightArm.visible = true;
                // body.rightSleeve.visible = true;
            }

            ResourceLocation resLoc = fashionData.getRenderingPart(EnumFashionSlot.CHEST);

            if (!fashionData.shouldRenderFashion() || resLoc == null || resLoc.toString().contains("missing"))
                return;

            poseStack.pushPose();

            boolean handFlag = hand == InteractionHand.MAIN_HAND;
            if (!handFlag)
                return;
            HumanoidArm humanoidarm = handFlag ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean flag = humanoidarm != HumanoidArm.LEFT;
            float f = flag ? 1.0F : -1.0F;
            float f1 = Mth.sqrt(swingProgress);
            float f2 = -0.3F * Mth.sin(f1 * 3.1415927F);
            float f3 = 0.4F * Mth.sin(f1 * 6.2831855F);
            float f4 = -0.4F * Mth.sin(swingProgress * 3.1415927F);
            poseStack.translate(f * (f2 + 0.64000005F), f3 - 0.6F + equipProgress * -0.6F, f4 - 0.71999997F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
            float f5 = Mth.sin(swingProgress * swingProgress * 3.1415927F);
            float f6 = Mth.sin(f1 * 3.1415927F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
            poseStack.translate(f * -1.0F, 3.5999999046325684D, 3.5D);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
            poseStack.translate(f * 5.6F, 0.0D, 0.0D);

            if (flag) {
                poseStack.translate(0.0625f * 13.5f, -0.0625 * (16f + 11f), 0.0f);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(6.25f));

            } else {
                poseStack.translate(-0.0625f, -0.0625 * (16f + 12.5f), -0.0625f / 2f);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-6.25f));

            }

            var s = 0.0625f * (16f + 16f);
            poseStack.scale(s, s, s);

            if (flag)
                body.rightArm.translateAndRotate(poseStack);
            else
                body.leftArm.translateAndRotate(poseStack);

            body.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(resLoc)), light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);

            poseStack.popPose();
        });
    }
}
