/*
 * RocketJumping, fireworks that launch players and nearby entities
 * Copyright (C) 2024-2026  Richard Mikloš
 *
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.notionsmp.rocketjumping;

public final class RocketConfig {
    public double boostPower = 1.5;
    public double multiplier = 0.8;
    public boolean damageShooter = false;
    public double radius = 5.0;
    public boolean creeperExplodeRockets = true;
    public double explosionPower = 2.0;
    public boolean explosionSetFire = false;
    public boolean explosionBreakBlocks = false;
}
