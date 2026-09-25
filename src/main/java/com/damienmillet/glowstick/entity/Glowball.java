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
import net.minecraft.world.phys.Vec3;

public class Glowball extends ThrowableItemProjectile {
	private static final double BOUNCE_DAMPING = 0.8;
	private static final double SETTLE_SPEED = 0.1;
	private static final int MAX_BOUNCES = 8;
	private static final int MAX_LIFETIME = 600;

	private int bounces = 0;
	private int life = 0;

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
		return 0.03;
	}

	@Override
	public void tick() {
		super.tick();
		life++;
		if (life >= MAX_LIFETIME && !this.level().isClientSide()) {
			placeLightAndDiscard();
			return;
		}
		if (this.level().isClientSide()) {
			this.level().addParticle(
				ParticleTypes.GLOW,
				this.getX() - this.getDeltaMovement().x * 0.5,
				this.getY() - this.getDeltaMovement().y * 0.5 + 0.1,
				this.getZ() - this.getDeltaMovement().z * 0.5,
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
					this.getX(), this.getY() + 0.1, this.getZ(),
					this.random.nextGaussian() * 0.05,
					this.random.nextDouble() * 0.1,
					this.random.nextGaussian() * 0.05
				);
			}
		} else {
			super.handleEntityEvent(eventId);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (this.level().isClientSide()) {
			return;
		}
		Vec3 movement = this.getDeltaMovement();
		double speed = movement.lengthSqr();
		boolean canBounce = bounces < MAX_BOUNCES && speed > SETTLE_SPEED * SETTLE_SPEED;

		if (canBounce) {
			bounces++;
			org.joml.Vector3f step = hitResult.getDirection().step();
			Vec3 normal = new Vec3(step.x(), step.y(), step.z());
			Vec3 reflected = movement.subtract(normal.scale(2.0 * movement.dot(normal)));
			Vec3 bounced = reflected.scale(BOUNCE_DAMPING);
			if (normal.y > 0.5) {
				bounced = new Vec3(bounced.x, Math.abs(bounced.y), bounced.z);
			}
			this.setDeltaMovement(bounced);
			this.level().playSound(
				null, this.blockPosition(),
				SoundEvents.SLIME_BLOCK_FALL, SoundSource.NEUTRAL,
				0.5F, (this.random.nextFloat() * 0.25F) + 0.85F
			);
		} else {
			this.level().broadcastEntityEvent(this, (byte) 3);
			placeLightAndDiscard();
		}
	}

	private void placeLightAndDiscard() {
		if (this.level() instanceof ServerLevel serverLevel) {
			BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
			BlockState lightState = Blocks.LIGHT.defaultBlockState();
			BlockPos target = findLightPosition(serverLevel, pos);
			if (target != null) {
				serverLevel.setBlockAndUpdate(target, lightState);
			} else {
				Block.popResource(serverLevel, pos, this.getItem().copy());
			}
		}
		this.discard();
	}

	private static BlockPos findLightPosition(ServerLevel level, BlockPos pos) {
		if (canHostLight(level, pos)) {
			return pos;
		}
		BlockPos above = pos.above();
		if (canHostLight(level, above)) {
			return above;
		}
		return null;
	}

	private static boolean canHostLight(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced();
	}
}
