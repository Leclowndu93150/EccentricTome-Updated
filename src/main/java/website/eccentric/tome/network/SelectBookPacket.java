package website.eccentric.tome.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import website.eccentric.tome.EccentricTome;
import website.eccentric.tome.core.TomeCore;
import website.eccentric.tome.core.TomeData;
import website.eccentric.tome.core.TomeManager;

public record SelectBookPacket(String modId, int index) implements CustomPacketPayload {
    
    public static final Type<SelectBookPacket> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(EccentricTome.ID, "select_book")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectBookPacket> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
        SelectBookPacket::modId,
        net.minecraft.network.codec.ByteBufCodecs.INT,
        SelectBookPacket::index,
        SelectBookPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SelectBookPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            InteractionHand hand = TomeManager.getTomeHand(player);
            
            if (hand != null) {
                ItemStack tomeStack = player.getItemInHand(hand);
                ItemStack bookStack = TomeManager.selectBook(tomeStack, packet.modId, packet.index);
                
                if (!bookStack.isEmpty()) {
                    player.setItemInHand(hand, bookStack);
                    EccentricTome.LOGGER.debug("Selected book: {} at index {}", packet.modId, packet.index);
                }
            }
        });
    }
}