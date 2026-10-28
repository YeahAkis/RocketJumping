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

package org.notionsmp.rocketJumping;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpTracker {

    private record Window(long boostedAt, long expiresAt) {
    }

    private final Map<UUID, Window> windows = new ConcurrentHashMap<>();

    public void mark(UUID uuid, long maxAirMillis) {
        long now = System.currentTimeMillis();
        windows.put(uuid, new Window(now, now + maxAirMillis));
    }

    public void land(UUID uuid, long minAirMillis, long tailMillis) {
        if (windows.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        windows.computeIfPresent(uuid, (id, window) -> {
            if (now - window.boostedAt() < minAirMillis) {
                return window;
            }
            long expires = Math.min(window.expiresAt(), now + tailMillis);
            return expires <= now ? null : new Window(window.boostedAt(), expires);
        });
    }

    public boolean isActive(UUID uuid) {
        Window window = windows.get(uuid);
        if (window == null) {
            return false;
        }
        if (window.expiresAt() < System.currentTimeMillis()) {
            windows.remove(uuid, window);
            return false;
        }
        return true;
    }

    public void clear(UUID uuid) {
        windows.remove(uuid);
    }
}
