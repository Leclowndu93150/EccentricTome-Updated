package website.eccentric.tome.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import website.eccentric.tome.EccentricTome;
import website.eccentric.tome.core.TomeManager;

public record RevertToTomePacket() implements CustomPacketPayload {
    
    public static final RevertToTomePacket INSTANCE = new RevertToTomePacket();
    
    public static final Type<RevertToTomePacket> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(EccentricTome.ID, "revert_to_tome")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, RevertToTomePacket> STREAM_CODEC = 
        StreamCodec.unit(INSTANCE);
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(RevertToTomePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            InteractionHand hand = TomeManager.getBookHand(player);
            
            if (hand != null) {
                ItemStack bookStack = player.getItemInHand(hand);
                ItemStack tomeStack = TomeManager.revertToTome(bookStack);
                
                if (!tomeStack.isEmpty()) {
                    player.setItemInHand(hand, tomeStack);
                    
                    if (context.flow().getReceptionSide().isClient()) {
                        Minecraft.getInstance().gameRenderer.itemInHandRenderer.itemUsed(hand);
                    }
                    
                    EccentricTome.LOGGER.debug("Reverted to tome");
                }
            }
        });
    }
}