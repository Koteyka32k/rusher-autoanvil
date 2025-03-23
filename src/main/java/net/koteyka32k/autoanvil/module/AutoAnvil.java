package net.koteyka32k.autoanvil.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.events.render.EventRender3D;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.WorldUtils;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.NumberSetting;
import org.rusherhack.core.utils.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Credits go to Kybe246 for open source airplace.
 * Credits go to John200401 for too bee too tee prio queue to test.
 *
 * Behold, code is crappy but works :thumbsup~1:
 * I forgot what some things do, but it works.
 *
 *
 * @author Koteyka32k
 * @since long ago
 */

public class AutoAnvil extends ToggleableModule {

	private final NumberSetting<Float> range = new NumberSetting<>("Range", "How far the anvil could be places (from player to blockpos).", 4.25f, 1f, 5f)
			.incremental(0.05f);
	private final BooleanSetting swing = new BooleanSetting("Swing", "Whether to send the swing packet.", true);
	private final NumberSetting<Integer> headPlaceYOffset = new NumberSetting("Y Offset", "How high up the anvil is places above the player's head.", 3, 1, 5);
	private final NumberSetting<Integer> tickDelay = new NumberSetting<>("Tick Delay", "Delay between places, in ticks.", 5, 0, 20);
	public AutoAnvil() {
		super("AutoAnvil", "too bee too tee auto anvil (airplace bypass included)", ModuleCategory.COMBAT);

		this.registerSettings(
				range,
				swing,
				headPlaceYOffset,
				tickDelay
		);
	}

	List<BlockPos> tefsd = new ArrayList<>();
	private final Timer tickTimer = new Timer();

	@Subscribe(stage = Stage.POST)
	private void onUpdate(EventUpdate update) {
		if (mc.level != null && mc.player != null && tickTimer.ticksPassed(tickDelay.getValue())) {
			tickTimer.reset();
			tefsd.clear();
			List<Player> targets = getClosestTargets();
			for (Player p : targets) {
				tefsd.add(new BlockPos(p.getBlockX(), p.getBlockY() + 1 + headPlaceYOffset.getValue(), p.getBlockZ()));
				rotateToBlock(new BlockPos(p.getBlockX(), p.getBlockY() + 1 + headPlaceYOffset.getValue(), p.getBlockZ()));
				placeTheAnvil(new BlockPos(p.getBlockX(), p.getBlockY() + 1 + headPlaceYOffset.getValue(), p.getBlockZ()), 0);
			}

		}
	}

	@Subscribe
	private void renderBPs(EventRender3D event) {
		event.getRenderer().begin(event.getMatrixStack());
		for (BlockPos pos : tefsd) {
			event.getRenderer().setLineWidth(2);
		    event.getRenderer().drawBox(pos, true, true, 0x2f7fff00);
		}

		event.getRenderer().end();
	}

	private List<Player> getClosestTargets() {
		List< Entity> entities = WorldUtils.getEntitiesSorted();
		List<Player> players = new ArrayList<>();
		for (Entity entity : entities) {
			if (entity instanceof Player && !entity.equals(mc.player)) {
				BlockPos b = entity.blockPosition().above(1 + headPlaceYOffset.getValue());
				boolean check1range = true;
				boolean check2airblock = true;
				boolean check3fallingentityalreadythere = true;
				if (mc.player.position().distanceTo(entity.position().add(0, 1 + headPlaceYOffset.getValue(), 0)) > range.getValue()) {
					check1range = false;
				}
				if (!mc.level.getBlockState(b).isAir()) {
					check2airblock = false;
				}
				for (Entity e9 : WorldUtils.getEntitiesSorted()) {
					if (e9 instanceof FallingBlockEntity && e9.getBlockX() == b.getX() && (e9.getBlockY() >= b.getY() && e9.getBlockY() <= b.getY() + 4) && e9.getBlockZ() == b.getZ()) {
						check3fallingentityalreadythere = false;
					}
				}
				if (check1range && check2airblock && check3fallingentityalreadythere) {
					players.add((Player) entity);
				}
			}
		}

		return players;
	}

	private void rotateToBlock(BlockPos pos) {
		if (pos != null) RusherHackAPI.getRotationManager().updateRotation(pos);
	}


	/**
	 * This was yoinked from kybe.
	 */
	private void placeTheAnvil(BlockPos pos, float partialTicks) {
		if (pos == null) return;
		BlockHitResult thit = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.DOWN, pos, false);

		boolean main = mc.player.getMainHandItem().getItem() instanceof BlockItem;
		boolean off = mc.player.getOffhandItem().getItem() instanceof BlockItem;
		mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));

		InteractionHand hand = main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		hand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

		mc.gameMode.useItemOn(mc.player, hand, thit);

		if (swing.getValue()) mc.player.swing(main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
		else mc.player.connection.send(new ServerboundSwingPacket(hand));

		mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.UP));
	}
}
