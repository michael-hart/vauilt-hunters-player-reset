# Vault Hunters Player Reset

TODO: add image for repo

This mod adds a few simple commands for resetting vault data associated with a player.
It uses a separate command, `reset_player`, instead of the built-in `reset` to avoid conflicting.

## Installation

Add this mod to the server mods folder. If only playing on a client, place it in the client's mods folder.

## Usage

For all usage commands, replace `BobJones` with the player name. All commands require moderator level or above to
execute.

### Complete reset

For a complete reset, including level, skills, expertise, bounties, relics, and unlocked transmogs/workbench
modifiers/potion effects/trinkets, use the `all` command:

```
/the_vault reset_player all BobJones
```

### Other Commands

For all other commands, replace `<command>` with the required command:

```
/the_vault reset_player <command> BobJones
```

To reset trinkets:

```
/the_vault reset_player trinkets BobJones
```

The possible commands are as follows:
- `bounties`
- `relics`
- `armor_models`
- `trinkets`
- `workbench_modifiers`
- `paradox`

## License

This project is MIT-licensed. Please see [LICENSE.txt](./LICENSE.txt) for more information.

## TO DO

- Reputation (add 25 to one; remove 25 to one) (add to README, Changelog)
- Potions (add to README, Changelog)
- Quests (add to README, Changelog)
- Black Market (/the_vault internal[ascension.json](..%2F..%2Fcurseforge%2Fminecraft%2FInstances%2FBackup%20Millenium%20Vault%20Hunters%2Fconfig%2Fthe_vault%2Fascension.json) reset_black_market Jrowez)
- Altar reset (/the_vault altar reset)
- Altar level (/the_vault debug altar_level set 1)

- Achievements (questline)
  - /advancements revoke Jrowez everything
- Vault History
- Ascension
- Verification for `all`: /execute as Jrowez run the_vault points_reset knowledge
