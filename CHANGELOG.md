# Changelog

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
