package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import xyz.nayskutzu.mythicalclient.utils.CapeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {
    
    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void onGetLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        CapeManager capeManager = CapeManager.getInstance();
        String playerName = player.getName();
        
        if (capeManager.hasCape(playerName)) {
            String capeName = capeManager.getCapeName(playerName);
            ResourceLocation capeTexture = capeManager.getCapeTexture(capeName);
            
            if (capeTexture != null) {
                cir.setReturnValue(capeTexture);
                return;
            }
        }
        
        // For the local player, always show a cape if they have one set
        if (player == net.minecraft.client.Minecraft.getMinecraft().thePlayer) {
            String capeName = capeManager.getCapeName(playerName);
            if (capeName != null) {
                ResourceLocation capeTexture = capeManager.getCapeTexture(capeName);
                if (capeTexture != null) {
                    cir.setReturnValue(capeTexture);
                }
            }
        }
    }
} 