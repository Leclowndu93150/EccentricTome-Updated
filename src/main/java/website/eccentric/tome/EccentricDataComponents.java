package website.eccentric.tome;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import website.eccentric.tome.core.TomeData;

public class EccentricDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, EccentricTome.ID);

    public static final Supplier<DataComponentType<TomeData>> TOME_DATA =
            registerDataComponentType("tome_data", () -> builder -> builder
                    .persistent(TomeData.CODEC)
                    .networkSynchronized(TomeData.STREAM_CODEC)
            );
    
    public static final Supplier<DataComponentType<Boolean>> IS_ACTIVE_BOOK =
            registerDataComponentType("is_active_book", () -> builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
            );
    
    public static final Supplier<DataComponentType<String>> ACTIVE_MOD =
            registerDataComponentType("active_mod", () -> builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            );
    
    public static final Supplier<DataComponentType<Integer>> ACTIVE_INDEX =
            registerDataComponentType("active_index", () -> builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
            );

    public static <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String name, Supplier<UnaryOperator<DataComponentType.Builder<T>>> builderOperator) {
        return COMPONENTS.register(name, () -> builderOperator.get().apply(DataComponentType.builder()).build());
    }
}
