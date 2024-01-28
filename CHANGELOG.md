# Changelog

##### 2.8.1

Fix plugin panel reloading causing RuneLite client size setting not working correctly. Fix config dropdown checkboxes to new ui style.  

#### 2.8

Added ability to keybind multiple presets to one keybind to easily toggle/cycle between presets. **Removed legacy plugin preset support.**

#### 2.7

**Fixed config changed event causing client to hang when profile was changed.** Profiles now finally work with no side effect to the plugin and now both system can be used in conjunction.

Fixed auto updater causing presets to disappear. Fixed some button context menu options.

Changed copy to clipboard button as duplicate preset button. Clipboard import is now context menu option on the green plus icon.

#### 2.6

Readded refresh presets button to plugin panel. Add load preset option to toggle switch context menu. Fixed auto updater not handling custom settings. Added error warning when preset (re)naming fails. Added way for selecting which preset is auto updated out of all matching presets. Added custom no presets panel with a wiki link.

#### 2.5

Added preset auto updating, plugin enabled/disabled filters and double click preset edit shortcut to preset panels.

Moved most of user help from Readme to Plugin Presets [wiki](https://github.com/antero111/plugin-presets/wiki)

Thanks to [Jademalo](https://github.com/Jademalo) for working with preset auto updating.

#### 2.4

Fixed custom settings causing infinite plugin toggle loop when loading presets. Thanks to [equirs](https://github.com/equirs)

Added partial config indicator.

Replaced custom setting checkbox with "remove" button.

##### 2.3.2

Annotated some classes as singletons so that the different areas of code that access those classes all talk to the same instance. This was preventing the custom settings from being "synced" properly between the preset's stored values and the panel's ui. CustomSettings changed to be a manager class.

Modified logic of duplicate custom settings now this checks for duplicates by both the config and setting name.

Various right click menus were previously only added upon right click. The menus are now added so that the first time they're right-clicked, they open the popup menu.

Added some documentation to various classes and functions.

Updates to the readme: some typo fixes and grammar adjustments, as well as some bits of additional info.

Contributed by [equirs](https://github.com/equirs)

##### 2.3.1

Added confirmation to removing not installed configurations from presets. Fixed _update all_ removing not installed configurations from presets.

#### 2.3

Presets are now refreshed automatically by detecting changes in the preset folder. Having multiple clients open and editing some preset causes that preset to also refresh on other open clients.

Added preset loading based on client focus.

Added open all/partial and close all buttons to config panel. Not included filter also shows partial configs. Fixed _Add all_ button to work with open configs.

Custom settings were mostly rewritten and fixed issues causing custom settings to be reset when adding new ones. Custom settings are now only visible to the preset where they are stored. Added confirmation to removing custom settings.

Improved edit panel and preset loading performance. Added and improved tooltips and fixed some wrong ui alingments. Added cloud icon to preset panel and improved some other icons.

Lots of code refactorings.

#### 2.2

Added _Included_ and _Not included_ filters and added a way to add custom settings e.g. for screen markers. Fixed duplicate configNames handling. Better dropdown arrows to edit panel and some other UX improvements.

#### 2.1

Configurations that don't have a name set can now be edited, this fixed volume sliders not being available in music plugin option. Added warning label for invalid plugin setting configurations. Preset switch now turns on correctly when loading presets if they contain invalid configurations.

### 2.0

The Plugin was mostly rewritten to use a new Plugin Preset file format and fit to a new way of editing presets with the editor panel. Previously presets contained all of your RuneLite configurations, now the presets only include those that user stores in them with the new preset editor panel. The new preset file format is much easier to work with and is much more future proof for future additions e.g. description.

Presets can now be also stored in the RuneLite configurations similiar to other RuneLite configurations. This allows having different presets with different RL accounts and syncing presets between computers.

Presets can now be bound to keybinds and multiple presets can be enabled at the same time. The preset panel now automatically knows if a preset is enabled or not by comparing the presets and users current configurations. Made improvements to preset importing and exporting, preset file saving and loading and performance improvements. Added icon for copying plugin preset data and updated some other icons. Lots of bugfixes and other various ux/ui fixes and code refactorings.

Old styled plugin presets will get convert to new style automatically after enabling the plugin.

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
