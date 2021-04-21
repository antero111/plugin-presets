package com.pluginpresets;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginPresetsPluginTest {
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(PluginPresetsPlugin.class);
		RuneLite.main(args);
	}
}