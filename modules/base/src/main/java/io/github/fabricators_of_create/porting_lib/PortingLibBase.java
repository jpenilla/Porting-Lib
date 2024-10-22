package io.github.fabricators_of_create.porting_lib;

import ca.spottedleaf.moonrise.fabric.FabricHooks;
import io.github.fabricators_of_create.porting_lib.command.ConfigCommand;
import io.github.fabricators_of_create.porting_lib.command.EnumArgument;
import io.github.fabricators_of_create.porting_lib.event.common.ChunkTrackingCallback;
import io.github.fabricators_of_create.porting_lib.event.common.ExplosionEvents;
import io.github.fabricators_of_create.porting_lib.event.common.ModsLoadedCallback;
import io.github.fabricators_of_create.porting_lib.util.DeferredSpawnEggItem;
import io.github.fabricators_of_create.porting_lib.util.UsernameCache;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.fabricators_of_create.porting_lib.command.ModIdArgument;
import io.github.fabricators_of_create.porting_lib.core.PortingLib;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemItemStorages;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class PortingLibBase implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Porting Lib Base");
	@Override
	public void onInitialize() {
		ItemItemStorages.init();
		UsernameCache.load();
		// can be used to force all mixins to apply
		// MixinEnvironment.getCurrentEnvironment().audit();

		ArgumentTypeRegistry.registerArgumentType(PortingLib.id("modid"), ModIdArgument.class,
				SingletonArgumentInfo.contextFree(ModIdArgument::modIdArgument));
		ArgumentTypeRegistry.registerArgumentType(PortingLib.id("enum"), EnumArgument.class,
				new EnumArgument.Info());

		CommandRegistrationCallback.EVENT.register(ConfigCommand::register);

		ModsLoadedCallback.EVENT.register(envType -> DeferredSpawnEggItem.init());

		if (FabricLoader.getInstance().isModLoaded("moonrise")) {
			FabricHooks.ON_EXPLOSION_DETONATE.register((level, explosion, entities, diameter) -> {
				ExplosionEvents.DETONATE.invoker().onDetonate(level, explosion, entities, diameter);
			});
			FabricHooks.ON_CHUNK_WATCH.register((level, chunk, player) -> {
				ChunkTrackingCallback.WATCH.invoker().onChunkWatch(player, chunk, level);
			});
			FabricHooks.ON_CHUNK_UNWATCH.register((level, chunk, player) -> {
				ChunkTrackingCallback.UNWATCH.invoker().onChunkUnwatch(player, chunk, level);
			});
		}
	}
}
