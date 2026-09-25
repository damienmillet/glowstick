package com.damienmillet.glowstick.entity;

import com.damienmillet.glowstick.GlowstickMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

public class Glowstick extends AbstractArrow {
	private static final int MAX_LIFETIME = 1200;

	private int life = 0;
	private BlockPos lightPos;

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
			if (this.isInGround() && this.lightPos == null) {
				placeLight(serverLevel, hitResult);
			}
			serverLevel.sendParticles(
				ParticleTypes.GLOW,
				this.getX(), this.getY(), this.getZ(),
				6, 0.1, 0.1, 0.1, 0.0
			);
		}
	}

	private void placeLight(ServerLevel level, BlockHitResult hitResult) {
		BlockPos entityPos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
		BlockPos target = findLightPosition(
			level,
			hitResult.getBlockPos().relative(hitResult.getDirection()),
			entityPos,
			entityPos.above()
		);
		if (target != null) {
			BlockState lightState = Blocks.LIGHT.defaultBlockState()
				.setValue(LightBlock.LEVEL, LightBlock.MAX_LEVEL)
				.setValue(LightBlock.WATERLOGGED, level.getBlockState(target).getFluidState().is(FluidTags.WATER));
			level.setBlockAndUpdate(target, lightState);
			this.lightPos = target;
		}
	}

	private static BlockPos findLightPosition(ServerLevel level, BlockPos... candidates) {
		for (BlockPos pos : candidates) {
			if (canHostLight(level, pos)) {
				return pos;
			}
		}
		return null;
	}

	private static boolean canHostLight(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced() || state.is(Blocks.LIGHT)
			|| state.getFluidState().is(FluidTags.WATER);
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		super.remove(reason);
		if (this.lightPos == null || this.level().isClientSide()) {
			return;
		}
		if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED
			|| reason == Entity.RemovalReason.CHANGED_DIMENSION) {
			if (this.level() instanceof ServerLevel serverLevel
				&& serverLevel.getBlockState(this.lightPos).is(Blocks.LIGHT)) {
				serverLevel.removeBlock(this.lightPos, false);
			}
			this.lightPos = null;
		}
	}

	@Override
	public void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (this.lightPos != null) {
			output.storeNullable("LightPos", BlockPos.CODEC, this.lightPos);
		}
	}

	@Override
	public void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.lightPos = input.read("LightPos", BlockPos.CODEC).orElse(null);
	}
}
