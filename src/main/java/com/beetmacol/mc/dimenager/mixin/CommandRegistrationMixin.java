package com.beetmacol.mc.dimenager.mixin;

import com.beetmacol.mc.dimenager.commands.DimensionCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandRegistrationMixin {
	@Shadow @Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(
			method = "<init>",
			at = @At("RETURN")
	)
	private void registerCommands(Commands.CommandSelection commandSelection, CallbackInfo ci) {
		DimensionCommand.register(this.dispatcher);
	}
}
