package com.mitchelnijdam.commonground.voting

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import kotlin.math.abs

class EloCalculatorTest {

    @Test
    fun `equally rated players exchange exactly half the K-factor`() {
        val (winner, loser) = EloCalculator.ratingsAfterWin(1500.0, 1500.0)
        assertThat(winner).isCloseTo(1516.0, within(0.0001))
        assertThat(loser).isCloseTo(1484.0, within(0.0001))
    }

    @Test
    fun `expected scores of both players always sum to one`() {
        listOf(1500.0 to 1500.0, 1800.0 to 1400.0, 1200.0 to 2000.0).forEach { (a, b) ->
            assertThat(EloCalculator.expectedScore(a, b) + EloCalculator.expectedScore(b, a)).isCloseTo(1.0, within(0.0001))
        }
    }

    @Test
    fun `winner gain equals loser loss`() {
        listOf(1500.0 to 1500.0, 1700.0 to 1300.0, 1450.0 to 1650.0).forEach { (winnerOld, loserOld) ->
            val (winnerNew, loserNew) = EloCalculator.ratingsAfterWin(winnerOld, loserOld)
            assertThat(loserOld - loserNew).isCloseTo(winnerNew - winnerOld, within(0.0001))
        }
    }

    @Test
    fun `a strong favourite gains little from beating a weak opponent`() {
        val (winner, loser) = EloCalculator.ratingsAfterWin(2000.0, 1200.0)
        assertThat(winner - 2000.0).describedAs("favourite gain should be tiny").isLessThan(1.0)
        assertThat(abs((2000.0 - winner) + (winner - 2000.0))).isLessThan(0.0001)
        assertThat(1200.0 - loser).isLessThan(1.0)
    }

    @Test
    fun `an underdog gains close to the full K-factor from an upset win`() {
        val (winner, _) = EloCalculator.ratingsAfterWin(1200.0, 2000.0)
        val gain = winner - 1200.0
        assertThat(gain).describedAs("underdog gain should approach K=32").isGreaterThan(31.0).isLessThan(32.0)
    }

    @Test
    fun `known reference values are reproduced`() {
        // expected score for 1613 vs 1609 is ~0.5058 (classic Elo example)
        assertThat(EloCalculator.expectedScore(1613.0, 1609.0)).isCloseTo(0.5058, within(0.001))
    }
}
