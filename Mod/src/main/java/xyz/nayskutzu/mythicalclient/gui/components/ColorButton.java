package xyz.nayskutzu.mythicalclient.gui.components;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.Color;

public class ColorButton extends GuiButton {
    private final int baseColor;
    private float hoverTime = 0;
    private float clickTime = 0;
    private boolean isSelected = false;
    @SuppressWarnings("unused")
    private long lastClickTime = 0;
    
    public ColorButton(int buttonId, int x, int y, int width, int height, String text, int color) {
        super(buttonId, x, y, width, height, text);
        this.baseColor = color;
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // Precise hover detection with strict bounds checking
            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && 
                            mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            
            // Individual button hover state management
            if (hovered) {
                if (hoverTime < 1) {
                    hoverTime += 0.15f;
                }
            } else {
                if (hoverTime > 0) {
                    hoverTime -= 0.15f;
                }
            }
            hoverTime = Math.max(0, Math.min(1, hoverTime));
            
            // Click animation
            if (this.mousePressed(mc, mouseX, mouseY)) {
                clickTime = 1.0f;
                lastClickTime = System.currentTimeMillis();
            } else {
                clickTime = Math.max(0, clickTime - 0.2f);
            }
            
            // Calculate colors with enhanced effects
            Color baseColorObj = new Color(baseColor);
            int darkColor = baseColorObj.darker().getRGB();
            int hoverColor = baseColor;
            int selectedColor = isSelected ? baseColor : darkColor;
            int finalColor = interpolateColor(selectedColor, hoverColor, hoverTime);
            
            // Apply click effect
            if (clickTime > 0) {
                finalColor = interpolateColor(finalColor, 0xFFFFFF, clickTime * 0.3f);
            }
            
            // Draw button background with gradient
            drawGradientRect(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + this.height,
                    new Color(20, 20, 25, 220).getRGB(),
                    new Color(30, 30, 35, 220).getRGB());
            
            // Draw border
            int borderColor = interpolateColor(new Color(60, 60, 70).getRGB(), finalColor, hoverTime * 0.5f);
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + 1, borderColor);
            drawRect(this.xPosition, this.yPosition, this.xPosition + 1, this.yPosition + this.height, borderColor);
            drawRect(this.xPosition + this.width - 1, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, borderColor);
            drawRect(this.xPosition, this.yPosition + this.height - 1, this.xPosition + this.width, this.yPosition + this.height, borderColor);
            
            // Draw colored accent bar
            int accentHeight = Math.max(2, (int)(3 * (1 + hoverTime * 0.5f)));
            drawRect(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + accentHeight,
                    finalColor);
            
            // Draw text with enhanced shadow
            GlStateManager.pushMatrix();
            float textScale = 1.0f + (hoverTime * 0.1f);
            GlStateManager.translate(this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0);
            GlStateManager.scale(textScale, textScale, 1);
            GlStateManager.translate(-(this.xPosition + this.width / 2), -(this.yPosition + (this.height - 8) / 2), 0);
            
            // Draw text shadow
            this.drawCenteredString(mc.fontRendererObj, this.displayString,
                this.xPosition + this.width / 2 + 1,
                this.yPosition + (this.height - 8) / 2 + 1,
                0x88000000);
            
            // Draw main text
            int textColor = interpolateColor(0xCCCCCC, 0xFFFFFF, hoverTime);
            this.drawCenteredString(mc.fontRendererObj, this.displayString,
                this.xPosition + this.width / 2,
                this.yPosition + (this.height - 8) / 2,
                textColor);
            
            GlStateManager.popMatrix();
            
            // Draw selection indicator
            if (isSelected) {
                drawRect(this.xPosition + this.width - 8, this.yPosition + 2,
                        this.xPosition + this.width - 2, this.yPosition + 8,
                        0x55FF55);
            }
        }
    }
    
    private int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int)(r1 + (r2 - r1) * factor);
        int g = (int)(g1 + (g2 - g1) * factor);
        int b = (int)(b1 + (b2 - b1) * factor);
        
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
    
    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double)right, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
    
    public void setColor(int color) {
        // This method is kept for compatibility but doesn't change the base color
        // The base color is final and set in constructor
    }
} 