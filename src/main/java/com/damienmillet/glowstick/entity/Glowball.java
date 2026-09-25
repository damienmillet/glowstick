package com.damienmillet.glowstick.entity;

import com.damienmillet.glowstick.GlowstickMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Glowball extends ThrowableItemProjectile {
	private static final double BOUNCE_DAMPING = 0.75;
	private static final int MAX_BOUNCES = 5;
	private int bounces = 0;

	public Glowball(EntityType<? extends Glowball> type, Level level) {
		super(type, level);
	}

	public Glowball(Level level, LivingEntity owner, ItemStack stack) {
		super(GlowstickMod.GLOWBALL, owner, level, stack);
	}

	public Glowball(Level level, double x, double y, double z, ItemStack stack) {
		super(GlowstickMod.GLOWBALL, x, y, z, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return GlowstickMod.GLOWBALL_ITEM;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.02;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.level().addParticle(
				ParticleTypes.GLOW,
				this.getX() - (this.getDeltaMovement().x * 0.5),
				this.getY() - (this.getDeltaMovement().y * 0.5),
				this.getZ() - (this.getDeltaMovement().z * 0.5),
				0.0, 0.0, 0.0
			);
		}
	}

	@Override
	public void handleEntityEvent(byte eventId) {
		if (eventId == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(
					ParticleTypes.GLOW,
					this.getX(), this.getY(), this.getZ(),
					this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.1, this.random.nextGaussian() * 0.05
				);
			}
		} else {
			super.handleEntityEvent(eventId);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		if (this.level().isClientSide()) {
			return;
		}
		Vec3 movement = this.getDeltaMovement();
		double horizontalSpeed = movement.x * movement.x + movement.z * movement.z;
		if (bounces < MAX_BOUNCES && horizontalSpeed + movement.y * movement.y > 0.01) {
			bounces++;
			org.joml.Vector3f step = hitResult.getDirection().step();
			Vec3 normal = new Vec3(step.x(), step.y(), step.z());
			Vec3 newMovement = movement.subtract(
				normal.scale(2.0 * movement.dot(normal)).scale(BOUNCE_DAMPING)
			);
			if (normal.y != 0) {
				newMovement = newMovement.multiply(1.0, BOUNCE_DAMPING, 1.0);
			}
			this.setDeltaMovement(newMovement);
			this.level().playSound(
				null, this.blockPosition(),
				SoundEvents.SLIME_BLOCK_FALL, SoundSource.NEUTRAL,
				0.5F, (this.random.nextFloat() * 0.25F) + 0.75F
			);
			return;
		}
		super.onHitBlock(hitResult);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			placeLightAndDiscard();
		}
	}

	private void placeLightAndDiscard() {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			this.discard();
			return;
		}
		BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
		BlockState lightState = Blocks.LIGHT.defaultBlockState();
		if (serverLevel.getBlockState(pos).canBeReplaced() || serverLevel.getBlockState(pos).isAir()) {
			serverLevel.setBlockAndUpdate(pos, lightState);
		} else {
			BlockPos above = pos.above();
			if (serverLevel.getBlockState(above).canBeReplaced() || serverLevel.getBlockState(above).isAir()) {
				serverLevel.setBlockAndUpdate(above, lightState);
			} else {
				Block.popResource(serverLevel, pos, this.getItem().copy());
				this.discard();
				return;
			}
		}
		this.discard();
	}
}
