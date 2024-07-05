# Vault Hunters Player Reset

TODO: add image for resetting

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
