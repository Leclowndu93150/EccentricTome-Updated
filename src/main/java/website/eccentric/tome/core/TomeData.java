package website.eccentric.tome.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record TomeData(UUID tomeId, Map<String, List<ItemStack>> books) implements TooltipProvider {
    
    public static final TomeData EMPTY = new TomeData(UUID.randomUUID(), new HashMap<>());
    
    public static final Codec<TomeData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("tome_id").forGetter(TomeData::tomeId),
            Codec.unboundedMap(Codec.STRING, ItemStack.CODEC.listOf()).fieldOf("books").forGetter(TomeData::books)
        ).apply(instance, TomeData::new)
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, TomeData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
        TomeData::tomeId,
        ByteBufCodecs.map(
            HashMap::new,
            ByteBufCodecs.STRING_UTF8,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            256
        ),
        TomeData::books,
        TomeData::new
    );
    
    public static TomeData fromCore(TomeCore core) {
        Map<String, List<ItemStack>> bookMap = new HashMap<>();
        for (String modId : core.getModIds()) {
            List<ItemStack> modBooks = core.getBooksForMod(modId).stream()
                .map(TomeCore.StoredBook::toItemStack)
                .collect(Collectors.toList());
            bookMap.put(modId, modBooks);
        }
        return new TomeData(core.getId(), bookMap);
    }
    
    public TomeCore toCore() {
        Map<String, List<TomeCore.StoredBook>> coreBooks = new HashMap<>();
        for (Map.Entry<String, List<ItemStack>> entry : books.entrySet()) {
            List<TomeCore.StoredBook> modBooks = entry.getValue().stream()
                .map(TomeCore.StoredBook::new)
                .collect(Collectors.toList());
            coreBooks.put(entry.getKey(), modBooks);
        }
        return new TomeCore(tomeId, coreBooks);
    }
    
    public TomeData withoutBook(String modId, int index) {
        TomeCore core = toCore();
        core.extractBook(modId, index);
        return fromCore(core);
    }
    
    public TomeData withBook(ItemStack book) {
        TomeCore core = toCore();
        core.addBook(book);
        return fromCore(core);
    }
    
    public int getTotalBookCount() {
        return books.values().stream().mapToInt(List::size).sum();
    }
    
    public boolean isEmpty() {
        return books.isEmpty();
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        consumer.accept(Component.literal("Hiii"));
    }
}