# Vault Hunters Player Reset

TODO: add image for repo

This mod adds a few simple commands for resetting vault data associated with a player.
It uses a separate command, `reset_player`, instead of the built-in `reset` to avoid conflicting commands.

## Installation

Add this mod to the server mods folder. If only playing on a client, place it in the client's mods folder.

## Usage

For all usage commands, replace `BobJones` with the player name. All commands require moderator level or above to
execute.

### Complete reset

For a complete reset of **all** commands from the [Other Commands](#other-commands) section at once, use the `all` command:

```
/the_vault reset_player all BobJones
```

This will print a message to verify the command. Either click the response text or re-enter the command within 10 seconds.
If you take longer than ten seconds to repeat the command, it will time out and the process will restart.

This will not reset Minecraft achievements for the player. If you also want to reset player achievements, use this
command:

```
/advancement revoke BobJones everything
```

### Other Commands

For all other commands, replace `<command>` with the required command:

```
/the_vault reset_player <command> BobJones
```

For example, to reset trinkets:

```
/the_vault reset_player trinkets BobJones
```

The possible commands are as follows:

- `level`
- `skills_and_abilities`
- `expertise`
- `knowledge`
- `research`
- `bounties`
- `relics`
- `armor_models`
- `trinkets`
- `workbench_modifiers`
- `potion_modifiers`
- `paradox`
- `god_reputation`
- `quests`
- `vault_history`
- `altar_level`
- `altar_recipe`
- `ascension_title`
- `proficiencies`

## License

This project is MIT-licensed. Please see [LICENSE.txt](./LICENSE.txt) for more information.
