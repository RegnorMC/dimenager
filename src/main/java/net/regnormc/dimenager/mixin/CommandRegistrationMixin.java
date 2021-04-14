package net.regnormc.dimenager.mixin;

import net.regnormc.dimenager.commands.DimensionCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandRegistrationMixin {
	@Shadow @Final
	private CommandDispatcher<ServerCommandSource> dispatcher;

	@Inject(
			method = "<init>",
			at = @At("RETURN")
	)
	private void registerCommands(CommandManager.RegistrationEnvironment commandSelection, CallbackInfo ci) {
		DimensionCommand.register(this.dispatcher);
	}
}
