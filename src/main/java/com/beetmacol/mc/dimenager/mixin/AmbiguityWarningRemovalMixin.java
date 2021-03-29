package com.beetmacol.mc.dimenager.mixin;

import com.beetmacol.mc.dimenager.Dimenager;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Commands.class)
public class AmbiguityWarningRemovalMixin {

	@Redirect(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V")
	)
	void removeAmbiguityWarnings(CommandDispatcher<CommandSourceStack> commandDispatcher, AmbiguityConsumer<CommandSourceStack> consumer) {
		if (!Dimenager.dimenagerConfiguration.isRemoveAmbiguityWarnings())
			commandDispatcher.findAmbiguities(consumer); // If the config option is disabled we will still find all the ambiguities
	}
}
