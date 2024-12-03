package com.chaosthedude.naturescompass.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeightVariationCategory implements ISortingCategory {

	@Override
	public int compare(Biome biome1, Biome biome2) {
		return Float.compare(biome1.getScale(), biome2.getScale());
	}

	@Override
	public Object getValue(Biome biome) {
		return biome.getScale();
	}

	@Override
	public ISortingCategory next() {
		return new TemperatureCategory();
	}

	@Override
	public String getLocalizedName() {
		return I18n.format("string.naturescompass.heightVariation");
	}

}
