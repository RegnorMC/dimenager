package com.beetmacol.mc.dimenager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GeneratedAndConfiguredRepository<T extends GeneratedItem, U> extends GeneratedRepository<T> {
	protected final Map<ResourceLocation, U> items = new HashMap<>();
	protected ResourceManager resourceManager;

	protected GeneratedAndConfiguredRepository(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, String itemGeneralName) {
		super(levelStorageAccess, itemGeneralName);
		this.resourceManager = resourceManager;
	}

	@Override
	public void addGeneratedItem(T item) {
		super.addGeneratedItem(item);
		items.put(item.getIdentifier(), null);
	}

	public void addGeneratedItem(T generatedItem, U item) {
		super.addGeneratedItem(generatedItem);
		items.put(generatedItem.getIdentifier(), item);
	}

	@Override
	public void resourceManagerReload(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		reload();
	}

	@Override
	public void reload() {
		items.clear();
		addConfiguredItems();
		super.reload();
	}

	protected abstract void addConfiguredItems();

	@Override
	public boolean contains(ResourceLocation identifier) {
		return items.containsKey(identifier);
	}

	public boolean containsGenerated(ResourceLocation identifier) {
		return super.contains(identifier);
	}

	public U get(ResourceLocation identifier) {
		return items.get(identifier);
	}

	public Collection<ResourceLocation> getIdentifiers() {
		return items.keySet();
	}
}
