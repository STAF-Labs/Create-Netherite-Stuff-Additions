package ru.lotuze.createnetheritestuffadditions.item;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import ru.lotuze.createnetheritestuffadditions.client.NetheritePortableDrillItemRenderer;

public class NetheritePortableDrillItem extends TieredItem {
    public static final int ORIGINAL_DISPLAY_CAPACITY = 1600;
    public static final int DISPLAY_CAPACITY = 1680;
    public static final int TANK_CAPACITY = DISPLAY_CAPACITY * 10;
    public static final int RESOURCE_PER_BLOCK = 10;
    public static final float UNPOWERED_MINING_SPEED = 9.5F;
    public static final float POWERED_MINING_SPEED = 30.0F;
    public static final TagKey<Fluid> FUEL_FLUIDS = FluidTags.LAVA;
    public static final TagKey<Fluid> WATER_FLUIDS = FluidTags.WATER;

    public NetheritePortableDrillItem() {
        super(Tiers.NETHERITE, new Item.Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .stacksTo(1)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 6.0D, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.8D, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build()));
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !state.is(Tiers.NETHERITE.getIncorrectBlocksForDrops());
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            return 1.0F;
        }
        return DualTankFluidHandler.hasFuelAndWater(stack, RESOURCE_PER_BLOCK) ? POWERED_MINING_SPEED : UNPOWERED_MINING_SPEED;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, net.minecraft.core.BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            DualTankFluidHandler.drainFuelAndWater(stack, RESOURCE_PER_BLOCK);
        }

        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack drill = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack refill = player.getItemInHand(otherHand);

        if (!level.isClientSide && tryRefillFromItem(drill, refill, player)) {
            return InteractionResultHolder.success(drill);
        }

        return InteractionResultHolder.pass(drill);
    }

    public static boolean tryRefillFromItem(ItemStack drill, ItemStack refill, Player player) {
        if (drill.isEmpty() || !(drill.getItem() instanceof NetheritePortableDrillItem) || refill.isEmpty()) {
            return false;
        }

        if (refill.is(Items.WATER_BUCKET)) {
            return fillAndConsume(drill, refill, player, new ItemStack(Items.BUCKET), new FluidStack(Fluids.WATER, 1000));
        }
        if (refill.is(Items.POTION)) {
            return fillAndConsume(drill, refill, player, new ItemStack(Items.GLASS_BOTTLE), new FluidStack(Fluids.WATER, 250));
        }
        if (refill.is(Blocks.WET_SPONGE.asItem())) {
            return fillAndConsume(drill, refill, player, new ItemStack(Blocks.SPONGE), new FluidStack(Fluids.WATER, 500));
        }

        int burnTime = refill.getBurnTime(null);
        if (burnTime > 1) {
            int fuelAmount = 10 * Math.round((float) (burnTime * 0.005D));
            return fillAndConsume(drill, refill, player, refill.is(Items.LAVA_BUCKET) ? new ItemStack(Items.BUCKET) : ItemStack.EMPTY,
                    new FluidStack(Fluids.LAVA, fuelAmount));
        }

        return false;
    }

    private static boolean fillAndConsume(ItemStack drill, ItemStack refill, Player player, ItemStack remainder, FluidStack fluid) {
        IFluidHandlerItem handler = drill.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return false;
        }

        int filled = handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return false;
        }

        if (!player.isCreative()) {
            refill.shrink(1);
            if (!remainder.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, remainder);
            }
        }
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return 0;
        }
        int fuel = handler.getFluidInTank(0).getAmount();
        int water = handler.getFluidInTank(1).getAmount();
        return Math.round(13.0F * Math.min(fuel, water) / TANK_CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x6D5A8A;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        int fuel = handler == null ? 0 : Math.round(handler.getFluidInTank(0).getAmount() * 0.1F);
        int water = handler == null ? 0 : Math.round(handler.getFluidInTank(1).getAmount() * 0.1F);

        tooltip.add(Component.translatable("item_desc.holdshift"));
        tooltip.add(Component.literal(Component.translatable("item_desc.fuel").getString() + fuel + "/" + DISPLAY_CAPACITY));
        tooltip.add(Component.literal(Component.translatable("item_desc.water").getString() + water + "/" + DISPLAY_CAPACITY));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item_desc.portable_drill.desc"));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new NetheritePortableDrillItemRenderer()));
    }
}
