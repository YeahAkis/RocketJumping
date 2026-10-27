# Changelog

## 2.3

- Added Vulcan support to the Paper plugin, flags from rocket jump movement are ignored while a player is boosted
- The window ends when the player lands, and chained boosts can't extend it past `vulcanMaxMillis`
- Only the checks listed in `vulcanChecks` are suppressed, combat and building checks still flag as normal
- New config keys `vulcanGraceMillis`, `vulcanMaxMillis` and `vulcanChecks`, existing configs keep working with the defaults
- Nothing changes on servers without Vulcan

## 2.2

- Added Fabric, NeoForge and Forge versions, they share one codebase and none of them need Fabric API
- `damageShooter: false` now stops the rocket from hurting its shooter for any shooter, before it only tried for players and did not block the damage
- Fixed an error when a dispenser fired the rocket
- Removed the debug lines that were logged on every hit
- The defaults in the code now match the shipped config
