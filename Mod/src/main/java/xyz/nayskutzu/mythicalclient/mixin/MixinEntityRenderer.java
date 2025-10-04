package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { EntityRenderer.class })
public class MixinEntityRenderer {

    @Inject(method = { "hurtCameraEffect" }, at = { @At(value = "HEAD") }, cancellable = true)
    public void hurtCameraEffect(CallbackInfo ci) {
        ci.cancel();

    }
}