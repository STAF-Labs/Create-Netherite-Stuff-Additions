package ru.lotuze.createnetheritestuffadditions.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.joml.Matrix4f;
import ru.lotuze.createnetheritestuffadditions.item.NetheritePortableDrillItem;

public class NetheritePortableDrillItemRenderer extends CustomRenderedItemModelRenderer {
    private static final float ROTATION_ORIGIN_Y = -0.125F;

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
            ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);

        Minecraft minecraft = Minecraft.getInstance();
        boolean mainHand = minecraft.player != null && minecraft.player.getMainHandItem() == stack;
        boolean offHand = minecraft.player != null && minecraft.player.getOffhandItem() == stack;
        boolean leftHanded = minecraft.player != null && minecraft.player.getMainArm() == HumanoidArm.LEFT;

        float renderTime = AnimationTickHolder.getRenderTime() / 10.0F;
        float angle = renderTime * -12.5F;
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        int fuel = 0;
        int water = 0;
        if (fluidHandler != null) {
            fuel = Math.round(fluidHandler.getFluidInTank(0).getAmount() * 0.1F);
            water = Math.round(fluidHandler.getFluidInTank(1).getAmount() * 0.1F);
        }

        if (fuel > 0 && water > 0) {
            angle = renderTime * (-12.5F
                    + Math.round((fuel + water) / (double) NetheritePortableDrillItem.DISPLAY_CAPACITY * -15.0D) * 4.0F);
        }

        if (mainHand || offHand) {
            float animation = CreateClient.POTATO_CANNON_RENDER_HANDLER.getAnimation(mainHand ^ leftHanded,
                    AnimationTickHolder.getPartialTicks());
            angle += 360.0F * Mth.clamp(animation * 5.0F, 0.0F, 1.0F);
        }

        angle %= 360.0F;

        poseStack.pushPose();
        rotateAroundHeadAxis(poseStack, angle);
        renderer.render(ModPartialModels.NETHERITE_PORTABLE_DRILL_COG.get(), light);

        rotateAroundHeadAxis(poseStack, angle * 1.5F);
        renderer.render(ModPartialModels.NETHERITE_PORTABLE_DRILL_HEAD.get(), light);
        poseStack.popPose();

        if (transformType == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(-16.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-133.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-23.0F));
            poseStack.translate(-0.14F, -0.065F, 0.0F);
            renderBars(stack, poseStack, buffer);
            poseStack.popPose();
        }
    }

    private static void rotateAroundHeadAxis(PoseStack poseStack, float angle) {
        poseStack.translate(0.0F, ROTATION_ORIGIN_Y, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(0.0F, -ROTATION_ORIGIN_Y, 0.0F);
    }

    private static void renderBars(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        float fuel = 0.0F;
        float water = 0.0F;
        if (fluidHandler != null) {
            fuel = Math.round(fluidHandler.getFluidInTank(0).getAmount() * 0.1F);
            water = Math.round(fluidHandler.getFluidInTank(1).getAmount() * 0.1F);
        }

        float fuelFill = Mth.clamp(fuel / NetheritePortableDrillItem.DISPLAY_CAPACITY, 0.0F, 1.0F);
        float waterFill = Mth.clamp(water / NetheritePortableDrillItem.DISPLAY_CAPACITY, 0.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 1.0F);
        poseStack.scale(0.0625F, 0.0625F, -0.0625F);
        drawBar(poseStack, buffer, -6.0F, -5.0F, fuelFill, 0xF5A470);
        drawBar(poseStack, buffer, -6.0F, -7.0F, waterFill, 0x7F9BD4);
        poseStack.popPose();
    }

    private static void drawBar(PoseStack poseStack, MultiBufferSource buffer, float x, float y, float fill, int color) {
        fill = Mth.clamp(fill, 0.0F, 1.0F);
        float width = 13.0F;
        float height = 2.0F;
        float filledWidth = Mth.floor(width * fill);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.debugQuads());
        addQuad(consumer, matrix, x, y, x + width, y + height, 0.0F, 0, 0, 0, 255);
        addQuad(consumer, matrix, x, y + height * 0.5F, x + filledWidth, y + height, -0.5F,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float x2, float y2,
            float z, int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, x1, y1, z).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x2, y1, z).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x2, y2, z).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, x1, y2, z).setColor(red, green, blue, alpha);
    }
}
