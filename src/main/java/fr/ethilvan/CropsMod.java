package fr.ethilvan;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
		modifyCropsDrops();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}


	private void modifyCropsDrops() {
		LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> {

			ResourceKey<LootTable> wheatTable = Blocks.WHEAT.getLootTable().orElseThrow();
			ItemStack tool = (ItemStack) context.getOptionalParameter(LootContextParams.TOOL);

			holder.unwrapKey().ifPresent(key -> {
				if (key.equals(wheatTable)) {
					if (tool != null && tool.is(ItemTags.HOES)) {
						drops.removeIf(stack -> stack.is(Items.WHEAT_SEEDS));
					}
				}
			});
		});
	}
}
