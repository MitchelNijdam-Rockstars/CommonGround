package com.mitchelnijdam.commonground.voting

import kotlin.math.pow

/**
 * Standard ELO: expected score = 1 / (1 + 10^((opponent - player) / 400)),
 * new rating = old + K * (actual - expected). K=32 suits a young dataset; revisit once
 * vote volumes grow (see plan notes).
 */
object EloCalculator {

    const val K_FACTOR = 32.0

    fun expectedScore(rating: Double, opponentRating: Double): Double =
        1.0 / (1.0 + 10.0.pow((opponentRating - rating) / 400.0))

    /** Returns the new (winnerRating, loserRating) after a single head-to-head outcome. */
    fun ratingsAfterWin(winnerRating: Double, loserRating: Double): Pair<Double, Double> {
        val newWinner = winnerRating + K_FACTOR * (1.0 - expectedScore(winnerRating, loserRating))
        val newLoser = loserRating + K_FACTOR * (0.0 - expectedScore(loserRating, winnerRating))
        return newWinner to newLoser
    }

    /**
     * One pick out of many: the winner beat each [loserRatings] entry. Each pairwise delta is
     * computed against the pre-vote ratings (so the result is order-independent); the winner's
     * gains are summed. Returns the new winner rating and the new rating per loser, in input order.
     * A single loser reduces to [ratingsAfterWin].
     */
    fun ratingsAfterWinAgainstAll(winnerRating: Double, loserRatings: List<Double>): Pair<Double, List<Double>> {
        val newWinner = winnerRating + loserRatings.sumOf { K_FACTOR * (1.0 - expectedScore(winnerRating, it)) }
        val newLosers = loserRatings.map { it + K_FACTOR * (0.0 - expectedScore(it, winnerRating)) }
        return newWinner to newLosers
    }
}
