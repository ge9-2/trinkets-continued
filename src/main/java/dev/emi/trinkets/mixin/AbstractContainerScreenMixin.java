package dev.emi.trinkets.mixin;


import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.emi.trinkets.CreativeTrinketScreen;
import dev.emi.trinkets.TrinketScreenManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.TrinketsClient;
import dev.emi.trinkets.mixin.accessor.SlotWrapperAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

/**
 * Draws trinket slot backs, adjusts z location of draw calls, and makes non-trinket slots un-interactable while a trinket slot group is focused
 * 
 * @author Emi
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	@Shadow @Nullable protected Slot hoveredSlot;
	@Shadow @Final private static Identifier SLOT_HIGHLIGHT_BACK_SPRITE;
	@Unique
	private static final Identifier MORE_SLOTS = Identifier.fromNamespaceAndPath("trinkets", "textures/gui/more_slots.png");
	@Unique
	private static final Identifier BLANK_BACK = Identifier.fromNamespaceAndPath("trinkets", "textures/gui/blank_back.png");

	private AbstractContainerScreenMixin() {
		super(null);
	}

	@Inject(at = @At("HEAD"), method = "removed")
	private void removed(CallbackInfo info) {
		if ((Object)this instanceof InventoryScreen) {
			TrinketScreenManager.removeSelections();
		}
	}

	@WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V"),
			method = "extractSlots")
	private boolean preventDrawingSlots(AbstractContainerScreen instance, GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
		return !(slot instanceof TrinketSlot trinketSlot) || !trinketSlot.renderAfterRegularSlots();
	}

	@Inject(at = @At("HEAD"), method = "extractSlot")
	private void drawSlotBackground(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
		if (slot instanceof TrinketSlot ts) {
			assert this.minecraft != null;
			Identifier slotTextureId = ts.getBackgroundIdentifier();

			if (!slot.getItem().isEmpty() || slotTextureId == null) {
				slotTextureId = BLANK_BACK;
			}

			if (ts.isTrinketFocused()) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, slotTextureId, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
				if (this.hoveredSlot == slot && this.hoveredSlot.isHighlightable()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, this.hoveredSlot.x - 4, this.hoveredSlot.y - 4, 24, 24);
				}
			} else {
				graphics.blit(RenderPipelines.GUI_TEXTURED, slotTextureId, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
				graphics.blit(RenderPipelines.GUI_TEXTURED, MORE_SLOTS, slot.x - 1, slot.y - 1, 4, 4, 18, 18, 256, 256);
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractContents(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", shift = At.Shift.AFTER), method = "extractRenderState")
	private void renderCreativeSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
		if (this instanceof CreativeTrinketScreen screen) {
			screen.trinkets$renderCreative(graphics, mouseX, mouseY, deltaTicks);
		}
	}

	@Inject(at = @At("HEAD"), method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", cancellable = true)
	private void isPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> info) {
		if (TrinketsClient.activeGroup != null) {
			if (slot instanceof TrinketSlot ts) {
				if (!ts.isTrinketFocused()) {
					info.setReturnValue(false);
				}
			} else {
				if (slot instanceof SlotWrapper cs) {
					if (((SlotWrapperAccessor) cs).getSlot().index != TrinketsClient.activeGroup.getSlotId()) {
						info.setReturnValue(false);
					}
				} else if (slot.index != TrinketsClient.activeGroup.getSlotId()) {
					info.setReturnValue(false);
				}
			}
		}
	}
}
