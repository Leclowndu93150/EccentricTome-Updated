package website.eccentric.tome;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;

public class ModName {
    private static final Map<String, String> modNames = new HashMap<>();

    public static final String PATCHOULI = "patchouli";

    static {
        for (var mod : FMLLoader.getCurrent().getLoadingModList().getMods()) {
            modNames.put(mod.getModId(), mod.getDisplayName());
        }
    }

    public static String from(BlockState state) {
        return orAlias(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

//    public static String from(ItemStack stack) {
//        var mod = stack.getItem().getCreatorModId(stack);
////        if (mod.equals(PATCHOULI)) {
////            var tag = stack.getTag();
////            if (tag == null)
////                return PATCHOULI;
////
////            var book = tag.getString(EccentricDataComponents.Patchouli.BOOK);
////            mod = ResourceLocation.parse(book).getNamespace();
////        }
//
//        return orAlias(mod);
//    }

    public static String orAlias(String mod) {
        return EccentricConfig.ALIAS_MAP.getOrDefault(mod, mod);
    }

    public static String name(String mod) {
        return modNames.getOrDefault(mod, mod);
    }
}
