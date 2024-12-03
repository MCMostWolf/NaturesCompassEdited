package com.chaosthedude.naturescompass.util;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.WorldWorkerManager;

import java.util.ArrayList;
import java.util.List;

import static com.chaosthedude.naturescompass.util.BiomeUtils.foundbiomes;
import static com.chaosthedude.naturescompass.util.BiomeUtils.coordinates;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

	public final int sampleSpace;
	public final int maxSamples;
	public final int maxRadius;
	public World world;
	public Biome biome;
	public ResourceLocation biomeKey;
	public BlockPos startPos;
	public int samples;
	public int nextLength;
	public Direction direction;
	public ItemStack stack;
	public PlayerEntity player;
	public int x;
	public int z;
	public int length;
	public boolean finished;
	public int lastRadiusThreshold;

	public BiomeSearchWorker(World world, PlayerEntity player, ItemStack stack, Biome biome, BlockPos startPos) {
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.biome = biome;
		this.startPos = startPos;
		x = startPos.getX();
		z = startPos.getZ();
		sampleSpace = ConfigHandler.GENERAL.sampleSpaceModifier.get() * BiomeUtils.getBiomeSize(world);
		maxSamples = ConfigHandler.GENERAL.maxSamples.get();
		maxRadius = ConfigHandler.GENERAL.radiusModifier.get() * BiomeUtils.getBiomeSize(world);
		nextLength = sampleSpace;
		length = 0;
		samples = 0;
		direction = Direction.UP;
		finished = false;
		biomeKey = BiomeUtils.getKeyForBiome(world, biome);
		lastRadiusThreshold = 0;
	}

	public void start() {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (maxRadius > 0 && sampleSpace > 0) {
				NaturesCompass.LOGGER.info("Starting search: " + sampleSpace + " sample space, " + maxSamples + " max samples, " + maxRadius + " max radius");
				WorldWorkerManager.addWorker(this);
			} else {
				finish(false);
			}
		}
	}

	@Override
	public boolean hasWork() {
		return !finished && getRadius() <= maxRadius && samples <= maxSamples;
	}

	@Override
	public boolean doWork() {
		if (hasWork()) {
			if (direction == Direction.NORTH) {
				z -= sampleSpace;
			} else if (direction == Direction.EAST) {
				x += sampleSpace;
			} else if (direction == Direction.SOUTH) {
				z += sampleSpace;
			} else if (direction == Direction.WEST) {
				x -= sampleSpace;
			}

			final BlockPos pos = new BlockPos(x, world.getHeight(), z);

			final Biome biomeAtPos = world.getBiomeManager().getBiome(pos);
			final ResourceLocation biomeAtPosKey = BiomeUtils.getKeyForBiome(world, biomeAtPos);

			if (biomeAtPosKey != null && biomeAtPosKey.equals(biomeKey)) {
				boolean isNew = true;
				if (!foundbiomes.isEmpty()) {
					for (Pair<Integer, Integer> coordinate : foundbiomes.get(true)) {
						if ((pos.getX() <= coordinate.getFirst().intValue() + ConfigHandler.GENERAL.ignoreRange.get().intValue())
								&& (pos.getX() >= coordinate.getFirst().intValue() - ConfigHandler.GENERAL.ignoreRange.get().intValue())
								&& (pos.getZ() >= coordinate.getSecond().intValue() - ConfigHandler.GENERAL.ignoreRange.get().intValue())
								&& (pos.getZ() <= coordinate.getSecond().intValue() + ConfigHandler.GENERAL.ignoreRange.get().intValue())) {
							isNew = false;
							break;
						}
					}
				}
				if (isNew) {
					coordinates.add(new Pair<>(x, z));
					// 找到了目标群系，缓存该位置
					List<Pair<Integer, Integer>> existingCoordinates = foundbiomes.getOrDefault(true, new ArrayList<>());
					existingCoordinates.addAll(coordinates);
					foundbiomes.put(true, existingCoordinates);
					finish(true);
					return false;
				}
			}

			samples++;
			length += sampleSpace;
			if (length >= nextLength) {
				if (direction != Direction.UP) {
					nextLength += sampleSpace;
					direction = direction.rotateY();
				} else {
					direction = Direction.NORTH;
				}
				length = 0;
			}
			int radius = getRadius();
			if (radius > 500 && radius / 500 > lastRadiusThreshold) {
				if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
					((NaturesCompassItem) stack.getItem()).setSearchRadius(stack, roundRadius(radius, 500), player);
				}
				lastRadiusThreshold = radius / 500;
			}
		}
		if (hasWork()) {
			return true;
		}
		finish(false);
		return false;
	}

	private void finish(boolean found) {
		if (!stack.isEmpty() && stack.getItem() == NaturesCompass.naturesCompass) {
			if (found) {
				NaturesCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
				((NaturesCompassItem) stack.getItem()).setFound(stack, x, z, samples, player);
				((NaturesCompassItem) stack.getItem()).setDisplayCoordinates(stack, ConfigHandler.GENERAL.displayCoordinates.get());
			} else {
				NaturesCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
				foundbiomes.clear();
				((NaturesCompassItem) stack.getItem()).setNotFound(stack, player, roundRadius(getRadius(), 500), samples);
			}
		} else {
			NaturesCompass.LOGGER.error("Invalid compass after search");
		}
		finished = true;
	}

	private int getRadius() {
		return BiomeUtils.getDistanceToBiome(startPos, x, z);
	}
	
	private int roundRadius(int radius, int roundTo) {
		return ((int) radius / roundTo) * roundTo;
	}

}