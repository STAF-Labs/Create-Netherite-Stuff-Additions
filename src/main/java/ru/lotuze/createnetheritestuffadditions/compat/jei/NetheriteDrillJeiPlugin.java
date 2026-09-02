package ru.lotuze.createnetheritestuffadditions.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.lotuze.createnetheritestuffadditions.CreateNetheriteStuffAdditions;
import ru.lotuze.createnetheritestuffadditions.registry.ModItems;

@JeiPlugin
public class NetheriteDrillJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            CreateNetheriteStuffAdditions.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                ModItems.NETHERITE_PORTABLE_DRILL.get(),
                Component.translatable("item_desc.portable_drill.desc"));
    }
}
