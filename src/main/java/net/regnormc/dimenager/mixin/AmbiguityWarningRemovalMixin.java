package net.regnormc.dimenager.mixin;

import net.regnormc.dimenager.Dimenager;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandManager.class)
public class AmbiguityWarningRemovalMixin {

	@Redirect(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V")
	)
	void removeAmbiguityWarnings(CommandDispatcher<ServerCommandSource> commandDispatcher, AmbiguityConsumer<ServerCommandSource> consumer) {
		if (!Dimenager.configuration.isRemoveAmbiguityWarnings())
			commandDispatcher.findAmbiguities(consumer); // If the config option is disabled we will still find all the ambiguities
	}
}
