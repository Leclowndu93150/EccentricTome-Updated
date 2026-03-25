package website.eccentric.tome;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import website.eccentric.tome.core.TomeData;
import website.eccentric.tome.network.SelectBookPacket;

public class TomeItem extends Item {
    public TomeItem(Properties props) {
        super(props.stacksTo(1)
                .component(EccentricDataComponents.TOME_DATA, TomeData.EMPTY));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        var tome = context.getItemInHand();
        var mod = ModName.from(context.getLevel().getBlockState(context.getClickedPos()));
        
        TomeData data = tome.getOrDefault(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);
        
        if (!player.isShiftKeyDown() || !data.books().containsKey(mod))
            return InteractionResult.PASS;

        var books = data.books().get(mod);
        if (!books.isEmpty() && context.getLevel().isClientSide()) {
            ClientPacketDistributor.sendToServer(new SelectBookPacket(mod, 0));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var tome = player.getItemInHand(hand);

        if (level.isClientSide()) {
            TomeClientUtils.openTome(tome);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        TomeData data = itemStack.getOrDefault(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);

        for (var entry : data.books().entrySet()) {
            builder.accept(Component.literal(ModName.name(entry.getKey())));

            for (var book : entry.getValue()) {
                if (book.is(Items.AIR))
                    continue;
                var name = book.getHoverName().getString();
                builder.accept(Component.literal("  " + ChatFormatting.GRAY + name));
            }
        }
    }

}
