package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import xyz.nayskutzu.mythicalclient.utils.CapeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCape.class)
public class MixinLayerCape {
    
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void onDoRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        CapeManager capeManager = CapeManager.getInstance();
        String playerName = player.getName();
        
        if (capeManager.hasCape(playerName)) {
            String capeName = capeManager.getCapeName(playerName);
            ResourceLocation capeTexture = capeManager.getCapeTexture(capeName);
            
            if (capeTexture != null) {
                // Cancel the original cape rendering
                ci.cancel();
                
                // Render our custom cape instead
                renderCustomCape(player, capeTexture, partialTicks);
            }
        }
    }
    
    private void renderCustomCape(AbstractClientPlayer player, ResourceLocation capeTexture, float partialTicks) {
        // This will be handled by the CustomCapes hack
        // The mixin just prevents the vanilla cape from rendering
    }
} 