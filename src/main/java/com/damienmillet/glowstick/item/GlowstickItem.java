package com.damienmillet.glowstick.item;

import com.damienmillet.glowstick.GlowstickMod;
import com.damienmillet.glowstick.entity.Glowball;
import com.damienmillet.glowstick.entity.Glowstick;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;

public class GlowstickItem extends Item implements ProjectileItem {
	public static final float PROJECTILE_SHOOT_POWER = 1.5F;

	public GlowstickItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(
			null, player.getX(), player.getY(), player.getZ(),
			SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL,
			1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(
				Glowstick::new, serverLevel, stack, player,
				0.0F, PROJECTILE_SHOOT_POWER, 0.0F
			);
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		return new Glowstick(level, position.x(), position.y(), position.z(), stack);
	}
}
