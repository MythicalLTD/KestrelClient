package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import xyz.nayskutzu.mythicalclient.SmoothSneakingState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {

    @Unique
    private SmoothSneakingState smoothSneakingState = new SmoothSneakingState();

    @Inject(method = "getEyeHeight", at = @At(value = "RETURN"), cancellable = true)
    public void getEyeHeight(CallbackInfoReturnable<Float> cir) {
        float returnValue = cir.getReturnValue();
        boolean isSneaking = ((Entity) (Object) this).isSneaking();
        if (isSneaking) returnValue += 0.08F;
        cir.setReturnValue(returnValue + smoothSneakingState.getSneakingHeightOffset(isSneaking));
    }

}
