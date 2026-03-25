package website.eccentric.tome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import website.eccentric.tome.core.TomeManager;

public class AttachmentRecipe extends CustomRecipe {
    private static final AttachmentRecipe INSTANCE = new AttachmentRecipe();
    public static final MapCodec<AttachmentRecipe> CODEC = MapCodec.unit(AttachmentRecipe.INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachmentRecipe> STREAM_CODEC = StreamCodec.unit(AttachmentRecipe.INSTANCE);

    @Override
    public boolean matches(CraftingInput crafting, Level level) {
        var foundTome = false;
        var foundTarget = false;

        for (var i = 0; i < crafting.size(); i++) {
            var stack = crafting.getItem(i);

            if (stack.isEmpty())
                continue;

            var item = stack.getItem();
            if (item instanceof BlockItem) {
                return false;
            }
            if (item instanceof TomeItem) {
                if (foundTome)
                    return false;
                foundTome = true;
            } else if (isTarget(stack)) {
                if (foundTarget)
                    return false;
                foundTarget = true;
            } else
                return false;
        }

        return foundTome && foundTarget;
    }

    @Override
    public ItemStack assemble(CraftingInput crafting) {
        var tome = ItemStack.EMPTY;
        var target = ItemStack.EMPTY;

        for (var i = 0; i < crafting.size(); i++) {
            var stack = crafting.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.getItem() instanceof TomeItem)
                tome = stack;
            else
                target = stack;
        }

        return TomeManager.addBookToTome(tome.copy(), target);
    }

    public boolean isTarget(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        var location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var locationString = location.toString();
        var locationDamage = locationString + ":" + stack.getDamageValue();

        var items = EccentricConfig.getWhitelistedItems();
        return location.getNamespace().equals(ModName.PATCHOULI) || location.getNamespace().equals("modonomicon") ||  items.contains(locationString) || items.contains(locationDamage);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput crafting) {
        return NonNullList.withSize(crafting.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return EccentricTome.ATTACHMENT.get();
    }


}