package website.eccentric.tome.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import website.eccentric.tome.EccentricDataComponents;
import website.eccentric.tome.EccentricTome;
import website.eccentric.tome.TomeItem;

import javax.annotation.Nullable;

public class TomeManager {
    
    private static final String ACTIVE_BOOK_MOD = "active_book_mod";
    private static final String ACTIVE_BOOK_INDEX = "active_book_index";
    
    public static ItemStack createTome() {
        ItemStack tome = new ItemStack(EccentricTome.TOME.get());
        tome.set(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);
        return tome;
    }
    
    public static ItemStack addBookToTome(ItemStack tome, ItemStack book) {
        if (!isTome(tome) || book.isEmpty()) {
            return tome;
        }
        
        TomeData data = tome.getOrDefault(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);
        TomeData newData = data.withBook(book);
        
        if (newData.equals(data)) {
            EccentricTome.LOGGER.debug("Book already exists in tome");
            return tome;
        }
        
        ItemStack result = tome.copy();
        result.set(EccentricDataComponents.TOME_DATA.get(), newData);
        return result;
    }
    
    public static ItemStack selectBook(ItemStack tome, String modId, int index) {
        if (!isTome(tome)) {
            return ItemStack.EMPTY;
        }
        
        TomeData data = tome.getOrDefault(EccentricDataComponents.TOME_DATA.get(), TomeData.EMPTY);
        TomeCore core = data.toCore();
        ItemStack book = core.getBook(modId, index);
        
        if (book.isEmpty()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack activeBook = book.copy();
        activeBook.set(EccentricDataComponents.TOME_DATA.get(), data);
        activeBook.set(EccentricDataComponents.IS_ACTIVE_BOOK.get(), true);
        activeBook.set(EccentricDataComponents.ACTIVE_MOD.get(), modId);
        activeBook.set(EccentricDataComponents.ACTIVE_INDEX.get(), index);
        
        setBookDisplayName(activeBook);
        
        return activeBook;
    }
    
    public static ItemStack revertToTome(ItemStack book) {
        if (!isActiveBook(book)) {
            return ItemStack.EMPTY;
        }
        
        TomeData data = book.get(EccentricDataComponents.TOME_DATA.get());
        if (data == null) {
            return ItemStack.EMPTY;
        }
        
        ItemStack tome = createTome();
        tome.set(EccentricDataComponents.TOME_DATA.get(), data);
        return tome;
    }
    
    public static ItemStack extractBook(ItemStack activeBook, boolean keepTome) {
        if (!isActiveBook(activeBook)) {
            return ItemStack.EMPTY;
        }
        
        TomeData data = activeBook.get(EccentricDataComponents.TOME_DATA.get());
        String modId = activeBook.get(EccentricDataComponents.ACTIVE_MOD.get());
        Integer index = activeBook.get(EccentricDataComponents.ACTIVE_INDEX.get());
        
        if (data == null || modId == null || index == null) {
            return ItemStack.EMPTY;
        }
        
        TomeData updatedData = data.withoutBook(modId, index);
        
        ItemStack cleanBook = activeBook.copy();
        cleanBook.remove(EccentricDataComponents.TOME_DATA.get());
        cleanBook.remove(EccentricDataComponents.IS_ACTIVE_BOOK.get());
        cleanBook.remove(EccentricDataComponents.ACTIVE_MOD.get());
        cleanBook.remove(EccentricDataComponents.ACTIVE_INDEX.get());
        cleanBook.remove(DataComponents.CUSTOM_NAME);
        
        if (keepTome) {
            ItemStack tome = createTome();
            tome.set(EccentricDataComponents.TOME_DATA.get(), updatedData);
            return tome;
        }
        
        return cleanBook;
    }
    
    public static boolean isTome(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof TomeItem;
    }
    
    public static boolean isActiveBook(ItemStack stack) {
        return !stack.isEmpty() && Boolean.TRUE.equals(stack.get(EccentricDataComponents.IS_ACTIVE_BOOK.get()));
    }
    
    @Nullable
    public static InteractionHand getTomeHand(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (isTome(mainHand)) {
            return InteractionHand.MAIN_HAND;
        }
        
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (isTome(offHand)) {
            return InteractionHand.OFF_HAND;
        }
        
        return null;
    }
    
    @Nullable
    public static InteractionHand getBookHand(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (isActiveBook(mainHand)) {
            return InteractionHand.MAIN_HAND;
        }
        
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (isActiveBook(offHand)) {
            return InteractionHand.OFF_HAND;
        }
        
        return null;
    }
    
    private static void setBookDisplayName(ItemStack book) {
        Component name = book.getHoverName().copy().withStyle(ChatFormatting.GREEN);
        book.set(DataComponents.CUSTOM_NAME, Component.translatable("eccentrictome.name", name));
    }
}