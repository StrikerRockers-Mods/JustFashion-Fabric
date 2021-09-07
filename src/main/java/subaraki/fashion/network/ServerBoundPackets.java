package subaraki.fashion.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.mod.Fashion;
import subaraki.fashion.render.EnumFashionSlot;

import java.util.ArrayList;
import java.util.List;

public class ServerBoundPackets {

    public static final ResourceLocation SYNC_FASHION = new ResourceLocation(Fashion.MODID, "sync_fashion_server");
    public static final ResourceLocation SET_IN_WARDROBE = new ResourceLocation(Fashion.MODID, "set_in_wardrobe");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SYNC_FASHION, (server, player, handler, buf, responseSender) -> {
            ResourceLocation[] ids = new ResourceLocation[6];
            for (int slot = 0; slot < ids.length; slot++)
                ids[slot] = new ResourceLocation(buf.readUtf());

            boolean isActive = buf.readBoolean();

            int size = buf.readInt();
            List<String> layers = new ArrayList<>();
            if (size > 0) {
                for (int i = 0; i < size; i++)
                    layers.add(buf.readUtf());
            }
            server.execute(() -> {
                FashionData fashion = FashionData.get(player);

                for (EnumFashionSlot slot : EnumFashionSlot.values())
                    fashion.updateFashionSlot(ids[slot.ordinal()], slot);

                fashion.setRenderFashion(isActive);

                fashion.setInWardrobe(false);

                //server sided keep layer list
                fashion.getKeepLayerNames().clear();
                fashion.addLayersToKeep(layers);
                for (ServerPlayer serverPlayer : PlayerLookup.tracking(player)) {
                    ClientBoundPackets.syncIsInWardrobeTracking(serverPlayer, player.getUUID(), false);
                    ClientBoundPackets.syncFashionTracking(serverPlayer, player.getUUID(), fashion);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_IN_WARDROBE, (server, player, handler, buf, responseSender) -> {
            boolean isInWardrobe = buf.readBoolean();
            server.execute(() -> {
                FashionData data = FashionData.get(player);
                data.setInWardrobe(isInWardrobe);
                for (ServerPlayer serverPlayer : PlayerLookup.tracking(player)) {
                    ClientBoundPackets.syncIsInWardrobeTracking(serverPlayer, player.getUUID(), isInWardrobe);
                }
            });
        });
    }

    public static void syncFashionToServer(FashionData fashion) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        for (ResourceLocation resLoc : fashion.getAllRenderedParts())
            if (resLoc != null)
                buf.writeUtf(resLoc.toString());
            else
                buf.writeUtf("missing");
        buf.writeBoolean(fashion.shouldRenderFashion());
        List<String> layers = fashion.getKeepLayerNames();
        buf.writeInt(layers == null ? 0 : layers.isEmpty() ? 0 : layers.size());

        if (layers != null && !layers.isEmpty()) {
            for (String layer : layers) {
                buf.writeUtf(layer);
            }
        }
        ClientPlayNetworking.send(SYNC_FASHION, buf);
    }

    public static void syncInWardrobe(boolean isInWardrobe) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isInWardrobe);
        ClientPlayNetworking.send(SET_IN_WARDROBE, buf);
    }
}
