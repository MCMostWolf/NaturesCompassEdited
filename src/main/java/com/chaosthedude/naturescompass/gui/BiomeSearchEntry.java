package com.chaosthedude.naturescompass.gui;

import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchEntry extends AbstractListEntry<BiomeSearchEntry> {

	private final Minecraft mc;
	private final NaturesCompassScreen parentScreen;
	private final Biome biome;
	private final BiomeSearchList biomesList;
	private long lastClickTime;

	public BiomeSearchEntry(BiomeSearchList biomesList, Biome biome) {
		this.biomesList = biomesList;
		this.biome = biome;
		parentScreen = biomesList.getParentScreen();
		mc = Minecraft.getInstance();
	}

	@Override
	public void render(MatrixStack matrixStack, int par1, int par2, int par3, int par4, int par5, int par6, int par7, boolean par8, float par9) {
		String precipitationState = I18n.format("string.naturescompass.none");
		if (biome.getPrecipitation() == RainType.SNOW) {
			precipitationState = I18n.format("string.naturescompass.snow");
		} else if (biome.getPrecipitation() == RainType.RAIN) {
			precipitationState = I18n.format("string.naturescompass.rain");
		}

		String title = parentScreen.getSortingCategory().getLocalizedName();
		Object value = parentScreen.getSortingCategory().getValue(biome);
		if (value == null) {
			title = I18n.format("string.naturescompass.topBlock");
			value = I18n.format(biome.getGenerationSettings().getSurfaceBuilderConfig().getTop().getBlock().getTranslationKey());
		}

		mc.fontRenderer.func_243248_b(matrixStack, new StringTextComponent(BiomeUtils.getBiomeNameForDisplay(parentScreen.world, biome)), par3 + 1, par2 + 1, 0xffffff);
		mc.fontRenderer.func_243248_b(matrixStack, new StringTextComponent(title + ": " + value), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 3, 0x808080);
		mc.fontRenderer.func_243248_b(matrixStack, new StringTextComponent(I18n.format("string.naturescompass.precipitation") + ": " + precipitationState), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 14, 0x808080);
		mc.fontRenderer.func_243248_b(matrixStack, new StringTextComponent(I18n.format("string.naturescompass.source") + ": " + BiomeUtils.getBiomeSource(parentScreen.world, biome)), par3 + 1, par2 + mc.fontRenderer.FONT_HEIGHT + 25, 0x808080);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			biomesList.selectBiome(this);
			if (Util.milliTime() - lastClickTime < 250L) {
				searchForBiome();
				return true;
			} else {
				lastClickTime = Util.milliTime();
				return false;
			}
		}
		return false;
	}

	public void searchForBiome() {
		mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		parentScreen.searchForBiome(biome);
	}

	public void viewInfo() {
		mc.displayGuiScreen(new BiomeInfoScreen(parentScreen, biome));
	}

}
