## Utilis

**Author**: ~jkoo

Utilis is a robust and feature-rich plugin for beta 1.7.3 servers, offering useful utilities and tools to enhance the multiplayer experience.
From managing nicknames to toggling vanish mode, Utilis is designed to simplify server administration while adding fun things for players.

### Features

- **Vanish**: Toggle invisibility to other players with `/vanish`.
- **Nickname Management**: Set, reset, and customize player nicknames and colors.
- **Night Skip**: Manage night-skip mechanics with `/ns`.
- **SleepMessage**: Manage custom sleep messages with `/sm`.
- **Inventory Management**: Use `/isee` to view and edit another player's inventory.
- **Inventory Recovery**: Recover a player's inventory after death using /recover.
- **Item Teleportation**: Instantly pull all nearby items with `/suck`.
- **Minecart Fix**: Stops players from getting fall damage.
- **Player Listing**: View a detailed list of all connected players.
- **Anti-Spam**: Stops players from spamming messages.
- **Debug Tools**: Diagnose plugin functionality with `/utilisdebug`.

### Commands

| Command        | Description                                                       | Usage                          | Aliases  |
|----------------|-------------------------------------------------------------------|--------------------------------|----------|
| `/vanish`      | Toggles vanish mode for the player.                               | `/v, /v s/silent, /v player`   | `/v`     |
| `/list`        | Lists all connected players.                                      | `/list`                        | -        |
| `/whitelist`   | Toggles functionality of whitelist.                               | `/wl`                          | -/wl     |
| `/nickname`    | Set or change your nickname.                                      | `/nickname <nickname>`         | -        |
| `/rename`      | Rename another player (admin command).                            | `/rename <player> <nickname>`  | -        |
| `/color`       | Change your nickname color.                                       | `/color <color>`               | -        |
| `/realname`    | Returns the real name of a player based on their nickname.        | `/realname <nickname>`         | -        |
| `/nickreset`   | Resets your nickname to your original name.                       | `/nickreset`                   | -        |
| `/suck`        | Teleports all items around you to you.                            | `/suck`                        | -        |
| `/ns`          | Toggle sleeping mechanics in current world.                       | `/ns toggle`                   | `/ns t`  |
| `/sm`          | Displays sleepmessages features.                                  | `/sleepmessage set <message>`  | `/sm`    |
| `/utilisdebug` | Runs a debug check for the plugin.                                | `/utilisdebug`                 | -        |
| `/sudo`        | Force a player to execute a command or send a chat message.       | `/sudo <player> <command>`     | -        |
| `/isee`        | View and edit another player's inventory.                         | `/isee <player>`               | -        |
| `/recover`     | Recover a player's inventory after they die.                      | `/recover <player>`            | -        |

### Lot of permissions!

| Permission               | Description                                       | Default |
|--------------------------|---------------------------------------------------|---------|
| `utilis.debug`           | Allows access to the `/utilisdebug` command.      | `op`    |
| `utilis.sudo`            | Allows use of the `/sudo` command.                | `op`    |
| `utilis.nickname`        | Allows setting a nickname.                        | `true`  |
| `utilis.rename`          | Allows renaming other players.                    | `op`    |
| `utilis.color`           | Allows changing nickname color.                   | `true`  |
| `utilis.nickreset`       | Allows resetting your nickname.                   | `true`  |
| `utilis.realname`        | Allows retrieving the real name of a player.      | `true`  |
| `utilis.list`            | Allows listing all online players.                | `true`  |
| `utilis.suck`            | Allows teleporting all items to the player.       | `op`    |
| `utilis.vanish`          | Allows toggling vanish mode.                      | `op`    |
| `utilis.sleep`           | Allows using the `/ns` command.                   | `op`    |
| `utilis.isee`            | Allows using the `/isee` command.                 | `op`    |
| `utilis.recover`         | Allows using the `/recover` command.              | `op`    |
| `utilis.whitelist.use`   | Allows the player to manage the whitelist         | `op`    |
| `utilis.whitelist.add`   | Allows the player to add to the whitelist         | `op`    |
| `utilis.whitelist.remove`| Allows the player to remove from the whitelist    | `op`    |
| `utilis.sm`              | Allows using the `/sleepmessage set` command      | `true`  |
| `utilis.sm.admin`        | Allows using the `/sleepmessage edit/remove` command  | `op`    |

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
