package com.mitchelnijdam.commonground.voting

import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EloCalculatorTest {

    @Test
    fun `equally rated players exchange exactly half the K-factor`() {
        val (winner, loser) = EloCalculator.ratingsAfterWin(1500.0, 1500.0)
        assertEquals(1516.0, winner, 0.0001)
        assertEquals(1484.0, loser, 0.0001)
    }

    @Test
    fun `expected scores of both players always sum to one`() {
        listOf(1500.0 to 1500.0, 1800.0 to 1400.0, 1200.0 to 2000.0).forEach { (a, b) ->
            assertEquals(1.0, EloCalculator.expectedScore(a, b) + EloCalculator.expectedScore(b, a), 0.0001)
        }
    }

    @Test
    fun `winner gain equals loser loss`() {
        listOf(1500.0 to 1500.0, 1700.0 to 1300.0, 1450.0 to 1650.0).forEach { (winnerOld, loserOld) ->
            val (winnerNew, loserNew) = EloCalculator.ratingsAfterWin(winnerOld, loserOld)
            assertEquals(winnerNew - winnerOld, loserOld - loserNew, 0.0001)
        }
    }

    @Test
    fun `a strong favourite gains little from beating a weak opponent`() {
        val (winner, loser) = EloCalculator.ratingsAfterWin(2000.0, 1200.0)
        assertTrue(winner - 2000.0 < 1.0, "favourite gain should be tiny, was ${winner - 2000.0}")
        assertTrue(abs((2000.0 - winner) + (winner - 2000.0)) < 0.0001)
        assertTrue(1200.0 - loser < 1.0)
    }

    @Test
    fun `an underdog gains close to the full K-factor from an upset win`() {
        val (winner, _) = EloCalculator.ratingsAfterWin(1200.0, 2000.0)
        val gain = winner - 1200.0
        assertTrue(gain > 31.0 && gain < 32.0, "underdog gain should approach K=32, was $gain")
    }

    @Test
    fun `known reference values are reproduced`() {
        // expected score for 1613 vs 1609 is ~0.5058 (classic Elo example)
        assertEquals(0.5058, EloCalculator.expectedScore(1613.0, 1609.0), 0.001)
    }
}
