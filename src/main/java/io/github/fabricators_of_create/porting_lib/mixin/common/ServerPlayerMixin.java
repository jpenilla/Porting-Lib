package io.github.fabricators_of_create.porting_lib.mixin.common;

import io.github.fabricators_of_create.porting_lib.event.PlayerTickEvents;

import io.github.fabricators_of_create.porting_lib.event.common.PlayerEvents;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import io.github.fabricators_of_create.porting_lib.event.ServerPlayerCreationCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

	@Shadow
	public abstract ServerLevel getLevel();

	public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void port_lib$init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, CallbackInfo ci) {
		ServerPlayerCreationCallback.EVENT.invoker().onCreate((ServerPlayer) (Object) this);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void port_lib$clientStartTickEvent(CallbackInfo ci) {
		PlayerTickEvents.START.invoker().onStartOfPlayerTick(this);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void port_lib$clientEndTickEvent(CallbackInfo ci) {
		PlayerTickEvents.END.invoker().onEndOfPlayerTick(this);
	}

	@Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 5, shift = At.Shift.AFTER))
	public void port_lib$onChangeDimension(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
		PlayerEvents.CHANGED_DIMENSION.invoker().onChangedDimension(this, getLevel().dimension(), destination.dimension());
	}

	@Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
	public void port_lib$onTeleportToDimension(ServerLevel newLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
		PlayerEvents.CHANGED_DIMENSION.invoker().onChangedDimension(this, getLevel().dimension(), newLevel.dimension());
	}
}
