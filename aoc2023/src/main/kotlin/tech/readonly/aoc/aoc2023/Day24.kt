package tech.readonly.aoc.aoc2023

import com.microsoft.z3.Context
import com.microsoft.z3.IntNum
import com.microsoft.z3.RatNum
import com.microsoft.z3.Status.SATISFIABLE
import tech.readonly.aoc.aoc2023.Day24.DOUBLE_FORMAT
import tech.readonly.aoc.aoc2023.util.Coords
import java.io.File
import java.math.RoundingMode.HALF_UP
import java.text.DecimalFormat
import java.util.Scanner

private data class Hailstone(val pos: Coords<Long>, val vel: Coords<Long>)

private object Day24 {
    val DOUBLE_FORMAT = DecimalFormat("0.###")

    init {
        DOUBLE_FORMAT.roundingMode = HALF_UP
    }
}

fun main() {
    val hailstones =
        Scanner(File(ClassLoader.getSystemResource("inputs/Day24.txt").file)).use { scanner ->
            sequence { while (scanner.hasNextLine()) yield(scanner.nextLine().trim()) }.toList()
                .map {
                    val (posStr, velStr) = it.split(" @ ")
                    val pos = posStr.trim().split(", ").map { s -> s.trim().toLong() }
                    val vel = velStr.trim().split(", ").map { s -> s.trim().toLong() }
                    Hailstone(Coords(*pos.toTypedArray()), Coords(*vel.toTypedArray()))
                }
        }

    println("Part 1: ${part1(hailstones)}")
    println("Part 2: ${part2(hailstones)}")
}

private fun part1(hailstones: List<Hailstone>): Int {
    val numFactor = 1.0
    val minCoord = 200000000000000L
    val maxCoord = 400000000000000L
    // for the sample input:
    //val minCoord = 7L
    //val maxCoord = 27L

    val ctx = Context()
    val s = ctx.mkSolver()

    var numInside = 0
    for (i in 0..<hailstones.lastIndex) {
        val hailstone1 = hailstones[i]

        val px1 = ctx.mkReal(hailstone1.pos.first)
        val py1 = ctx.mkReal(hailstone1.pos.second)
        val vx1 = ctx.mkReal(hailstone1.vel.first)
        val vy1 = ctx.mkReal(hailstone1.vel.second)
        val n = ctx.mkRealConst("n$i")

        for (j in (i + 1)..hailstones.lastIndex) {
            val hailstone2 = hailstones[j]
            //println("Hailstone A: ${hailstone1.pos.first}, ${hailstone1.pos.second}, ${hailstone1.pos.third} @ ${hailstone1.vel.first}, ${hailstone1.vel.second}, ${hailstone1.vel.third}")
            //println("Hailstone B: ${hailstone2.pos.first}, ${hailstone2.pos.second}, ${hailstone2.pos.third} @ ${hailstone2.vel.first}, ${hailstone2.vel.second}, ${hailstone2.vel.third}")

            val px2 = ctx.mkReal(hailstone2.pos.first)
            val py2 = ctx.mkReal(hailstone2.pos.second)
            val vx2 = ctx.mkReal(hailstone2.vel.first)
            val vy2 = ctx.mkReal(hailstone2.vel.second)
            val m = ctx.mkRealConst("m$j")

            s.push()
            s.add(ctx.mkEq(ctx.mkAdd(px1, ctx.mkMul(vx1, n)), ctx.mkAdd(px2, ctx.mkMul(vx2, m))))
            s.add(ctx.mkEq(ctx.mkAdd(py1, ctx.mkMul(vy1, n)), ctx.mkAdd(py2, ctx.mkMul(vy2, m))))

            if (s.check() == SATISFIABLE) {
                val nResult = s.model.getConstInterp(n) as RatNum
                val mResult = s.model.getConstInterp(m) as RatNum
                val nFactor = nResult.numerator.int64 / nResult.denominator.int64.toDouble()
                val mFactor = mResult.numerator.int64 / mResult.denominator.int64.toDouble()
                val intersection = (hailstone1.pos * numFactor) + (hailstone1.vel * nFactor)

                val inside =
                    minCoord <= intersection.first && minCoord <= intersection.second && maxCoord >= intersection.first && maxCoord >= intersection.second

                if (inside && nFactor > 0 && mFactor > 0) {
                    numInside++
                }

                //printIntersect(nFactor, mFactor, inside, intersection)
            } else {
                //println("Hailstones' paths are parallel; they never intersect.")
            }

            s.pop()
            //println()
        }
    }

    return numInside
}

private fun part2(hailstones: List<Hailstone>): Long {
    val ctx = Context()
    val s = ctx.mkSolver()
    val zero = ctx.mkInt(0)

    val px = ctx.mkIntConst("px")
    val py = ctx.mkIntConst("py")
    val pz = ctx.mkIntConst("pz")
    val vx = ctx.mkIntConst("vx")
    val vy = ctx.mkIntConst("vy")
    val vz = ctx.mkIntConst("vz")

    val t = hailstones.indices.map { i -> ctx.mkIntConst("t$i") }
    t.forEach { s.add(ctx.mkGe(it, zero)) }

    hailstones.forEachIndexed { i, h ->
        if (i >= 4) return@forEachIndexed

        val pxKnown = ctx.mkInt(h.pos.first)
        val pyKnown = ctx.mkInt(h.pos.second)
        val pzKnown = ctx.mkInt(h.pos.third)
        val vxKnown = ctx.mkInt(h.vel.first)
        val vyKnown = ctx.mkInt(h.vel.second)
        val vzKnown = ctx.mkInt(h.vel.third)

        val xKnownAdd = ctx.mkAdd(pxKnown, ctx.mkMul(vxKnown, t[i]))
        val yKnownAdd = ctx.mkAdd(pyKnown, ctx.mkMul(vyKnown, t[i]))
        val zKnownAdd = ctx.mkAdd(pzKnown, ctx.mkMul(vzKnown, t[i]))
        val xAdd = ctx.mkAdd(px, ctx.mkMul(vx, t[i]))
        val yAdd = ctx.mkAdd(py, ctx.mkMul(vy, t[i]))
        val zAdd = ctx.mkAdd(pz, ctx.mkMul(vz, t[i]))

        s.add(ctx.mkEq(xKnownAdd, xAdd))
        s.add(ctx.mkEq(yKnownAdd, yAdd))
        s.add(ctx.mkEq(zKnownAdd, zAdd))
    }

    if (s.check() != SATISFIABLE) {
        error("Could not solve")
    }

    val pxResult = s.model.getConstInterp(px) as IntNum
    val pyResult = s.model.getConstInterp(py) as IntNum
    val pzResult = s.model.getConstInterp(pz) as IntNum

    return pxResult.int64 + pyResult.int64 + pzResult.int64
}

private fun printIntersect(
    nFactor: Double,
    mFactor: Double,
    inside: Boolean,
    intersection: Coords<Double>,
) {
    if (nFactor < 0) {
        if (mFactor < 0) {
            println("Hailstones' paths crossed in the past for both hailstones.")
        } else {
            println("Hailstones' paths crossed in the past for hailstone A.")
        }
    } else if (mFactor < 0) {
        println("Hailstones' paths crossed in the past for hailstone B.")
    } else {
        println(
            "Hailstones' paths will cross ${if (inside) "inside" else "outside"} the test area (at x=${
                DOUBLE_FORMAT.format(
                    intersection.first
                )
            }, y=${DOUBLE_FORMAT.format(intersection.second)})."
        )
    }
}
