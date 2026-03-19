/**
 * Copyright (C) 2026 @bear_369
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.bear369.logistorage.shared.computercraft.peripherial;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAttachedPeripherial implements IPeripheral {
    private Set<IComputerAccess> computers = new HashSet<>();

    public void emitEventSignal(boolean isNameAttached, String event, Object... objects) {
        for (IComputerAccess computer : computers) {
            Object[] args = Arrays.copyOf(objects, objects.length);
            if (isNameAttached) {
                args = Arrays.copyOf(args, args.length + 1);
                args[args.length - 1] = computer.getAttachmentName();
            }
            computer.queueEvent(event, args);
        }
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other;
    }
}
