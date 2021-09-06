package subaraki.fashion.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.mixin.accessor.ScreenAccessor;
import subaraki.fashion.mod.Fashion;
import subaraki.fashion.mod.FashionClient;
import subaraki.fashion.render.EnumFashionSlot;
import subaraki.fashion.util.ResourcePackReader;

import java.util.Arrays;

public class WardrobeScreen extends Screen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Fashion.MODID, "textures/gui/wardrobe.png");
    private final Player player = Minecraft.getInstance().player;
    protected int xSize = 176;
    protected int ySize = 166;
    protected int guiLeft;
    protected int guiTop;
    private float oldMouseX;
    private float oldMouseY;
    private int id = 0;

    public WardrobeScreen() {
        super(new TextComponent("fashion.wardrobe"));
    }

    public static void renderEntityInInventory(int x, int y, int scale, float mouseX, float mouseY, LivingEntity player, float rotateY, float offsetZ) {
        float flipScale = offsetZ < 0 ? -1.0f : 1.0f;
        float lookX = (float) Math.atan(mouseX / 40.0F) * flipScale;
        float lookY = (float) Math.atan(mouseY / 40.0F);
        PoseStack poseStackGeneral = RenderSystem.getModelViewStack();
        poseStackGeneral.pushPose();
        poseStackGeneral.translate(x, y, 1050.0D + offsetZ);
        poseStackGeneral.scale(flipScale, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(scale, scale, scale);
        Quaternion rotationZ = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion rotationX = Vector3f.XP.rotationDegrees(lookY * 20.0F);
        // rotationZ.mul(rotationX);
        poseStack.mulPose(rotationZ);
        float var12 = player.yBodyRot;
        float var13 = player.getYRot();
        float var14 = player.getXRot();
        float var15 = player.yHeadRotO;
        float var16 = player.yHeadRot;
        player.yBodyRot = 180.0F + lookX * 20.0F;
        player.setYRot(180.0F + lookX * 40.0F);
        player.setXRot(-lookY * 20.0F);
        player.yHeadRot = player.getYRot();
        player.yHeadRotO = player.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher var17 = Minecraft.getInstance().getEntityRenderDispatcher();
        rotationX.conj();
        var17.overrideCameraOrientation(rotationX);
        var17.setRenderShadow(false);
        MultiBufferSource.BufferSource var18 = Minecraft.getInstance().renderBuffers().bufferSource();
        poseStack.mulPose(new Quaternion(0, rotateY, 0, true));
        RenderSystem.runAsFancy(() -> var17.render(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, var18, 15728880));
        var18.endBatch();
        var17.setRenderShadow(true);
        player.yBodyRot = var12;
        player.setYRot(var13);
        player.setXRot(var14);
        player.yHeadRotO = var15;
        player.yHeadRot = var16;
        poseStackGeneral.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    @Override
    protected void init() {

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Arrays.stream(EnumFashionSlot.values()).forEach(t -> {
            addSlotButton(t);
            addSlotButton(t);
        });

        id = 0; // reset id to 0 in case of resizing ! else it will add up and up and offset y
        // to infinity
        InventoryScreen.renderEntityInInventory(guiLeft + 33, guiTop + 65, 25, -(guiLeft - 70 - oldMouseX) / 2.5f, guiTop + 40 - oldMouseY,
                player);
        // TODO hacky way of letting vanilla layers show up on first opening of gui
        // FIXME
        FashionData fashion = FashionData.get(player);
        // toggle buttons for each render layer that exists in the game for players,
        // both from mods and vanilla
        if (!fashion.getOtherModLayersList().isEmpty())
            for (int i = 0; i < fashion.getOtherModLayersList().size(); i++) {
                RenderLayer<?, ?> layer = fashion.getOtherModLayersList().get(i);
                boolean isActive = fashion.getLayersToKeep().contains(layer);
                String classname = layer.getClass().getSimpleName();
                this.addRenderableWidget(new FancyButton(guiLeft - 12 - (i % 5) * 10, guiTop + 6 + (i / 5) * 10, classname,
                        b -> toggleLayer((FancyButton) b, classname)).setActive(isActive));
            }

        // toggle button, with the explicit press ID of 12 (could be anything at this
        // point, it's an artifact for pre 1.14
        this.addRenderableWidget(new FancyButton(guiLeft + 8, guiTop + ySize / 2 + 14, c -> pressToggle((FancyButton) c)).setActive(fashion.shouldRenderFashion()));
    }

    private void addSlotButton(EnumFashionSlot slot) {

        int offset = 0;
        if (slot.ordinal() > 3)
            offset = 10;

        boolean back = id % 2 == 0;
        Button b = new Button(guiLeft + 105 + (back ? 0 : 55), guiTop + 10 + offset + (id / 2 * 15), 10, 10, new TextComponent(back ? "<" : ">"),
                c -> cycle(back, slot));
        this.addRenderableWidget(b);
        id++;
    }

    @Override
    public void render(PoseStack mat, int mouseX, int mouseY, float partialTicks) {

        this.drawGuiContainerBackgroundLayer(mat, partialTicks, mouseX, mouseY);

        RenderSystem.disableDepthTest();
        super.render(mat, mouseX, mouseY, partialTicks);

        mat.pushPose();

        this.drawGuiContainerForegroundLayer(mat, mouseX, mouseY);

        mat.popPose();

        RenderSystem.enableDepthTest();

    }

    protected void drawGuiContainerBackgroundLayer(PoseStack mat, float partialTicks, int mouseX, int mouseY) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        blit(mat, guiLeft + 14, guiTop + 7, xSize, 0, 38, 62);

        FashionData.get(player).setInWardrobe(false); // disable for in gui rendering
        mat.pushPose();

        float rotationMirrorBack = Fashion.config.face_mirror ? 0.0F : -150F;
        float rotationMirrorFront = Fashion.config.face_mirror ? 180.0F : 0.0F;

        float flipMouse = Fashion.config.face_mirror ? -1f : 1f;

        float scale = Fashion.config.bigger_model ? 1.3f : 1.1f;

        int offsetBigModel = (Fashion.config.bigger_model ? 5 : 0);

        mat.pushPose();
        renderEntityInInventory(guiLeft + 33, guiTop + 65 + offsetBigModel, (int) (25f * scale),
                ((guiLeft + 70 - oldMouseX) / 2.5f) * -flipMouse, guiTop + 40 - oldMouseY, player, rotationMirrorBack, -200f);
        mat.popPose();

        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        blit(mat, guiLeft, guiTop, 0, 0, xSize, ySize);
        RenderSystem.disableBlend();

        mat.pushPose();
        renderEntityInInventory((int) (guiLeft + 68f), (int) (guiTop + 82f), (int) (30f * scale),
                ((guiLeft + 70 - oldMouseX) / 2.5f) * flipMouse, guiTop + 40 - oldMouseY, player, rotationMirrorFront, 200f);
        mat.popPose();

        mat.popPose();

        FashionData.get(player).setInWardrobe(true); // set back after drawn for in world rendering
    }

    protected void drawGuiContainerForegroundLayer(PoseStack mat, int mouseX, int mouseY) {

        FashionData fashion = FashionData.get(player);
        int id = 0;
        for (EnumFashionSlot slot : EnumFashionSlot.values()) {
            ResourceLocation resLoc = fashion.getRenderingPart(slot);
            String[] s = resLoc.getPath().split("/");
            String name = null;
            if (s.length > 0)
                name = s[s.length - 1].split("\\.")[0];

            if (name == null)
                name = "no model";

            if (name.contains("blank") || name.contains("missing"))
                name = "N/A";

            int offset = 0;
            if (id > 3)
                offset = 10;
            minecraft.font.draw(mat, name, guiLeft + 138 - minecraft.font.width(name) / 2,
                    guiTop + ((id++ + 1) * 15) - 3 + offset, 0xffffff);
        }

        String toggled = fashion.shouldRenderFashion() ? "Showing Fashion" : "Showing Armor";
        minecraft.font.draw(mat, toggled, guiLeft + xSize / 2 - 68, guiTop + ySize / 2 + 14, 0xffffff);

        // tracking player view !!
        this.oldMouseX = mouseX;
        this.oldMouseY = Math.min((float) mouseY, guiTop + 50);

        for (Widget guiButton : ((ScreenAccessor) this).getRenderables()) {
            if (guiButton instanceof FancyButton fancyButton) {
                if (fancyButton.isMouseOver(mouseX, mouseY) && !fancyButton.isSwitch()) {
                    HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(fancyButton.name));
                    Style style = Style.EMPTY.withHoverEvent(hover);
                    this.renderComponentHoverEffect(mat, style, mouseX, mouseY);
                }
            }
        }
    }

    // called whenever a screen is closed, by player or by opening another screen or
    // trough force (to far away)

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // needed to trigger onClose and packets on pressing escape button
    }

    @Override
    public void removed() {
        FashionData.get(player).setInWardrobe(false);
    }

    @Override
    public boolean keyPressed(int keysym, int scancode, int keyPressed) {

        if (FashionClient.keyWardrobe.matches(keysym, scancode)) {
            this.minecraft.player.closeContainer();
            onClose();
            removed();
            return true;
        }

        return super.keyPressed(keysym, scancode, keyPressed);
    }

    private void cycle(boolean back, EnumFashionSlot slot) {
        FashionData fashion = FashionData.get(player);
        if (fashion.shouldRenderFashion()) {

            ResourceLocation name = fashion.getRenderingPart(slot);
            ResourceLocation newPart = back ? ResourcePackReader.getPreviousClothes(slot, name) : ResourcePackReader.getNextClothes(slot, name);

            fashion.updateFashionSlot(newPart, slot);
        }
    }

    private void pressToggle(FancyButton button) {
        button.toggle();
        FashionData.get(player).setRenderFashion(button.isActive());
    }

    private void toggleLayer(FancyButton button, String layerName) {

        button.toggle(); // set opposite of current state

        FashionData fashion = FashionData.get(player);
        if (button.isActive())// if set to active
            fashion.addLayerToKeep(layerName);
        else
            fashion.removeLayerFromKeep(layerName);

        fashion.fashionLayers.clear();
    }

    @Override
    public boolean isPauseScreen() {

        return false;
    }
}
