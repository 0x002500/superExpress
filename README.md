# FastMinecarts Plugin
[![Kotlin CI with Maven](https://github.com/0x002500/superExpress/actions/workflows/maven.yml/badge.svg)](https://github.com/0x002500/superExpress/actions/workflows/maven.yml)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/VH6xs2AG)

Simple plugin for Minecraft [Folia](https://papermc.io/software/folia) servers that changes the speed of minecarts depending on the block beneath the rails.

## Features
- Configure which blocks affect speed.
- Choose the minecart speed for each block.
- Slow minecarts back to vanilla speed when player disembarks.
## Installation
To install FastMinecarts, follow these steps:
1. Download the plugin JAR file from [Modrinth](https://modrinth.com/plugin/superexpress) or the [Releases](https://github.com/0x002500/superExpress/releases) page.
2. Place the JAR file in the plugins folder of your Paper (or Paper fork) server.
3. Start the server and verify that the plugin loaded successfully.
## Configuration
The plugin can be configured via the `config.yml` file located in `plugins/FastMinecarts`.

The following configuration options are available:

- `blocks` - List of blocks and their corresponding modified speed.
### Default Config:
```yml
# List of blocks and their corresponding maximum minecart speeds
# Blocks must be from https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
# The default minecart speed is 0.4
blocks:
  GRAVEL: 0.8
  SOUL_SAND: 0.2
```
## Usage
The FastMinecarts plugin does not *yet* have any commands or permissions. Simply install and configure the plugin on your server to start using it.

When a player enters a minecart on rails above configured blocks, the speed is modified.
