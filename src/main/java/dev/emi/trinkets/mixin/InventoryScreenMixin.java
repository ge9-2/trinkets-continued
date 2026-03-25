package dev.emi.trinkets.mixin;

import dev.emi.trinkets.mixin.accessor.AbstractRecipeBookScreenAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.trinkets.Point;
import dev.emi.trinkets.TrinketPlayerScreenHandler;
import dev.emi.trinkets.TrinketScreen;
import dev.emi.trinkets.TrinketScreenManager;
import dev.emi.trinkets.api.SlotGroup;

/**
 * Delegates drawing and slot group selection logic
 * 
 * @author Emi
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements RecipeUpdateListener, TrinketScreen {
	private InventoryScreenMixin() {
		super(null, null, null, null);
	}

	@Inject(at = @At("HEAD"), method = "init")
	private void init(CallbackInfo info) {
		TrinketScreenManager.init(this);
	}

	@Inject(at = @At("TAIL"), method = "containerTick")
	private void tick(CallbackInfo info) {
		TrinketScreenManager.tick();
	}

	@Inject(at = @At("HEAD"), method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
	private void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo info) {
		TrinketScreenManager.update(mouseX, mouseY);
	}

	@Inject(at = @At("RETURN"), method = "extractBackground")
	private void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		TrinketScreenManager.drawExtraGroups(graphics);
	}

	@Override
	public TrinketPlayerScreenHandler trinkets$getHandler() {
		return (TrinketPlayerScreenHandler) this.menu;
	}
	
	@Override
	public Rect2i trinkets$getGroupRect(SlotGroup group) {
		Point pos = ((TrinketPlayerScreenHandler) menu).trinkets$getGroupPos(group);
		if (pos != null) {
			return new Rect2i(pos.x() - 1, pos.y() - 1, 17, 17);
		}
		return new Rect2i(0, 0, 0, 0);
	}

	@Override
	public Slot trinkets$getFocusedSlot() {
		return this.hoveredSlot;
	}

	@Override
	public int trinkets$getX() {
		return this.leftPos;
	}

	@Override
	public int trinkets$getY() {
		return this.topPos;
	}

	@Override
	public boolean trinkets$isRecipeBookOpen() {
		return ((AbstractRecipeBookScreenAccessor) this).getRecipeBookComponent().isVisible();
	}
}
