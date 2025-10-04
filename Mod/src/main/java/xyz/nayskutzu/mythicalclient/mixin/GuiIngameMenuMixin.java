package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nayskutzu.mythicalclient.gui.GamsterDashboard;

@Mixin(GuiIngameMenu.class)
public class GuiIngameMenuMixin extends GuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    private void addDashboardButton(CallbackInfo ci) {
        // Move all buttons down by 24 pixels
        
        for (GuiButton button : this.buttonList) {
            if (button.id == 1) { // Back to Game button
                continue;
            }
            button.yPosition += 90;
        }
        
        // Add dashboard button at the top
        this.buttonList.add(new GuiButton(999, this.width / 2 - 100, this.height / 4 + 8, "§c§lGamster Dashboard"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 999) {
            mc.displayGuiScreen(new GamsterDashboard());
        }
    }
} 