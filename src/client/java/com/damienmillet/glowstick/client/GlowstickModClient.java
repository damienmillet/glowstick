package com.damienmillet.glowstick.client;

import com.damienmillet.glowstick.GlowstickMod;
import com.damienmillet.glowstick.entity.Glowball;
import com.damienmillet.glowstick.entity.Glowstick;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class GlowstickModClient implements ClientModInitializer {

	private static final Identifier GLOWSTICK_LOCATION = Identifier.fromNamespaceAndPath(
		GlowstickMod.MOD_ID, "textures/entity/projectiles/glowstick.png"
	);

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(GlowstickMod.GLOWBALL, context -> new ThrownItemRenderer<>(context, 1.0F, true));
		EntityRendererRegistry.register(GlowstickMod.GLOWSTICK_ENTITY, GlowstickRenderer::new);
	}

	private static final class GlowstickRenderer extends ArrowRenderer<Glowstick, ArrowRenderState> {

		private GlowstickRenderer(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		protected Identifier getTextureLocation(ArrowRenderState state) {
			return GLOWSTICK_LOCATION;
		}

		@Override
		public ArrowRenderState createRenderState() {
			return new ArrowRenderState();
		}
	}
}
