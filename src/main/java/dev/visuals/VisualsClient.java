package dev.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("visuals");

	/** Klawisz otwierający menu (domyślnie O, do zmiany w ustawieniach sterowania). */
	public static KeyBinding openMenuKey;

	@Override
	public void onInitializeClient() {
		VisualsConfig.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("visuals", "main"));
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.visuals.open", GLFW.GLFW_KEY_O, category));

		ClientTickEvents.END_CLIENT_TICK.register(VisualsClient::onTick);

		// Zapamiętujemy, w kogo uderzył gracz, żeby po jego śmierci odpalić kill effect.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient()) {
				KillEffect.onAttack(entity);
			}
			return ActionResult.PASS;
		});

		// Chinese hat jako warstwa renderera gracza.
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
				registrationHelper.register(new HatFeatureRenderer(playerRenderer));
			}
		});

		LOGGER.info("Visuals załadowany. Menu otwierasz klawiszem O.");
	}

	private static void onTick(MinecraftClient client) {
		while (openMenuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new ConfigScreen());
			}
		}

		if (client.player == null || client.world == null) {
			LandingEffect.reset();
			KillEffect.reset();
			return;
		}

		TrailEffect.tick(client);
		LandingEffect.tick(client);
		KillEffect.tick(client);
	}
}
