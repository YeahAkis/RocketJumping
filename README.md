# RocketJumping

Fireworks launch you and everything near the blast. Shoot a firework from a crossbow at the ground next to your feet and you go flying, with the strength and range set in a config file. Runs as a Paper plugin (Folia too) and as a Fabric, NeoForge or Forge mod. Only the server needs it, players join with a vanilla client. Releases are on [Modrinth](https://modrinth.com/project/eIQHmakB).

## How it works

When a firework rocket hits a block or an entity, everything within `radius` blocks of it gets pushed away from the blast. The push is

```
boostPower * flightDuration * multiplier * (1 - distance / radius)
```

so longer flights hit harder and things at the edge of the radius barely move. A rocket only counts if it has at least one explosion star and a flight duration above 0. Rockets with a creeper shaped star can also cause a real explosion, see `creeperExplodeRockets` below.

`damageShooter` controls whether the blast hurts whoever fired the rocket. It is off by default, so you can jump without taking the explosion damage. Everyone else standing there still takes it.

## Config

The keys are the same everywhere. The Paper plugin reads `plugins/RocketJumping/config.yml`, the mods read `config/rocketjumping.json`. Both are created on the first start.

| Key | Default | What it does |
| --- | --- | --- |
| `boostPower` | 1.5 | Base strength of the push |
| `multiplier` | 0.8 | Scales the push with the flight duration |
| `radius` | 5.0 | How far from the rocket entities are pushed, in blocks |
| `damageShooter` | false | Whether the shooter takes the firework's explosion damage |
| `creeperExplodeRockets` | true | Rockets with a creeper shaped star cause an explosion |
| `explosionPower` | 2.0 | Power of that explosion |
| `explosionSetFire` | false | Whether it starts fires |
| `explosionBreakBlocks` | false | Whether it breaks blocks |

`/rocketjumping reload` (also `/rocket reload`, or `rl`) reloads the file. On Paper it needs `rocketjumping.admin`, on the mods it needs operator rights.

## Layout

- `paper/`: the Paper and Folia plugin. Bukkit events and ACF for the command.
- `common/`: the mod code. A config class, the boost routine and two mixins, one on `FireworkRocketEntity` and one that registers the command.
- `fabric/`, `neoforge/`, `forge/`: loader modules that compile `common/` and add the metadata. They only use mixins, so no Fabric API or loader events are needed.

The mod loader builds are for Minecraft 26.3. The Paper plugin targets the 1.19 API and has been run on 26.3 Paper and Folia.

## Building

JDK 25 to run Gradle. The mods target Java 25, the Paper plugin targets Java 21.

```
./gradlew build
```

Jars end up in `paper/build/libs`, `fabric/build/libs`, `neoforge/build/libs` and `forge/build/libs`.

## Testing

Mixin targets are strings, so a clean compile does not prove much. Start a dev server with `./gradlew :fabric:runServer` (or `:neoforge:runServer`, `:forge:runServer`), put `eula=true` in the run folder and turn on RCON in `server.properties`. In a flat world, put a block a few blocks above the ground, drop an invulnerable item entity below it and summon a firework rocket with a star and a flight duration under the block. The rocket hits the block and the item should get shoved sideways. Summoning a villager with a UUID and giving the rocket the same `Owner` shows `damageShooter`.

## Known issues

The boost only happens when the rocket hits a block or an entity. A rocket that burns out in the air explodes as usual and pushes nobody.

## Contributing

Open an issue or a pull request. Commits are small and use `feat:`, `fix:`, `chore:`, `refactor:`, `build:` or `docs:`.

## License

GPL-3.0-only, see `LICENSE`.
