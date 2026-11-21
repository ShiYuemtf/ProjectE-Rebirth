package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.utils.WorldTransmutations;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Draws the ghost blocks of a world transmutation plus the matching hud icon. Client only.
 */
public class TransmutationRenderingOverlay {

	private static final TransmutationRenderingOverlay INSTANCE = new TransmutationRenderingOverlay();

	private final Minecraft mc = Minecraft.getInstance();
	@Nullable
	private BlockState transmutationResult;
	private long lastGameTime;

	public static void register() {
		HudRenderCallback.EVENT.register(INSTANCE::render);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(INSTANCE::onOverlay);
	}

	private void render(GuiGraphics graphics, float partialTicks) {
		if (mc.options.hideGui || transmutationResult == null) {
			return;
		}
		//Only when a philosopher's stone is actually in hand: the pending result is cleared on the next game tick, which never happens while the
		// game is paused, and switching to another item used to leave the preview icon stuck on screen until something forced a tick.
		if (mc.player == null || !(mc.player.getMainHandItem().getItem() instanceof PhilosophersStone)) {
			transmutationResult = null;
			return;
		}
		ClientLevel level = mc.level;
		if (transmutationResult.getBlock() instanceof LiquidBlock liquidBlock && level != null) {
			BlockPos tintPos = mc.player == null ? BlockPos.ZERO : mc.player.blockPosition();
			FluidState fluidState = transmutationResult.getFluidState();
			//shortcut: Forge's IClientFluidTypeExtensions exposed one default tint, Fabric's handler wants a position, so the player's own position stands in for it
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getType());
			TextureAtlasSprite sprite = handler == null ? null : firstSprite(handler.getFluidSprites(level, tintPos, fluidState));
			if (sprite == null) {
				graphics.renderItem(new ItemStack(transmutationResult.getBlock()), 1, 1);
			} else {
				int color = handler.getFluidColor(level, tintPos, fluidState);
				graphics.blit(1, 1, 0, 16, 16, sprite,
						(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F);
			}
		} else {
			//Just render it normally instead of with the given model as some block's don't render properly then as an item
			// for example glass panes
			graphics.renderItem(new ItemStack(transmutationResult.getBlock()), 1, 1);
		}
		long gameTime = level == null ? 0 : level.getGameTime();
		if (lastGameTime != gameTime) {
			//If the game time changed, so we aren't actually still hovering a block set our
			// result to null. We do this after rendering it just in case there is a single
			// frame where this may actually be valid based on the order the events are fired
			transmutationResult = null;
			lastGameTime = gameTime;
		}
	}

	@Nullable
	private static TextureAtlasSprite firstSprite(@Nullable TextureAtlasSprite[] sprites) {
		return sprites == null || sprites.length == 0 ? null : sprites[0];
	}

	/**
	 * @return true so that the vanilla block outline is never suppressed.
	 */
	private boolean onOverlay(WorldRenderContext context, HitResult hitResult) {
		Camera activeRenderInfo = context.camera();
		if (!(activeRenderInfo.getEntity() instanceof Player player)) {
			return true;
		}
		lastGameTime = mc.level == null ? 0 : mc.level.getGameTime();
		Level level = player.level();
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		if (stack.isEmpty() || !(stack.getItem() instanceof PhilosophersStone philoStone)) {
			transmutationResult = null;
			return true;
		}
		//Note: We use the philo stone's ray trace instead of the event's ray trace as we want to make sure that we
		// can properly take fluid into account/ignore it when needed
		BlockHitResult rtr = philoStone.getHitBlock(player);
		if (rtr.getType() != HitResult.Type.BLOCK) {
			transmutationResult = null;
			return true;
		}
		BlockState current = level.getBlockState(rtr.getBlockPos());
		transmutationResult = WorldTransmutations.getWorldTransmutation(current, player.isSecondaryUseActive());
		if (transmutationResult == null) {
			return true;
		}
		MultiBufferSource consumers = context.consumers();
		PoseStack matrix = context.matrixStack();
		if (consumers == null || matrix == null) {
			//Not inside the buffer-source window of the frame, nothing can be vertex-built here
			return true;
		}
		VertexConsumer builder = consumers.getBuffer(PERenderType.TRANSMUTATION_OVERLAY);
		Vec3 viewPosition = activeRenderInfo.getPosition();
		int charge = philoStone.getCharge(stack);
		byte mode = philoStone.getMode(stack);
		float alpha = ProjectEConfig.client.pulsatingOverlay.get() ? getPulseProportion() * 0.60F : 0.35F;
		matrix.pushPose();
		matrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);
		CollisionContext selectionContext = CollisionContext.of(player);
		for (BlockPos pos : PhilosophersStone.getChanges(level, rtr.getBlockPos(), player, rtr.getDirection(), mode, charge).keySet()) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir()) {
				VoxelShape shape = state.getShape(level, pos, selectionContext);
				if (!shape.isEmpty()) {
					matrix.pushPose();
					matrix.translate(pos.getX(), pos.getY(), pos.getZ());
					Matrix4f matrix4f = matrix.last().pose();
					shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> addBox(builder, matrix4f, alpha,
							(float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ));
					matrix.popPose();
				}
			}
		}
		matrix.popPose();
		return true;
	}

	private void addBox(VertexConsumer builder, Matrix4f matrix4f, float alpha, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		//Top
		builder.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();

		//Bottom
		builder.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, alpha).endVertex();

		//Front
		builder.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, alpha).endVertex();

		//Back
		builder.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, alpha).endVertex();

		//Left
		builder.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, alpha).endVertex();

		//Right
		builder.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, alpha).endVertex();
		builder.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, alpha).endVertex();
	}

	private float getPulseProportion() {
		return (float) (0.5F * Math.sin(System.currentTimeMillis() / 350.0) + 0.5F);
	}
}
