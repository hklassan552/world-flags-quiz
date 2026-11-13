package com.worldflags.quiz.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.worldflags.quiz.R
import com.worldflags.quiz.domain.model.*

@Composable
fun continentName(continent: Continent): String = stringResource(when (continent) {
    Continent.EUROPE -> R.string.europe
    Continent.ASIA -> R.string.asia
    Continent.AFRICA -> R.string.africa
    Continent.NORTH_AMERICA -> R.string.north_america
    Continent.SOUTH_AMERICA -> R.string.south_america
    Continent.OCEANIA -> R.string.oceania
})

@Composable
fun difficultyName(difficulty: Difficulty): String = stringResource(when (difficulty) {
    Difficulty.LEARNER -> R.string.learner
    Difficulty.EXPLORER -> R.string.explorer
    Difficulty.NAVIGATOR -> R.string.navigator
})

@Composable
fun knowledgeLabel(bits: Int): String = stringResource(when (bits) {
    1 -> R.string.flag_learned
    2 -> R.string.capital_learned
    3 -> R.string.country_learned
    else -> R.string.not_started
})

@Composable
fun capitalQuestion(country: Country): String = stringResource(when (country.role) {
    CapitalRole.CAPITAL -> R.string.capital_question
    CapitalRole.ADMINISTRATIVE -> R.string.administrative_question
    CapitalRole.CONSTITUTIONAL -> R.string.constitutional_question
    CapitalRole.LEGISLATIVE -> R.string.legislative_question
    CapitalRole.SEAT -> R.string.seat_question
}, countryName(country.id))

@Composable
fun capitalFact(country: Country): String = stringResource(when (country.role) {
    CapitalRole.CAPITAL -> R.string.capital_fact
    CapitalRole.ADMINISTRATIVE -> R.string.administrative_fact
    CapitalRole.CONSTITUTIONAL -> R.string.constitutional_fact
    CapitalRole.LEGISLATIVE -> R.string.legislative_fact
    CapitalRole.SEAT -> R.string.seat_fact
}, capitalName(country.id), countryName(country.id))
