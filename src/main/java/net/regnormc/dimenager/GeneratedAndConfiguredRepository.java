package net.regnormc.dimenager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;

public abstract class GeneratedAndConfiguredRepository<T extends GeneratedItem, U> extends GeneratedRepository<T> {
	protected final Map<Identifier, U> items = new HashMap<>();
	protected ResourceManager resourceManager;

	protected GeneratedAndConfiguredRepository(ResourceManager resourceManager, LevelStorage.Session levelStorageAccess, String itemGeneralName) {
		super(levelStorageAccess, itemGeneralName);
		this.resourceManager = resourceManager;
	}

	@Override
	public void addGeneratedItem(T generatedItem) {
		addGeneratedItem(generatedItem, null);
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
	public boolean contains(Identifier identifier) {
		return items.containsKey(identifier);
	}

	public boolean containsGenerated(Identifier identifier) {
		return super.contains(identifier);
	}

	public U get(Identifier identifier) {
		return items.get(identifier);
	}

	public Collection<Identifier> getIdentifiers() {
		return items.keySet();
	}
}
