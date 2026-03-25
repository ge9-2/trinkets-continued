package dev.emi.trinkets.mixin.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * This is main schema where Minecraft defines most post-flattening data formats.
 * Trinkets injects here adding support for basic datafixing in case of other mods supporting it or just general vanilla nbt
 * format changes.
 *
 * @author Patbox
 */
@Mixin(V1460.class)
public class V1460Mixin {

	@Unique
	private static Schema schema;

	@Inject(method = "registerTypes", at = @At("HEAD"))
	private void captureSchema(
			Schema schemax,
			Map<String, Supplier<TypeTemplate>> entityTypes,
			Map<String, Supplier<TypeTemplate>> blockEntityTypes,
			CallbackInfo ci
	) {
		schema = schemax;
	}

	@ModifyArgs(
			method = "registerTypes",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/schemas/Schema;registerType(ZLcom/mojang/datafixers/DSL$TypeReference;Ljava/util/function/Supplier;)V"
			)
	)
	private void wrapPlayerAndEntity(Args args) {
		DSL.TypeReference reference = args.get(1);
		Supplier<TypeTemplate> original = args.get(2);

		if (reference != References.PLAYER && reference != References.ENTITY) {
			return;
		}

		args.set(2, (Supplier<TypeTemplate>) () -> {
			TypeTemplate template = original.get();

			return DSL.allWithRemainder(
					DSL.optional(
							DSL.field("cardinal_components",
									DSL.optionalFields("trinkets:trinkets",
											DSL.optional(DSL.compoundList(
													DSL.optional(DSL.compoundList(
															DSL.optionalFields("Items",
																	DSL.list(References.ITEM_STACK.in(schema))
															)
													))
											))
									)
							)
					),
					template
			);
		});
	}
}
