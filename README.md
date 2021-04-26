# Runelite Plugin Presets plugin

This plugin adds the ability to create presets of your Runelite plugin configurations.

![Plugin demo](readme_visuals/plugin_presets_demo.gif)

## Using Plugin Presets

Firstly, download the Plugin Presets plugin from the Runelite Plugin Hub and enable it.

Click the green + sign to create a new _plugin preset_ from your current plugin configurations. Name your new preset and your current plugin settings are saved to that preset.

_Plugin presets_ are "copies" of your current plugin configurations from the moment of creating or modifying a preset. Creating a new preset means that Plugin Presets saves a copy of your current plugin configurations. So to make new presets, simply modify your Runelite plugin configurations as usual and create a new preset.

![Plugin create demo](readme_visuals/create_preset_demo.gif)

To load a preset, simply click the "load preset" button to enable configurations from that preset. **If you have "unsaved" configurations when loading to a different preset, those configurations are lost**.

Modifying presets happen by _overwriting_ its configurations by pressing the pen icon.

![Plugin modify demo](readme_visuals/modify_preset_demo.gif)

Note that Plugin Presets does not check if you have already saved your Runelite configurations to a preset. To stay organized, name your presets well and delete unnecessary duplicate configurations.

<details>
  <summary>How to revert back to default Runelite settings?</summary>
    If you need to revert back to default Runelite settings, delete the `settings.properties` file from `~/.runelite/` and reload your client. This does not affect any of your presets but your current plugin configurations will be set to default values. (This works for all plugins, not a Plugin Presets feature.)
</details>

### Sharing your Plugin Presets

Your presets are stored in `~/.runelite/presets/`. You can share these .json files with others, they don't contain information about your account, runelite notes, Discord or Twitch. **When sending presets to others, make sure to allways double check for sensetive information!**

To "install" new presets, simply copy the .json file to the preset folder and then press the "refresh plugins" button. You can easily access your preset folder by right clicking the Plugin Preset icon in the Runelite sidebar.

Note that these presets contains _all_ of your plugin configurations, so when installing presets from others, they might have changed eg. keybinds that you might have set up differently. Allways make sure to keep backups of your configurations when trying to install presets from others.

## Issues

If you've experienced an issue with Plugin Presets, or have a recommendation on how to improve it, please [create an issue](https://github.com/antero111/plugin-presets/issues/new) with the relevant details.
