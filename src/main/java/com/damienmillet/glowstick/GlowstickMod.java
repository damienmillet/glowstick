package com.damienmillet.glowstick;

import com.damienmillet.glowstick.entity.Glowball;
import com.damienmillet.glowstick.entity.Glowstick;
import com.damienmillet.glowstick.item.GlowballItem;
import com.damienmillet.glowstick.item.GlowstickItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlowstickMod implements ModInitializer {
	public static final String MOD_ID = "glowstick";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityType<Glowball> GLOWBALL = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(MOD_ID, "glowball"),
		EntityType.Builder.<Glowball>of(Glowball::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "glowball")))
	);

	public static final EntityType<Glowstick> GLOWSTICK_ENTITY = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(MOD_ID, "glowstick"),
		EntityType.Builder.<Glowstick>of(Glowstick::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "glowstick")))
	);

	public static final Item GLOWBALL_ITEM = Registry.register(
		BuiltInRegistries.ITEM,
		Identifier.fromNamespaceAndPath(MOD_ID, "glowball"),
		new GlowballItem(new Item.Properties().stacksTo(16).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "glowball"))))
	);

	public static final Item GLOWSTICK = Registry.register(
		BuiltInRegistries.ITEM,
		Identifier.fromNamespaceAndPath(MOD_ID, "glowstick"),
		new GlowstickItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "glowstick"))))
	);

	@Override
	public void onInitialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(GLOWBALL_ITEM.getDefaultInstance());
			output.accept(GLOWSTICK.getDefaultInstance());
		});
		LOGGER.info("Glowstick mod initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
