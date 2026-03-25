package website.eccentric.tome.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.*;

public class TomeCore {
    
    private final UUID tomeId;
    private final Map<String, List<StoredBook>> books;
    
    public TomeCore() {
        this.tomeId = UUID.randomUUID();
        this.books = new HashMap<>();
    }
    
    public TomeCore(UUID tomeId, Map<String, List<StoredBook>> books) {
        this.tomeId = tomeId;
        this.books = new HashMap<>(books);
    }
    
    public UUID getId() {
        return tomeId;
    }
    
    public boolean addBook(ItemStack book) {
        if (book.isEmpty()) return false;
        
        String modId;
        try {
            modId = book.getItem().getCreatorModId(null, book);
        } catch (Exception e) {
            modId = BuiltInRegistries.ITEM.getKey(book.getItem()).getNamespace();
        }
        StoredBook storedBook = new StoredBook(book);
        
        List<StoredBook> modBooks = books.computeIfAbsent(modId, k -> new ArrayList<>());
        
        for (StoredBook existing : modBooks) {
            if (existing.matches(book)) {
                return false;
            }
        }
        
        modBooks.add(storedBook);
        return true;
    }
    
    public ItemStack extractBook(String modId, int index) {
        List<StoredBook> modBooks = books.get(modId);
        if (modBooks == null || index < 0 || index >= modBooks.size()) {
            return ItemStack.EMPTY;
        }
        
        StoredBook book = modBooks.remove(index);
        if (modBooks.isEmpty()) {
            books.remove(modId);
        }
        
        return book.toItemStack();
    }
    
    public ItemStack getBook(String modId, int index) {
        List<StoredBook> modBooks = books.get(modId);
        if (modBooks == null || index < 0 || index >= modBooks.size()) {
            return ItemStack.EMPTY;
        }
        
        return modBooks.get(index).toItemStack();
    }
    
    public List<ItemStack> getAllBooks() {
        List<ItemStack> allBooks = new ArrayList<>();
        for (List<StoredBook> modBooks : books.values()) {
            for (StoredBook book : modBooks) {
                allBooks.add(book.toItemStack());
            }
        }
        return allBooks;
    }
    
    public Set<String> getModIds() {
        return new HashSet<>(books.keySet());
    }
    
    public List<StoredBook> getBooksForMod(String modId) {
        return books.getOrDefault(modId, Collections.emptyList());
    }
    
    public int getTotalBookCount() {
        return books.values().stream().mapToInt(List::size).sum();
    }
    
    public boolean isEmpty() {
        return books.isEmpty();
    }
    
    public TomeCore copy() {
        Map<String, List<StoredBook>> copiedBooks = new HashMap<>();
        for (Map.Entry<String, List<StoredBook>> entry : books.entrySet()) {
            copiedBooks.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return new TomeCore(tomeId, copiedBooks);
    }
    
    public static class StoredBook {
        private final Identifier itemId;
        private final ItemStack cachedStack;
        
        public StoredBook(ItemStack stack) {
            this.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            this.cachedStack = stack.copyWithCount(1);
        }
        
        public ItemStack toItemStack() {
            return cachedStack.copy();
        }
        
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) return false;
            Identifier otherId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!itemId.equals(otherId)) return false;
            
            return ItemStack.isSameItemSameComponents(cachedStack, stack);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof StoredBook other)) return false;
            return itemId.equals(other.itemId) && 
                   ItemStack.isSameItemSameComponents(cachedStack, other.cachedStack);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(itemId);
        }
    }
}