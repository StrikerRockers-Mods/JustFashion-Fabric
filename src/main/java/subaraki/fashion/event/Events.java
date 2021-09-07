package subaraki.fashion.event;

import dev.onyxstudios.cca.api.v3.entity.TrackingStartCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import subaraki.fashion.capability.FashionData;
import subaraki.fashion.network.ClientBoundPackets;

public class Events {
    public static void register() {
        TrackingStartCallback.EVENT.register((player, entity) -> {
            if (entity instanceof Player target && player != null && !player.level.isClientSide()) {
                FashionData data = FashionData.get(target);
                for (ServerPlayer serverPlayer : PlayerLookup.tracking(target)) {
                    ClientBoundPackets.syncIsInWardrobeTracking(serverPlayer, target.getUUID(), data.isInWardrobe());
                    ClientBoundPackets.syncFashionTracking(serverPlayer, target.getUUID(), data);
                }
            }
        });
    }
}
