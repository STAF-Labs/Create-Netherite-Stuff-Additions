package ru.lotuze.createnetheritestuffadditions.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.lotuze.createnetheritestuffadditions.CreateNetheriteStuffAdditions;
import ru.lotuze.createnetheritestuffadditions.item.NetheritePortableDrillItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateNetheriteStuffAdditions.MODID);

    public static final DeferredItem<Item> NETHERITE_PORTABLE_DRILL = ITEMS.register(
            "netherite_portable_drill",
            NetheritePortableDrillItem::new);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
