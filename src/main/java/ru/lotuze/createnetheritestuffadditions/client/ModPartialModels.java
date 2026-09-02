package ru.lotuze.createnetheritestuffadditions.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import ru.lotuze.createnetheritestuffadditions.CreateNetheriteStuffAdditions;

@EventBusSubscriber(modid = CreateNetheriteStuffAdditions.MODID, value = Dist.CLIENT)
public final class ModPartialModels {
    public static final PartialModel NETHERITE_PORTABLE_DRILL_COG = partial("item_renderer/netherite_portable_drill_cog");
    public static final PartialModel NETHERITE_PORTABLE_DRILL_HEAD = partial("item_renderer/netherite_portable_drill_head");

    private ModPartialModels() {
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(NETHERITE_PORTABLE_DRILL_COG.modelLocation()));
        event.register(ModelResourceLocation.standalone(NETHERITE_PORTABLE_DRILL_HEAD.modelLocation()));
    }

    private static PartialModel partial(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateNetheriteStuffAdditions.MODID, path));
    }
}
