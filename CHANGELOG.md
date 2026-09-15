**This release includes major breaking changes for all other mods that interact with this mod!**

- Added multiloader support for NeoForge
- Added support for 26.3
- The mod ID has been changed to `female_gender_mod`
- Player configurations have been largely rewritten, and now save in a more structured format
- Armor rendering now uses vanilla rendering methods where possible
  - Please report any visual issues that may be caused by this change, especially in regard to armor trims and modded armors!
- Syncing with the connected server (if supported) has also been heavily changed:
  - The mod now requires both sides to support a hello packet in the configuration phase, and will not attempt to sync
    unless both sides support the same sync protocol version
  - The play phase sync packet IDs have been changed to `female_gender_mod:{client,server}bound/sync`
  - Sync packets between the client and server will now omit irrelevant data where possible
- Fixed an issue with default skins using the wrong texture path in the UV Editor screen
- Fix rendering of upside down entities in UIs (for players like Dinnerbone, Grumm, or any other names mods may tweak to be upside down)
- Updated/added translations for Russian, Turkish, Spanish, Chinese, LOLCAT, and various English locales (Canadian, British, Australian, and Upside Down)
- Holiday themes have been removed
