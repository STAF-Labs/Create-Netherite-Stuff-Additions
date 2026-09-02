package ru.lotuze.createnetheritestuffadditions.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class DualTankFluidHandler implements IFluidHandlerItem {
    private static final String TAG_FLUID = "Fluid";
    private static final String TAG_AMOUNT = "Amount";

    private final ItemStack container;
    private final int capacity;
    private final TagKey<Fluid> fuelTag;
    private final TagKey<Fluid> waterTag;

    public DualTankFluidHandler(ItemStack container, int capacity, TagKey<Fluid> fuelTag, TagKey<Fluid> waterTag) {
        this.container = container;
        this.capacity = capacity;
        this.fuelTag = fuelTag;
        this.waterTag = waterTag;
    }

    public static boolean hasFuelAndWater(ItemStack stack, int amount) {
        IFluidHandlerItem handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        return handler != null
                && handler.getFluidInTank(0).getAmount() >= amount
                && handler.getFluidInTank(1).getAmount() >= amount;
    }

    public static void drainFuelAndWater(ItemStack stack, int amount) {
        IFluidHandlerItem handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return;
        }
        handler.drain(new FluidStack(handler.getFluidInTank(0).getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
        handler.drain(new FluidStack(handler.getFluidInTank(1).getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
    }

    private String fluidKey(int tank) {
        return TAG_FLUID + tank;
    }

    private String amountKey(int tank) {
        return TAG_AMOUNT + tank;
    }

    private CompoundTag copyData() {
        return container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private FluidStack getStoredFluid(int tank) {
        CompoundTag tag = copyData();
        String fluidKey = fluidKey(tank);
        String amountKey = amountKey(tank);
        if (!tag.contains(fluidKey)) {
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString(fluidKey)));
        int amount = tag.getInt(amountKey);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    private void setStoredFluid(int tank, FluidStack fluidStack) {
        CompoundTag tag = copyData();
        String fluidKey = fluidKey(tank);
        String amountKey = amountKey(tank);

        if (fluidStack.isEmpty()) {
            tag.remove(fluidKey);
            tag.remove(amountKey);
        } else {
            tag.putString(fluidKey, BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString());
            tag.putInt(amountKey, fluidStack.getAmount());
        }

        container.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank < 0 || tank >= getTanks()) {
            return FluidStack.EMPTY;
        }
        return getStoredFluid(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank >= 0 && tank < getTanks() ? capacity : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank < 0 || tank >= getTanks() || stack.isEmpty()) {
            return false;
        }
        return tank == 0 ? stack.getFluid().is(fuelTag) : stack.getFluid().is(waterTag);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }

        for (int tank = 0; tank < getTanks(); tank++) {
            if (!isFluidValid(tank, resource)) {
                continue;
            }

            FluidStack stored = getStoredFluid(tank);
            if (!stored.isEmpty() && !stored.isFluidEqual(resource)) {
                continue;
            }

            int filled = Math.min(capacity - stored.getAmount(), resource.getAmount());
            if (filled <= 0) {
                return 0;
            }

            if (action.execute()) {
                setStoredFluid(tank, new FluidStack(resource.getFluid(), stored.getAmount() + filled));
            }
            return filled;
        }

        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        for (int tank = 0; tank < getTanks(); tank++) {
            FluidStack stored = getStoredFluid(tank);
            if (stored.isEmpty() || !stored.isFluidEqual(resource)) {
                continue;
            }

            int drained = Math.min(resource.getAmount(), stored.getAmount());
            FluidStack result = new FluidStack(stored.getFluid(), drained);
            if (action.execute()) {
                int remaining = stored.getAmount() - drained;
                setStoredFluid(tank, remaining <= 0 ? FluidStack.EMPTY : new FluidStack(stored.getFluid(), remaining));
            }
            return result;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        for (int tank = 0; tank < getTanks(); tank++) {
            FluidStack stored = getStoredFluid(tank);
            if (stored.isEmpty()) {
                continue;
            }

            int drained = Math.min(maxDrain, stored.getAmount());
            FluidStack result = new FluidStack(stored.getFluid(), drained);
            if (action.execute()) {
                int remaining = stored.getAmount() - drained;
                setStoredFluid(tank, remaining <= 0 ? FluidStack.EMPTY : new FluidStack(stored.getFluid(), remaining));
            }
            return result;
        }

        return FluidStack.EMPTY;
    }
}
