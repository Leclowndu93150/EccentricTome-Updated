package website.eccentric.tome;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import website.eccentric.tome.core.TomeManager;
import website.eccentric.tome.network.RevertToTomePacket;

import java.util.function.Supplier;

@Mod(EccentricTome.ID)
public class EccentricTome {
    public static final String ID = "eccentrictome";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EccentricTome.ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, ID);

    public static final Supplier<RecipeSerializer<? extends CustomRecipe>> ATTACHMENT = RECIPES.register("attachment",
            () -> new RecipeSerializer<>(AttachmentRecipe.CODEC, AttachmentRecipe.STREAM_CODEC));
    public static final DeferredItem<Item> TOME = ITEMS.registerItem("tome", TomeItem::new);

    public EccentricTome(IEventBus modEvent, ModContainer container) {
        ITEMS.register(modEvent);
        RECIPES.register(modEvent);
        EccentricDataComponents.COMPONENTS.register(modEvent);

        modEvent.addListener(this::onClientSetup);
        modEvent.addListener(this::onModConfig);
        modEvent.addListener(this::onBuildCreativeModeTabContents);
        container.registerConfig(ModConfig.Type.COMMON, EccentricConfig.SPEC);

        var minecraftEvent = NeoForge.EVENT_BUS;
        minecraftEvent.addListener(EventPriority.LOW, this::onItemDropped);
        minecraftEvent.addListener(this::onLeftClickEmpty);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
    }

    private void onModConfig(ModConfigEvent event) {
        EccentricConfig.ALIAS_MAP.clear();
        for (var alias : EccentricConfig.getAliases()) {
            var tokens = alias.split("=");
            EccentricConfig.ALIAS_MAP.put(tokens[0], tokens[1]);
        }
    }

    private void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var stack = event.getItemStack();
        if (TomeManager.isActiveBook(stack)) {
            ClientPacketDistributor.sendToServer(RevertToTomePacket.INSTANCE);
        }
    }

    private void onItemDropped(ItemTossEvent event) {
        if (!event.getPlayer().isShiftKeyDown())
            return;

        var entity = event.getEntity();
        var stack = entity.getItem();
        
        if (TomeManager.isActiveBook(stack)) {
            var level = entity.level();
            
            if (!level.isClientSide()) {
                ItemStack extractedBook = TomeManager.extractBook(stack, false);
                ItemStack tome = TomeManager.extractBook(stack, true);
                
                if (!extractedBook.isEmpty() && !tome.isEmpty()) {
                    entity.setItem(extractedBook);
                    level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), tome));
                }
            }
        }
    }

    private void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(TOME);
        }
    }
}
