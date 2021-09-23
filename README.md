# RuneLite Plugin Presets plugin [![](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/plugin-presets)](https://runelite.net/plugin-hub)

This plugin adds the ability to create presets of your RuneLite plugin configurations.

![Plugin demo](readme_visuals/plugin_presets_demo.gif)

## Using Plugin Presets

Firstly, download the Plugin Presets plugin from the RuneLite Plugin Hub and enable it.

Click the green + sign to create a new _plugin preset_ from your current plugin configurations. Name your new preset and your current plugin settings are saved to that preset.

_Plugin presets_ are "copies" of your current plugin configurations from the moment of creating or modifying a preset. Creating a new preset means that Plugin Presets saves a copy of your current plugin configurations to be used later. To make new presets, simply modify your RuneLite plugin configurations as usual and create a new preset.

![Plugin create demo](readme_visuals/create_preset_demo.gif)

To load a preset, simply click the "load preset" button to enable configurations from that preset. **If you have "unsaved" configurations when loading to a different preset, those configurations are lost.**. Plugin Presets will notify you with an orange pen icon if you have unsaved plugin configurations.

Modifying presets happen by updating (overwriting) its configurations by pressing the pen icon.

![Plugin modify demo](readme_visuals/modify_preset_demo.gif)

When downloading external plugins from the Plugin Hub, new plugins won't be added to your existing presets. To fix this, simply update your presets with the new plugin loaded. Plugin Presets will notify you when you have plugins that are not saved to a certain preset with an orange pen icon.

![Unsaved plugin demo](readme_visuals/unsaved_plugins_demo.png)

Note that Plugin Presets does not check if you have already saved your RuneLite configurations to a preset. To stay organized, name your presets well and delete unnecessary duplicate configurations.

<details>
  <summary>How to revert back to default RuneLite settings</summary>
    If you need to revert back to default RuneLite settings, delete the <code>settings.properties</code> file from <code>~/.runelite/</code> and reload your client. This does not affect any of your plugin presets but <i>all</i> of your current plugin configurations will be set to default values. (This works for all plugins, not a Plugin Presets feature.)
</details>

### Sharing plugin presets

Your presets are stored in `~/.runelite/presets/`. You can share these .json files with others, they don't contain information about your account, RuneLite notes, Discord or Twitch. Alternatively you can right-click on of your presets and click the "Export preset to clipboard". This will copy the preset to your clipboard for easier sharing. **When sending presets to others, make sure to always double check for sensetive information!**

To "install" new presets, simply copy the .json file to the preset folder and then press the "refresh presets" button. Alternatively you can right-click the green + sign to import presets from clipboard. You can easily access your preset folder by right-clicking the Plugin Preset icon in the RuneLite sidebar.

Note that these presets contains _all_ of your plugin configurations, so when installing presets from others, they might have changed eg. keybinds that you might have set up differently.

Presets from others might have settings to plugins installed from the Plugin Hub. Plugin Presets will notify you when you have "uninstalled" plugins with a red warning sign.

![Missing plugins demo](readme_visuals/missing_plugins_demo.png)

If you don't want to install external plugins, enable the preset and update (overwrite) it with your new settings. This will discard the external plugin settings from the preset.

## Issues

If you've experienced an issue with Plugin Presets, or have a recommendation on how to improve it, please [create an issue](https://github.com/antero111/plugin-presets/issues/new) with the relevant details.

### Changelog

#### 1.6

Presets now include RuneLite client settings. Added ability to ignore certain plugin setting keys, fixes some sensitive data showing in shareable presets.

#### 1.5

Plugin now warns from unsaved configurations when making changes to plugin configurations. Fixed presets not staying in alphabethical order when renaming them. Preset renaming improvements. Preset names now contains more valid characters. Fixed bug causing all presets disappearing when turning the plugin off.

#### 1.4

Added unsaved plugin configuration detection/notification and tooltips to preset renaming.
Removed preset unloading, caused unnessesscary issues and confusion. Fixed some tooltip typos.

#### 1.3

Added the ability to import/export presets to and from clipboard. Added error notificator. Fixed it so that presets will now stay in alphabethical order when they are created.

#### 1.2

Added unsaved/uninstalled external plugin notification, plugin icon. Changed "overwrite" to "update".

#### 1.1

Added the ability to unload a preset. Fixed unsaved Plugin Hub plugin handling

#### 1.0

Plugin added
