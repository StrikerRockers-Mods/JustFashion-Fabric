package subaraki.fashion.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.mod.Fashion;
import subaraki.fashion.util.ClientReferences;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientBoundPackets {

    public static final ResourceLocation SET_IN_WARDROBE_TRACKING = new ResourceLocation(Fashion.MODID, "set_in_wardrobe_tracking");
    public static final ResourceLocation SYNC_FASHION_CLIENT = new ResourceLocation(Fashion.MODID, "sync_fashion_client");
    public static final ResourceLocation SYNC_FASHION_TRACKING = new ResourceLocation(Fashion.MODID, "sync_fashion_tracking");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SET_IN_WARDROBE_TRACKING, (client, handler, buf, responseSender) -> {
            UUID sender = buf.readUUID();
            boolean isInWardrobe = buf.readBoolean();
            client.execute(() -> {
                Player player = ClientReferences.getClientPlayerByUUID(sender);
                FashionData.get(player).setInWardrobe(isInWardrobe);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(SYNC_FASHION_TRACKING, (client, handler, buf, responseSender) -> {
            ResourceLocation[] ids = new ResourceLocation[6];
            for (int slot = 0; slot < ids.length; slot++)
                ids[slot] = new ResourceLocation(buf.readUtf(256));

            boolean isActive = buf.readBoolean();
            UUID sender = buf.readUUID();

            int size = buf.readInt();
            List<String> layers = new ArrayList<>();
            if (size > 0)
                for (int i = 0; i < size; i++)
                    layers.add(buf.readUtf(128));
            client.execute(() -> ClientReferencesPacket.handle(ids, isActive, sender, layers));
        });
    }

    public static void syncIsInWardrobeTracking(ServerPlayer player, UUID sender, boolean isInWardrobe) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(sender);
        buf.writeBoolean(isInWardrobe);
        ServerPlayNetworking.send(player, SET_IN_WARDROBE_TRACKING, buf);
    }

    public static void syncFashionTracking(ServerPlayer playerToSendTo, UUID sender, FashionData fashionData) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        for (ResourceLocation resLoc : fashionData.getAllRenderedParts())
            if (resLoc != null)
                buf.writeUtf(resLoc.toString());
            else
                buf.writeUtf("missing");

        buf.writeBoolean(fashionData.shouldRenderFashion());
        buf.writeUUID(sender);
        List<String> layers = fashionData.getKeepLayerNames();
        buf.writeInt(layers == null ? 0 : layers.isEmpty() ? 0 : layers.size());

        if (layers != null && !layers.isEmpty()) {
            for (String layer : layers) {
                buf.writeUtf(layer);
            }
        }
        ServerPlayNetworking.send(playerToSendTo, SYNC_FASHION_TRACKING, buf);
    }

}
