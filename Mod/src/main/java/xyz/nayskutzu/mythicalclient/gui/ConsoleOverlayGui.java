package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;
import java.util.List;

public class ConsoleOverlayGui extends GuiScreen {
    private GuiTextField inputField;
    private int scrollOffset = 0; // number of lines from bottom
    private static final int PADDING = 6;

    @Override
    public void initGui() {
        int inputHeight = 18;
        inputField = new GuiTextField(0, this.fontRendererObj, PADDING, this.height - inputHeight - PADDING, this.width - PADDING * 2, inputHeight);
        inputField.setMaxStringLength(512);
        inputField.setFocused(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, this.width - 80 - PADDING, PADDING, 80, 20, "Clear"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            ConsoleLogBuffer.getInstance().clear();
            scrollOffset = 0;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = org.lwjgl.input.Mouse.getDWheel();
        if (dWheel != 0) {
            int delta = dWheel > 0 ? -5 : 5;
            int size = ConsoleLogBuffer.getInstance().size();
            int maxOffset = Math.max(0, size - visibleLines());
            scrollOffset = Math.max(0, Math.min(scrollOffset + delta, maxOffset));
        }
    }

    private int visibleLines() {
        int inputHeight = 18 + PADDING * 2;
        int available = this.height - inputHeight - PADDING * 2;
        return Math.max(1, available / (this.fontRendererObj.FONT_HEIGHT + 1));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == 28) { // ENTER
            String text = inputField.getText();
            if (text != null && !text.trim().isEmpty()) {
                if (text.startsWith("/")) {
                    mc.thePlayer.sendChatMessage(text);
                } else if (text.startsWith(":js ")) {
                    String code = text.substring(4);
                    String result = xyz.nayskutzu.mythicalclient.scripting.ConsoleScriptEngine.getInstance().evaluate(code);
                    ConsoleLogBuffer.getInstance().append("> " + code);
                    ConsoleLogBuffer.getInstance().append(String.valueOf(result));
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(text));
                }
                inputField.setText("");
                scrollOffset = 0;
            }
            return;
        }
        inputField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        inputField.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        FontRenderer fr = this.fontRendererObj;

        // Draw logs
        List<String> lines = ConsoleLogBuffer.getInstance().snapshot();
        int linesToShow = visibleLines();
        int start = Math.max(0, lines.size() - linesToShow - scrollOffset);
        int end = Math.min(lines.size(), start + linesToShow);
        int y = PADDING + 2;
        for (int i = start; i < end; i++) {
            String ln = lines.get(i);
            fr.drawString(ln, PADDING, y, 0xE0E0E0);
            y += fr.FONT_HEIGHT + 1;
        }

        // Input background and field
        int inputY = this.height - (18 + PADDING * 2);
        drawRect(PADDING - 2, inputY - 4, this.width - PADDING + 2, inputY + 20, 0x88000000);
        inputField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
} 