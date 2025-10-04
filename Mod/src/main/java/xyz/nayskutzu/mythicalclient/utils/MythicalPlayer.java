package xyz.nayskutzu.mythicalclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;

public class MythicalPlayer {
    public static MythicalPlayer instance = new MythicalPlayer();
    private MythicalClientMod mod = MythicalClientMod.instance;
    private Config config = Config.instance;
    private Minecraft mc = Minecraft.getMinecraft();
    private BlockPos currentBlock;
    private EnumFacing currentFacing;
    private float yawBefore;
    private float pitchBefore;
    private boolean sneaking = false;
    private boolean safewalkSneaking = false;
    private boolean userSneaking = false;
    private long lastRightClick = System.currentTimeMillis();
    private int rightClickDelay = 290;

    public void onJump() {
        try {
            if (this.config == null || this.mod == null) return;
            
            if (this.config.jump && this.mod.isToggled()) {
                this.mod.toggle(false);
                this.mod.sendToggle(this.config.getMode(this.config.mode), "on", "off", false);
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    public void onTick() {
        try {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;
            
            // Check if player is falling and safewalk is toggled
            if (this.config != null && this.mod != null) {
                if (this.mc.thePlayer.prevPosY - this.mc.thePlayer.posY > 0.4 && this.config.fall && this.mod.isToggled()) {
                    this.mod.toggle(false);
                    this.mod.sendToggle(this.config.getMode(this.config.mode), "on", "off", false);
                }
            }

            double playerX = this.mc.thePlayer.posX;
            double playerY = this.mc.thePlayer.posY - 1.0;
            double playerZ = this.mc.thePlayer.posZ;
            BlockPos currentBlock = new BlockPos((int) Math.floor(playerX), (int) Math.floor(playerY), (int) Math.floor(playerZ));

            if (this.currentBlock == null || !this.isSameBlock(currentBlock, this.currentBlock)) {
                this.currentBlock = currentBlock;
            }

            // Check if the current block is air (not solid) and set safewalk sneaking
            if (this.mc.theWorld.isAirBlock(this.currentBlock)) {
                this.setSafewalkSneaking(true);
            } else {
                this.setSafewalkSneaking(false);
            }

            this.checkSneak();
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private void checkSneak() {
        try {
            if (mc == null || mc.gameSettings == null || mc.gameSettings.keyBindSneak == null || mc.thePlayer == null) return;

            if (!this.userSneaking) {
                if (this.mc.thePlayer.onGround) {
                    this.sneaking = this.safewalkSneaking;
                    this.doSafewalk();
                } else {
                    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
                }
            } else {
                KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private void doSafewalk() {
        try {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null || config == null) return;

            if (this.config.mode == 1) {
                if (this.isSneaking() != this.mc.thePlayer.isSneaking()) {
                    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), this.isSneaking());
                }
                if (this.config.click) {
                    this.tryRightClick();
                }
            }

            if (this.config.mode == 2) {
                KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
                if (this.isSneaking()) {
                    this.mc.thePlayer.motionX *= 0.5;
                    this.mc.thePlayer.motionZ *= 0.5;

                    if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.getBlockPos() != null) {
                        if (this.mc.thePlayer.getHeldItem() != null && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
                            if (this.isSameBlock(this.mc.objectMouseOver.getBlockPos(), this.getBlock(this.currentBlock, this.mc.objectMouseOver.sideHit))) {
                                if (this.isFacingSide(this.mc.objectMouseOver.sideHit)) {
                                    if (System.currentTimeMillis() - this.lastRightClick > this.rightClickDelay) {
                                        this.lastRightClick = System.currentTimeMillis();
                                        this.mc.playerController.onPlayerRightClick(this.mc.thePlayer, this.mc.theWorld, this.mc.thePlayer.getHeldItem(), this.mc.objectMouseOver.getBlockPos(), this.mc.objectMouseOver.sideHit, this.mc.objectMouseOver.hitVec);
                                        this.mc.thePlayer.swingItem();
                                    }
                                } else {
                                    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                                }
                            }
                        } else {
                            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        }
                    } else {
                        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                    }
                }
            }

            if (this.config.mode == 3) {
                handleMode3();
            }
        } catch (Exception e) {
            // Silently fail rather than crash
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
    }

    private void handleMode3() {
        try {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;
            
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
            if (this.isSneaking()) {
                if (this.mc.thePlayer.getHeldItem() != null && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
                    this.yawBefore = this.mc.thePlayer.rotationYaw;
                    this.pitchBefore = this.mc.thePlayer.rotationPitch;
                    this.setBlockAndFacing(this.currentBlock);
                    
                    if (this.currentBlock != null && this.currentFacing != null) {
                        float[] facing = BlockUtil.getDirectionToBlock(this.currentBlock.getX(), this.currentBlock.getY(), this.currentBlock.getZ(), this.currentFacing);
                        float yawToBlock = facing[0];
                        float pitchToBlock = Math.min(90.0f, facing[1] + 9.0f);

                        this.mc.thePlayer.rotationYaw = yawToBlock;
                        this.mc.thePlayer.rotationPitch = pitchToBlock;

                        this.mc.playerController.onPlayerRightClick(this.mc.thePlayer, this.mc.theWorld, this.mc.thePlayer.getHeldItem(), this.currentBlock, this.currentFacing, new Vec3(this.currentBlock.getX(), this.currentBlock.getY(), this.currentBlock.getZ()));
                        this.mc.thePlayer.swingItem();

                        new Delay(0) {
                            @Override
                            public void onTick() {
                                try {
                                    mc.thePlayer.rotationYaw = yawBefore;
                                    mc.thePlayer.rotationPitch = pitchBefore;
                                } catch (Exception e) {
                                    // Silently fail rather than crash
                                }
                            }
                        };
                    }
                } else {
                    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }
            }
        } catch (Exception e) {
            // Silently fail rather than crash
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
    }

    private void tryRightClick() {
        try {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null || 
                mc.objectMouseOver == null || currentBlock == null) {
                return;
            }

            BlockPos targetPos = mc.objectMouseOver.getBlockPos();
            if (targetPos == null) return;

            if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
                return;
            }

            EnumFacing sideHit = mc.objectMouseOver.sideHit;
            if (sideHit == null) return;

            if (isFacingSide(sideHit) && isSameBlock(targetPos, getBlock(currentBlock, sideHit))) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRightClick > rightClickDelay) {
                    lastRightClick = currentTime;
                }
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    public void setSafewalkSneaking(boolean safewalkSneaking) {
        try {
            this.safewalkSneaking = safewalkSneaking;
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    public void setUserSneaking(boolean userSneaking) {
        try {
            this.userSneaking = userSneaking;
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private boolean isSneaking() {
        try {
            return this.sneaking;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSameBlock(BlockPos pos1, BlockPos pos2) {
        try {
            if (pos1 == null || pos2 == null) return false;
            return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isFacingSide(EnumFacing facing) {
        try {
            if (facing == null) return false;
            return facing != EnumFacing.UP && facing != EnumFacing.DOWN;
        } catch (Exception e) {
            return false;
        }
    }

    private BlockPos getBlock(BlockPos block, EnumFacing facing) {
        try {
            if (block == null || facing == null) return null;
            
            switch (facing) {
                case NORTH:
                    return new BlockPos(block.getX(), block.getY(), block.getZ() + 1);
                case SOUTH:
                    return new BlockPos(block.getX(), block.getY(), block.getZ() - 1);
                case EAST:
                    return new BlockPos(block.getX() - 1, block.getY(), block.getZ());
                case WEST:
                    return new BlockPos(block.getX() + 1, block.getY(), block.getZ());
                case UP:
                    return new BlockPos(block.getX(), block.getY() + 1, block.getZ());
                default:
                    return block;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void setBlockAndFacing(BlockPos block) {
        try {
            if (mc == null || mc.theWorld == null || block == null) {
                this.currentBlock = null;
                this.currentFacing = null;
                return;
            }

            // Safely check each direction
            try {
                if (isNonAirBlock(block.down())) {
                    this.currentBlock = block.down();
                    this.currentFacing = EnumFacing.UP;
                    return;
                }
            } catch (Exception e) {}

            try {
                if (isNonAirBlock(block.west())) {
                    this.currentBlock = block.west();
                    this.currentFacing = EnumFacing.EAST;
                    return;
                }
            } catch (Exception e) {}

            try {
                if (isNonAirBlock(block.east())) {
                    this.currentBlock = block.east();
                    this.currentFacing = EnumFacing.WEST;
                    return;
                }
            } catch (Exception e) {}

            try {
                if (isNonAirBlock(block.north())) {
                    this.currentBlock = block.north();
                    this.currentFacing = EnumFacing.SOUTH;
                    return;
                }
            } catch (Exception e) {}

            try {
                if (isNonAirBlock(block.south())) {
                    this.currentBlock = block.south();
                    this.currentFacing = EnumFacing.NORTH;
                    return;
                }
            } catch (Exception e) {}

            this.currentBlock = null;
            this.currentFacing = null;
        } catch (Exception e) {
            this.currentBlock = null;
            this.currentFacing = null;
        }
    }

    private boolean isNonAirBlock(BlockPos pos) {
        try {
            return mc != null && mc.theWorld != null && pos != null && 
                   mc.theWorld.getBlockState(pos) != null && 
                   mc.theWorld.getBlockState(pos).getBlock() != null && 
                   mc.theWorld.getBlockState(pos).getBlock() != Blocks.air;
        } catch (Exception e) {
            return false;
        }
    }
}
