/*
 * Copyright (C) 2016-2017 The MoonLake (mcmoonlake@hotmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.minecraft.moonlake.api.region

import com.google.common.collect.Iterators
import com.minecraft.moonlake.api.toBukkitWorld
import org.bukkit.World

open class RegionCuboid(
        world: World,
        private var pos1: RegionVector,
        private var pos2: RegionVector) : RegionAbstract(world), RegionFlat {

    /** api */

    fun getPos1(): RegionVector
            = pos1

    fun setPos1(pos1: RegionVector)
            { this.pos1 = pos1 }

    fun getPos2(): RegionVector
            = pos2

    fun setPos2(pos2: RegionVector)
            { this.pos2 = pos2 }

    override fun getMinimumPoint(): RegionVector
            = RegionVector(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z))

    override fun getMaximumPoint(): RegionVector
            = RegionVector(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z))

    override fun contains(vector: RegionVector): Boolean {
        val x = vector.x
        val y = vector.y
        val z = vector.z
        val min = getMinimumPoint()
        val max = getMaximumPoint()
        return x >= min.getBlockX() && x <= max.getBlockX() &&
                y >= min.getBlockY() && y <= max.getBlockY() &&
                z >= min.getBlockZ() && z <= max.getBlockZ()
    }

    override fun getMinimumY(): Int
            = Math.min(pos1.getBlockY(), pos2.getBlockY())

    override fun getMaximumY(): Int
            = Math.max(pos1.getBlockY(), pos2.getBlockY())

    override fun iterator(): MutableIterator<RegionVectorBlock> {
        return object: MutableIterator<RegionVectorBlock> {
            private val min = getMinimumPoint()
            private val max = getMaximumPoint()
            private var nextX = min.getBlockX()
            private var nextY = min.getBlockY()
            private var nextZ = min.getBlockZ()

            override fun hasNext(): Boolean
                    = nextX != 0x7fffffff

            override fun next(): RegionVectorBlock {
                if(!hasNext())
                    throw NoSuchElementException()
                val answer = RegionVectorBlock(nextX, nextY, nextZ)
                if(++nextX > max.getBlockX()) {
                    nextX = min.getBlockX()
                    if(++nextY > max.getBlockY()) {
                        nextY = min.getBlockY()
                        if(++nextZ > max.getBlockZ()) {
                            nextX = 0x7fffffff
                        }
                    }
                }
                return answer
            }

            override fun remove()
                    = throw UnsupportedOperationException()
        }
    }

    override fun asRegionFlat(): MutableIterable<RegionVector2D> {
        return object: MutableIterable<RegionVector2D> {
            override fun iterator(): MutableIterator<RegionVector2D> {
                return object: MutableIterator<RegionVector2D> {
                    private val min = getMinimumPoint()
                    private val max = getMaximumPoint()
                    private var nextX = min.getBlockX()
                    private var nextZ = min.getBlockZ()

                    override fun hasNext(): Boolean
                            = nextX != 0x7fffffff

                    override fun next(): RegionVector2D {
                        if(!hasNext())
                            throw NoSuchElementException()
                        val answer = RegionVector2D(nextX, nextZ)
                        if(++nextX > max.getBlockX()) {
                            nextX = min.getBlockX()
                            if(++nextZ > max.getBlockZ()) {
                                nextX = 0x7fffffff
                            }
                        }
                        return answer
                    }

                    override fun remove()
                            = throw UnsupportedOperationException()
                }
            }
        }
    }

    fun getFaces(): MutableIterable<RegionVectorBlock> {
        return object: MutableIterable<RegionVectorBlock> {
            private val min = getMinimumPoint()
            private val max = getMaximumPoint()
            private val faceRegions = arrayOf(
                    RegionCuboid(getWorld(), pos1.setX(min.x), pos2.setX(min.x)),
                    RegionCuboid(getWorld(), pos1.setX(max.x), pos2.setX(max.x)),
                    RegionCuboid(getWorld(), pos1.setY(min.y), pos2.setY(min.y)),
                    RegionCuboid(getWorld(), pos1.setY(max.y), pos2.setY(max.y)),
                    RegionCuboid(getWorld(), pos1.setZ(min.z), pos2.setZ(min.z)),
                    RegionCuboid(getWorld(), pos1.setZ(max.z), pos2.setZ(max.z)))

            override fun iterator(): MutableIterator<RegionVectorBlock> {
                val iterators = faceRegions.map { it -> it.iterator() }.toTypedArray()
                return Iterators.concat(*iterators)
            }
        }
    }

    /** significant */

    override fun equals(other: Any?): Boolean {
        if(other === this)
            return true
        if(other is RegionCuboid)
            return super.equals(other) && pos1 == other.pos1 && pos2 == other.pos2
        return false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + pos1.hashCode()
        result = 31 * result + pos2.hashCode()
        return result
    }

    override fun toString(): String {
        return "RegionCuboid(world=${getWorld()}, pos1=$pos1, pos2=$pos2)"
    }

    override fun serialize(): MutableMap<String, Any> {
        val result = LinkedHashMap<String, Any>()
        result.put("world", getWorld().name)
        result.put("pos1", pos1.serialize())
        result.put("pos2", pos2.serialize())
        return result
    }

    /** static */

    companion object {

        @JvmStatic
        @JvmName("deserialize")
        @Suppress("UNCHECKED_CAST")
        fun deserialize(args: Map<String, Any>): RegionCuboid {
            val world = args["world"]?.toString()?.toBukkitWorld() ?: throw IllegalArgumentException("未知的世界: ${args["world"]}")
            val pos1 = RegionVector.deserialize(args["pos1"] as Map<String, Any>)
            val pos2 = RegionVector.deserialize(args["pos2"] as Map<String, Any>)
            return RegionCuboid(world, pos1, pos2)
        }
    }
}
