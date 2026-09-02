package ru.lotuze.createnetheritestuffadditions.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import ru.lotuze.createnetheritestuffadditions.CreateNetheriteStuffAdditions;
import ru.lotuze.createnetheritestuffadditions.item.NetheritePortableDrillItem;
import ru.lotuze.createnetheritestuffadditions.registry.ModItems;

@EventBusSubscriber(modid = CreateNetheriteStuffAdditions.MODID)
public final class ModEvents {
    private ModEvents() {
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack drill = event.getPlayer().getOffhandItem();
        if (!drill.is(ModItems.NETHERITE_PORTABLE_DRILL.get())) {
            return;
        }

        if (NetheritePortableDrillItem.tryRefillFromItem(drill, event.getEntity().getItem(), event.getPlayer())
                && event instanceof ICancellableEvent cancellable) {
            cancellable.setCanceled(true);
        }
    }
}
