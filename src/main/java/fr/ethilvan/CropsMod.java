package fr.ethilvan;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class CropsMod implements ModInitializer {
	public static final String MOD_ID = "crops-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("crops-mod enabled");
		changeCropsBreakBehavior();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}


	private void changeCropsBreakBehavior() {
		PlayerBlockBreakEvents.BEFORE.register((
				world,
				player,
				pos,
				state,
				blockEntity
		) -> {
			// Skip if not crop
			if (!(state.getBlock() instanceof CropBlock crop)) {
				return true;
			}
			// Skip if not a hoe
			ItemStack tool = player.getMainHandItem();
			if (!tool.is(ItemTags.HOES)) {
				return true;
			}
			// Skip if not fully grown
			if (!crop.isMaxAge(state)) {
				return false;
			}

			// Manage drops
			LootParams.Builder builder = new LootParams.Builder((ServerLevel) world)
					.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
					.withParameter(LootContextParams.TOOL, tool)
					.withOptionalParameter(LootContextParams.THIS_ENTITY, player);
			List<ItemStack> drops = state.getDrops(builder);
			Item replantItem = getReplantItem(state);
			for (ItemStack drop : drops) {
				if (drop.getItem().equals(replantItem)) {
					// Cancel drop if seeds
					if (getDropsToCancel().contains(drop.getItem())) {
						drop.setCount(0);
						continue;
					}
					// Remove 1 if crop and plantable item
					if (getDropsToReduce().contains(drop.getItem())) {
						drop.shrink(1);
					}
				}
			}

			// Replant
			world.setBlock(pos, crop.getStateForAge(0), CropBlock.UPDATE_ALL);

			// Drop remaining crops
			for (ItemStack stack : drops) {
				if (!stack.isEmpty()) {
					Block.popResource(world, pos, stack);
				}
			}

			// Damage tool
			tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

			return false;
		});
	}


	private static Item getReplantItem(BlockState blockState) {
		if (blockState.is(Blocks.WHEAT)) return Items.WHEAT_SEEDS;
		if (blockState.is(Blocks.CARROTS)) return Items.CARROT;
		if (blockState.is(Blocks.POTATOES)) return Items.POTATO;
		if (blockState.is(Blocks.BEETROOTS)) return Items.BEETROOT_SEEDS;
		if (blockState.is(Blocks.NETHER_WART)) return Items.NETHER_WART;

		return null;
	}


	private static List<Item> getDropsToCancel() {
		List<Item> dropsToCancel = new ArrayList<>();
		dropsToCancel.add(Items.WHEAT_SEEDS);
		dropsToCancel.add(Items.BEETROOT_SEEDS);
		return dropsToCancel;
	}


	private static List<Item> getDropsToReduce() {
		List<Item> dropsToReduce = new ArrayList<>();
		dropsToReduce.add(Items.CARROT);
		dropsToReduce.add(Items.POTATO);
		dropsToReduce.add(Items.NETHER_WART);
		return dropsToReduce;
	}
}
