package com.damienmillet.glowstick.entity;

import com.damienmillet.glowstick.GlowstickMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class Glowstick extends AbstractArrow {
	private static final int MAX_LIFETIME = 1200;

	private int life = 0;

	public Glowstick(EntityType<? extends Glowstick> type, Level level) {
		super(type, level);
	}

	public Glowstick(Level level, LivingEntity owner, ItemStack stack) {
		super(GlowstickMod.GLOWSTICK_ENTITY, owner, level, stack, stack.copy());
	}

	public Glowstick(Level level, double x, double y, double z, ItemStack stack) {
		super(GlowstickMod.GLOWSTICK_ENTITY, x, y, z, level, stack, stack.copy());
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(GlowstickMod.GLOWSTICK);
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.WOOD_PLACE;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.isInGround()) {
			life++;
		}
		if (!this.level().isClientSide() && life >= MAX_LIFETIME) {
			this.discard();
			return;
		}
		if (this.level().isClientSide() && (this.isInGround() || this.tickCount % 2 == 0)) {
			this.level().addParticle(
				ParticleTypes.GLOW,
				this.getX(), this.getY() + 0.15, this.getZ(),
				0.0, 0.02, 0.0
			);
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity target) {
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(
				ParticleTypes.GLOW,
				this.getX(), this.getY(), this.getZ(),
				6, 0.1, 0.1, 0.1, 0.0
			);
		}
	}
}
