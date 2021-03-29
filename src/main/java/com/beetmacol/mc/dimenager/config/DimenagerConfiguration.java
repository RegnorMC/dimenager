package com.beetmacol.mc.dimenager.config;

import com.beetmacol.mc.dimenager.Dimenager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class DimenagerConfiguration {
	private final boolean modifyTpCommand;
	private final boolean removeAmbiguityWarnings;

	public DimenagerConfiguration(boolean modifyTpCommand, boolean removeAmbiguityWarnings) {
		this.modifyTpCommand = modifyTpCommand;
		this.removeAmbiguityWarnings = removeAmbiguityWarnings;
	}

	public static DimenagerConfiguration fromJson(JsonObject jsonObject) throws JsonSyntaxException {
		return new DimenagerConfiguration(
				GsonHelper.getAsBoolean(jsonObject, "modify_tp_command", true),
				GsonHelper.getAsBoolean(jsonObject, "remove_command_ambiguity_warns", false)
		);
	}

	public static DimenagerConfiguration readConfiguration() {
		File file = new File("config/dimenager.json");
		if (!file.exists()) {
			URL defaultConfig = Dimenager.class.getClassLoader().getResource("default_config.json");
			if (defaultConfig == null) {
				Dimenager.LOGGER.fatal("The default Dimenager's configuration file does not exist in the classpath! will use hardcoded defaults");
				return hardcodedDefaultConfiguration();
			}
			try {
				FileUtils.copyURLToFile(defaultConfig, file);
			} catch (IOException exception) {
				Dimenager.LOGGER.error("Could not copy the default configuration file to the 'config' directory; will use hardcoded defaults");
				return hardcodedDefaultConfiguration();
			}
		}
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(file));
			return fromJson(new JsonParser().parse(jsonReader).getAsJsonObject());
		} catch (IllegalStateException | JsonSyntaxException exception) {
			Dimenager.LOGGER.error("Could not parse the configuration, will use the hardcoded defaults", exception);
			return hardcodedDefaultConfiguration();
		} catch (FileNotFoundException exception) {
			throw new IllegalStateException("File not found after it was created");
		}
	}

	public static DimenagerConfiguration hardcodedDefaultConfiguration() {
		return new DimenagerConfiguration(true, false);
	}

	public boolean isModifyTpCommand() {
		return modifyTpCommand;
	}

	public boolean isRemoveAmbiguityWarnings() {
		return removeAmbiguityWarnings;
	}
}
