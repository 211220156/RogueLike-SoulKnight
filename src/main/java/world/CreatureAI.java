/*
 * Copyright (C) 2015 Aeranythe Echosong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package world;

import java.awt.Point;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Aeranythe Echosong
 */
abstract class CreatureAI implements Serializable, Runnable {

    protected Creature creature;
    protected World world;

    public CreatureAI(Creature creature) {
        this.creature = creature;
        this.creature.setAI(this);
        this.world = creature.getWorld();
    }

    public abstract void onEnter(int x, int y, Tile tile);

    public abstract void onUpdate();

    public abstract void attack(Creature another);
    public abstract void attack();

    public abstract void onNotify(String message);
    public void run() {}

}
