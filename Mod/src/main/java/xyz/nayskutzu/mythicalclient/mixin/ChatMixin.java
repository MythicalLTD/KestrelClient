package xyz.nayskutzu.mythicalclient.mixin;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nayskutzu.mythicalclient.v2.ChatServer;

@Mixin(GuiNewChat.class)
public class ChatMixin {
    @Inject(method = "printChatMessage", at = @At("HEAD"))
    private void onPrintChatMessage(IChatComponent chatComponent, CallbackInfo ci) {
        if (chatComponent != null) {
            String message = chatComponent.getUnformattedText();
            ChatServer.getInstance().broadcastMessage(message);
        }
    }
} 