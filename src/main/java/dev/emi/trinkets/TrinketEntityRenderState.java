package dev.emi.trinkets;

import dev.emi.trinkets.api.SlotReference;
import java.util.List;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;

public interface TrinketEntityRenderState {

	void trinkets$setState(List<Pair<ItemStack, SlotReference>> items);

	List<Pair<ItemStack, SlotReference>> trinkets$getState();
}
