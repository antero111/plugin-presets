/*
 * Copyright (c) 2023, antero111 <https://github.com/antero111>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.pluginpresets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Setter;
import net.runelite.client.config.Keybind;

/**
 * Container for storing all keybinds from all plugins across all presets
 */
@Singleton
public class KeybindManager
{
	private final HashMap<Keybind, List<PluginPreset>> keybinds;

	@Setter
	private CurrentConfigurations currentConfigurations;

	@Inject
	public KeybindManager()
	{
		this.keybinds = new HashMap<>();
	}

	public void cacheKeybinds(final List<PluginPreset> pluginPresets)
	{
		keybinds.clear();
		pluginPresets.forEach(preset -> {
			final Keybind keybind = preset.getKeybind();
			if (keybind != null)
			{
				// try to add to existing keybind list
				if (keybinds.containsKey(keybind))
				{
					keybinds.get(keybind).add(preset);
				}
				else
				{
					final List<PluginPreset> list = new ArrayList<>();
					list.add(preset);
					keybinds.put(keybind, list);
				}
			}
		});
	}

	public PluginPreset getPresetFor(final Keybind keybind)
	{
		if (keybinds.containsKey(keybind))
		{
			return getNextPreset(keybinds.get(keybind));
		}
		return null;
	}

	public void clearKeybinds()
	{
		keybinds.clear();
	}

	/**
	 * Returns the next preset for the same keybind.
	 * This allows for cycling through presets with the same keybind.
	 *
	 * @param list Plugin preset list for certain keybind
	 */
	private PluginPreset getNextPreset(final List<PluginPreset> list)
	{
		int currentIndex = -1;
		for (int i = 0; i < list.size(); i++)
		{
			final PluginPreset preset = list.get(i);
			if (preset.match(currentConfigurations))
			{
				currentIndex = i;
				break;
			}
		}
		return list.get(currentIndex == list.size() - 1 ? 0 : currentIndex + 1);
	}
}
