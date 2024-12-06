## Utilis

**Author**: matejkoo  

Utilis is a robust and feature-rich plugin for beta 1.7.3 servers, offering useful utilities and tools to enhance the multiplayer experience.
From managing nicknames to toggling vanish mode, Utilis is designed to simplify server administration while adding fun things for players.

### Features

- **Vanish**: Toggle invisibility to other players with `/vanish`.
- **Nickname Management**: Set, reset, and customize player nicknames and colors.
- **Sleeping Skip Mode**: Manage sleeping mechanics with `/as`.
- **Inventory Management**: Use `/isee` to view and edit another player's inventory.
- **Item Teleportation**: Instantly pull all nearby items with `/suck`.
- **Player Listing**: View a detailed list of all connected players.
- **Debug Tools**: Diagnose plugin functionality with `/utilisdebug`.

### Commands

| Command        | Description                                                       | Usage                          | Aliases  |
|----------------|-------------------------------------------------------------------|--------------------------------|----------|
| `/vanish`      | Toggles vanish mode for the player.                               | `/vanish`                      | `/v`     |
| `/list`        | Lists all connected players.                                      | `/list`                        | -        |
| `/nickname`    | Set or change your nickname.                                      | `/nickname <nickname>`         | -        |
| `/rename`      | Rename another player (admin command).                            | `/rename <player> <nickname>`  | -        |
| `/color`       | Change your nickname color.                                       | `/color <color>`               | -        |
| `/realname`    | Returns the real name of a player based on their nickname.        | `/realname <nickname>`         | -        |
| `/nickreset`   | Resets your nickname to your original name.                       | `/nickreset`                   | -        |
| `/suck`        | Teleports all items around you to your inventory.                 | `/suck`                        | -        |
| `/as`          | Toggle sleeping mechanics.                                        | `/as toggle`                   | `/as t`  |
| `/utilisdebug` | Runs a debug check for the plugin.                                | `/utilisdebug`                 | -        |
| `/sudo`        | Force a player to execute a command or send a chat message.       | `/sudo <player> <command>`     | -        |
| `/isee`        | View and edit another player's inventory silently.                | `/isee <player>`               | -        |

### Permissions

| Permission          | Description                                       | Default |
|---------------------|---------------------------------------------------|---------|
| `utilis.debug`      | Allows access to the `/utilisdebug` command.      | `op`    |
| `utilis.sudo`       | Allows use of the `/sudo` command.                | `op`    |
| `utilis.nickname`   | Allows setting a nickname.                        | `true`  |
| `utilis.rename`     | Allows renaming other players.                    | `op`    |
| `utilis.color`      | Allows changing nickname color.                   | `true`  |
| `utilis.nickreset`  | Allows resetting your nickname.                   | `true`  |
| `utilis.realname`   | Allows retrieving the real name of a player.      | `true`  |
| `utilis.list`       | Allows listing all online players.                | `true`  |
| `utilis.suck`       | Allows teleporting all items to the player.       | `op`    |
| `utilis.vanish`     | Allows toggling vanish mode.                      | `op`    |
| `utilis.as`         | Allows using the `/as` command.                   | `op`    |
| `utilis.isee`       | Allows using the `/isee` command.                 | `op`    |

### Installation

1. Download the plugin from the releases section of this repository.
2. Place the `.jar` file into your server’s plugins folder.
3. Restart your server.

### Configuration

- Customize the plugin through config.yml and messages.yml . Both are located in the `plugins/Utilis` folder after the first run.
- Customizable messages.
- Color codes and placeholders.

### Contributing

Feel free to contribute to the development of Utilis. Fork this repository, make changes, and submit a pull request.

---

Enjoy using Utilis! For issues, feedback, or feature requests, please visit the Issues Section (or DM me at discord -matejkoo).