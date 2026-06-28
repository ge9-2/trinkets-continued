package dev.emi.trinkets.mixin;
import com.mojang.datafixers.util.Pair;

import dev.emi.trinkets.TrinketEntityRenderState;
import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements TrinketEntityRenderState {

    @Unique
    private List<Pair<ItemStack, SlotReference>> trinketsState = new ArrayList<>();

    @Override
    public void trinkets$setState(List<Pair<ItemStack, SlotReference>> items) {
        this.trinketsState = items;
    }

    @Override
    public List<Pair<ItemStack, SlotReference>> trinkets$getState() {
        return this.trinketsState;
    }
}
