package ru.lotuze.createnetheritestuffadditions.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.lotuze.createnetheritestuffadditions.CreateNetheriteStuffAdditions;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            CreateNetheriteStuffAdditions.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CreateNetheriteStuffAdditions.MODID))
                    .icon(() -> new ItemStack(ModItems.NETHERITE_PORTABLE_DRILL.get()))
                    .displayItems((parameters, output) -> output.accept(ModItems.NETHERITE_PORTABLE_DRILL.get()))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
