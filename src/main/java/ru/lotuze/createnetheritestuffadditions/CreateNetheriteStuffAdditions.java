package ru.lotuze.createnetheritestuffadditions;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import ru.lotuze.createnetheritestuffadditions.item.DualTankFluidHandler;
import ru.lotuze.createnetheritestuffadditions.item.NetheritePortableDrillItem;
import ru.lotuze.createnetheritestuffadditions.registry.ModCreativeTabs;
import ru.lotuze.createnetheritestuffadditions.registry.ModItems;

@Mod(CreateNetheriteStuffAdditions.MODID)
public class CreateNetheriteStuffAdditions {
    public static final String MODID = "create_netherite_stuff_additions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateNetheriteStuffAdditions(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new DualTankFluidHandler(
                        stack,
                        NetheritePortableDrillItem.TANK_CAPACITY,
                        NetheritePortableDrillItem.FUEL_FLUIDS,
                        NetheritePortableDrillItem.WATER_FLUIDS),
                ModItems.NETHERITE_PORTABLE_DRILL.get());
    }
}
