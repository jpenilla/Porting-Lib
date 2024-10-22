package io.github.fabricators_of_create.porting_lib.chunk.loading.mixin;

import io.github.fabricators_of_create.porting_lib.chunk.loading.ForcedChunkManager;
import io.github.fabricators_of_create.porting_lib.chunk.loading.extensions.DistanceManagerExtension;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.DistanceManager;

import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements DistanceManagerExtension {
	private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets = new Long2ObjectOpenHashMap<>();

	@Shadow
	public abstract <T> void addRegionTicket(TicketType<T> type, ChunkPos pos, int distance, T value);

	@Shadow
	public abstract <T> void removeRegionTicket(TicketType<T> type, ChunkPos pos, int distance, T value);

	@Override
	public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
		ForcedChunkManager.forceTicksToggle().set(forceTicks);
		addRegionTicket(pType, pPos, pDistance, pValue);
		ForcedChunkManager.forceTicksToggle().set(false);
	}

	@Override
	public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
		ForcedChunkManager.forceTicksToggle().set(forceTicks);
		removeRegionTicket(pType, pPos, pDistance, pValue);
		ForcedChunkManager.forceTicksToggle().set(false);
	}

	@Override
	public boolean shouldForceTicks(long chunkPos) {
		SortedArraySet<Ticket<?>> tickets = forcedTickets.get(chunkPos);
		return tickets != null && !tickets.isEmpty();
	}

	@Inject(method = "addTicket(JLnet/minecraft/server/level/Ticket;)V", at = @At("TAIL"))
	private void addForcedTicket(long chunkPos, Ticket<?> ticket, CallbackInfo ci) {
		if (ticket.isForceTicks()) {
			SortedArraySet<Ticket<?>> tickets = forcedTickets.computeIfAbsent(chunkPos, e -> SortedArraySet.create(4));
			tickets.addOrGet(ticket);
		}
	}

	@Inject(method = "removeTicket(JLnet/minecraft/server/level/Ticket;)V", at = @At("TAIL"))
	private void removeForcedTicket(long chunkPos, Ticket<?> ticket, CallbackInfo ci) {
		if (ticket.isForceTicks()) {
			SortedArraySet<Ticket<?>> tickets = forcedTickets.get(chunkPos);
			if (tickets != null) {
				tickets.remove(ticket);
			}
		}
	}
}
