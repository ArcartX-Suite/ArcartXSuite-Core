package xuanmo.arcartxsuite.api.capability;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability for operating a player's extra backpack without opening its UI.
 */
public interface ExtraBackpackAccess {

    /**
     * Returns whether the extra backpack is enabled and usable for the player.
     */
    boolean isAvailable(@NotNull Player player);

    /**
     * Returns a snapshot of the player's categories, capacities, and occupied slots.
     */
    @NotNull Snapshot snapshot(@NotNull Player player);

    /**
     * Stores as much of the item stack as the matching extra-backpack category can hold.
     */
    @NotNull DepositResult deposit(@NotNull Player player, @NotNull ItemStack itemStack);

    /**
     * Removes up to {@code amount} items from one category slot.
     */
    @NotNull WithdrawResult withdraw(
        @NotNull Player player,
        @NotNull String categoryId,
        int slot,
        int amount
    );

    record Snapshot(boolean available, @NotNull List<CategorySnapshot> categories) {
        public Snapshot {
            categories = List.copyOf(categories);
        }
    }

    record CategorySnapshot(
        @NotNull String categoryId,
        long capacity,
        long used,
        @NotNull List<SlotSnapshot> slots
    ) {
        public CategorySnapshot {
            slots = List.copyOf(slots);
        }
    }

    record SlotSnapshot(int slot, @NotNull ItemStack itemStack) {
    }

    record DepositResult(
        boolean success,
        long storedAmount,
        int remainingAmount,
        @NotNull String message
    ) {
    }

    record WithdrawResult(
        boolean success,
        int takenAmount,
        @Nullable ItemStack itemStack,
        @NotNull String message
    ) {
    }
}
