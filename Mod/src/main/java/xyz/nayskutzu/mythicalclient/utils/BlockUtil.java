package xyz.nayskutzu.mythicalclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class BlockUtil {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static float[] getDirectionToBlock(int x, int y, int z, EnumFacing facing) {
        EntityEgg egg = new EntityEgg(BlockUtil.mc.theWorld);
        egg.posX = (double) x + 0.5;
        egg.posY = (double) y + 0.5;
        egg.posZ = (double) z + 0.5;

        // Adjust the egg's position slightly in the direction of the block
        egg.posX += (double) facing.getDirectionVec().getX() * 0.25;
        egg.posY += (double) facing.getDirectionVec().getY() * 0.25;
        egg.posZ += (double) facing.getDirectionVec().getZ() * 0.25;

        return BlockUtil.getDirectionToEntity(egg);
    }

    private static float[] getDirectionToEntity(Entity entity) {
        return new float[] {
                BlockUtil.getYaw(entity) + BlockUtil.mc.thePlayer.rotationYaw,
                BlockUtil.getPitch(entity) + BlockUtil.mc.thePlayer.rotationPitch
        };
    }

    public static float[] getRotationNeededForBlock(EntityPlayer player, BlockPos pos) {
        double deltaX = (double) pos.getX() - player.posX;
        double deltaY = (double) pos.getY() + 0.5 - (player.posY + (double) player.getEyeHeight());
        double deltaZ = (double) pos.getZ() - player.posZ;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI));

        return new float[] { yaw, pitch };
    }

    public static float getYaw(Entity entity) {
        double deltaX = entity.posX - BlockUtil.mc.thePlayer.posX;
        double deltaZ = entity.posZ - BlockUtil.mc.thePlayer.posZ;

        double angle = deltaZ < 0.0 && deltaX < 0.0 ? 90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX))
                : (deltaZ < 0.0 && deltaX > 0.0 ? -90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX))
                        : Math.toDegrees(-Math.atan(deltaX / deltaZ)));

        return MathHelper.wrapAngleTo180_float(-(BlockUtil.mc.thePlayer.rotationYaw - (float) angle));
    }

    public static float getPitch(Entity entity) {
        double deltaX = entity.posX - BlockUtil.mc.thePlayer.posX;
        double deltaZ = entity.posZ - BlockUtil.mc.thePlayer.posZ;
        double deltaY = entity.posY - 1.6 + (double) entity.getEyeHeight() - BlockUtil.mc.thePlayer.posY;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double pitchAngle = -Math.toDegrees(Math.atan(deltaY / horizontalDistance));

        return -MathHelper.wrapAngleTo180_float(BlockUtil.mc.thePlayer.rotationPitch - (float) pitchAngle);
    }

}
