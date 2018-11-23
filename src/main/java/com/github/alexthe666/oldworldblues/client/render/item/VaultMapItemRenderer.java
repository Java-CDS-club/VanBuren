package com.github.alexthe666.oldworldblues.client.render.item;

import com.google.common.collect.Maps;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class VaultMapItemRenderer
{
    private static final ResourceLocation TEXTURE_MAP_ICONS = new ResourceLocation("oldworldblues:textures/map/vault_map_icons.png");
    private final TextureManager textureManager;
    private final Map<String, VaultMapItemRenderer.Instance> loadedMaps = Maps.<String, VaultMapItemRenderer.Instance>newHashMap();

    public VaultMapItemRenderer(TextureManager textureManagerIn)
    {
        this.textureManager = textureManagerIn;
    }

    /**
     * Updates a map texture
     */
    public void updateMapTexture(MapData mapdataIn)
    {
        this.getMapRendererInstance(mapdataIn).updateMapTexture();
    }

    public void renderMap(MapData mapdataIn, boolean noOverlayRendering)
    {
        this.getMapRendererInstance(mapdataIn).render(noOverlayRendering);
    }

    /**
     * Returns {@link net.minecraft.client.gui.MapItemRenderer.Instance MapItemRenderer.Instance} with given map data
     */
    private VaultMapItemRenderer.Instance getMapRendererInstance(MapData mapdataIn)
    {
        VaultMapItemRenderer.Instance mapitemrenderer$instance = this.loadedMaps.get(mapdataIn.mapName);

        if (mapitemrenderer$instance == null)
        {
            mapitemrenderer$instance = new VaultMapItemRenderer.Instance(mapdataIn);
            this.loadedMaps.put(mapdataIn.mapName, mapitemrenderer$instance);
        }

        return mapitemrenderer$instance;
    }

    @Nullable
    public VaultMapItemRenderer.Instance getMapInstanceIfExists(String p_191205_1_)
    {
        return this.loadedMaps.get(p_191205_1_);
    }

    /**
     * Clears the currently loaded maps and removes their corresponding textures
     */
    public void clearLoadedMaps()
    {
        for (VaultMapItemRenderer.Instance mapitemrenderer$instance : this.loadedMaps.values())
        {
            this.textureManager.deleteTexture(mapitemrenderer$instance.location);
        }

        this.loadedMaps.clear();
    }

    @Nullable
    public MapData getData(@Nullable VaultMapItemRenderer.Instance p_191207_1_)
    {
        return p_191207_1_ != null ? p_191207_1_.mapData : null;
    }

    @SideOnly(Side.CLIENT)
    class Instance
    {
        private final MapData mapData;
        private final DynamicTexture mapTexture;
        private final ResourceLocation location;
        private final int[] mapTextureData;

        private Instance(MapData mapdataIn)
        {
            this.mapData = mapdataIn;
            this.mapTexture = new DynamicTexture(128, 128);
            this.mapTextureData = this.mapTexture.getTextureData();
            this.location = VaultMapItemRenderer.this.textureManager.getDynamicTextureLocation("map/" + mapdataIn.mapName, this.mapTexture);

            for (int i = 0; i < this.mapTextureData.length; ++i)
            {
                this.mapTextureData[i] = 0;
            }
        }

        /**
         * Updates a map {@link net.minecraft.client.gui.MapItemRenderer.Instance#mapTexture texture}
         */
        private void updateMapTexture()
        {
            for (int i = 0; i < 16384; ++i)
            {
                int j = this.mapData.colors[i] & 255;

                if (j / 4 == 0)
                {
                    this.mapTextureData[i] = (i + i / 128 & 1) * 8 + 16 << 24;
                }
                else
                {
                    this.mapTextureData[i] = MapColor.COLORS[j / 4].getMapColor(j & 3);
                }
            }

            this.mapTexture.updateDynamicTexture();
        }

        /**
         * Renders map and players to it
         */
        private void render(boolean noOverlayRendering)
        {
            updateMapTexture();
            int i = 0;
            int j = 0;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            float f = 0.0F;
            VaultMapItemRenderer.this.textureManager.bindTexture(this.location);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(0.0D, 128.0D, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(128.0D, 128.0D, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(128.0D, 0.0D, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
            GL11.glPushMatrix();
            GL11.glColor4f(0, 0, 1.0F, 0.5F);
            tessellator.draw();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            VaultMapItemRenderer.this.textureManager.bindTexture(VaultMapItemRenderer.TEXTURE_MAP_ICONS);
            int k = 0;

            for (MapDecoration mapdecoration : this.mapData.mapDecorations.values())
            {
                if (!noOverlayRendering || mapdecoration.renderOnFrame())
                {
                    if (mapdecoration.render(k)) { k++; continue; }
                    VaultMapItemRenderer.this.textureManager.bindTexture(VaultMapItemRenderer.TEXTURE_MAP_ICONS); // Rebind in case custom render changes it
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F, -0.02F);
                    GlStateManager.rotate((float)(mapdecoration.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
                    float scale = mapdecoration.getType() == MapDecoration.Type.MONUMENT ? 2.0F : 1.0F;
                    GlStateManager.scale(scale * 4.0F, scale * 4.0F, 3.0F);
                    GlStateManager.translate(-0.125F, 0.125F, 0.0F);
                    byte b0 = mapdecoration.getImage();
                    float f1 = (float)(b0 % 4 + 0) / 4.0F;
                    float f2 = (float)(b0 / 4 + 0) / 4.0F;
                    float f3 = (float)(b0 % 4 + 1) / 4.0F;
                    float f4 = (float)(b0 / 4 + 1) / 4.0F;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                    float f5 = -0.001F;
                    bufferbuilder.pos(-1.0D, 1.0D, (double)((float)k * -0.001F)).tex((double)f1, (double)f2).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, (double)((float)k * -0.001F)).tex((double)f3, (double)f2).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, (double)((float)k * -0.001F)).tex((double)f3, (double)f4).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, (double)((float)k * -0.001F)).tex((double)f1, (double)f4).endVertex();
                    tessellator.draw();
                    GlStateManager.popMatrix();
                    ++k;
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, -0.04F);
            GlStateManager.scale(1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}