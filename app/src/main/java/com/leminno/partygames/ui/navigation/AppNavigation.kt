package com.leminno.partygames.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.leminno.partygames.ui.games.battleship.BattleshipScreen
import com.leminno.partygames.ui.games.charades.CharadesScreen
import com.leminno.partygames.ui.games.chiryauri.ChiryaUriScreen
import com.leminno.partygames.ui.games.codenames.CodenamesScreen
import com.leminno.partygames.ui.games.connectfour.ConnectFourScreen
import com.leminno.partygames.ui.games.decibel_scream.DecibelScreamScreen
import com.leminno.partygames.ui.games.fake_it.FakeItScreen
import com.leminno.partygames.ui.games.hand_cricket.HandCricketScreen
import com.leminno.partygames.ui.games.hangman.HangmanScreen
import com.leminno.partygames.ui.games.hotpotato.HotPotatoScreen
import com.leminno.partygames.ui.games.i_want_to_be.IWantToBeScreen
import com.leminno.partygames.ui.games.mafia_werewolf.MafiaWerewolfScreen
import com.leminno.partygames.ui.games.most_likely_to.MostLikelyToScreen
import com.leminno.partygames.ui.games.name_place_animal.NamePlaceAnimalScreen
import com.leminno.partygames.ui.games.neverhaveiever.NeverHaveIEverScreen
import com.leminno.partygames.ui.games.scribble_and_pass.ScribbleAndPassScreen
import com.leminno.partygames.ui.games.scrabble_league.ScrabbleLeagueScreen
import com.leminno.partygames.ui.games.silent_library.SilentLibraryScreen
import com.leminno.partygames.ui.games.truthordare.TruthOrDareScreen
import com.leminno.partygames.ui.games.twit.TwitScreen
import com.leminno.partygames.ui.games.two_truths_and_a_lie.TwoTruthsAndALieScreen
import com.leminno.partygames.ui.games.ultimate_ttt.PowerUpTTTScreen
import com.leminno.partygames.ui.games.undercover.UndercoverSpyScreen
import com.leminno.partygames.ui.games.wavelength.WavelengthScreen
import com.leminno.partygames.ui.games.whoami.WhoAmIScreen
import com.leminno.partygames.ui.games.wouldyourather.WouldYouRatherScreen
import com.leminno.partygames.ui.games.write_funny.WriteFunnyScreen
import com.leminno.partygames.ui.hub.ArcadeHubScreen

private val gameRouteArgs = listOf(
    navArgument("playerCount") { type = NavType.IntType },
    navArgument("timerSec") { type = NavType.IntType }
)

@Composable
fun PartyGamesAppNavHost(
    initialRoomCode: String? = null,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "hub") {
        // Arcade Hub Screen
        composable("hub") {
            ArcadeHubScreen(
                initialRoomCode = initialRoomCode,
                onLaunchGame = { gameId, playerCount, timerSec ->
                    navController.navigate("game/$gameId/$playerCount/$timerSec")
                }
            )
        }

        // Modular Game Nav Sub-graphs
        mvpGameRoutes(navController)
        phase2GameRoutes(navController)
        phase3GameRoutes(navController)
    }
}

private fun NavGraphBuilder.mvpGameRoutes(navController: NavHostController) {
    composable("game/who_am_i/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val timerSec = backStackEntry.arguments?.getInt("timerSec") ?: 60
        WhoAmIScreen(
            timerSec = timerSec,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/truth_or_dare/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        TruthOrDareScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/never_have_i_ever/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        NeverHaveIEverScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/undercover_spy/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        UndercoverSpyScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/hot_potato/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        HotPotatoScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/chirya_uri/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        ChiryaUriScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/would_you_rather/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        WouldYouRatherScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/connect_four/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        ConnectFourScreen(
            onExitGame = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.phase2GameRoutes(navController: NavHostController) {
    composable("game/charades/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val timerSec = backStackEntry.arguments?.getInt("timerSec") ?: 60
        CharadesScreen(
            timerSec = timerSec,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/i_want_to_be/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        IWantToBeScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/two_truths_and_a_lie/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        TwoTruthsAndALieScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/most_likely_to/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        MostLikelyToScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/decibel_scream/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        DecibelScreamScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/silent_library/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        SilentLibraryScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/ultimate_ttt/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        PowerUpTTTScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/hand_cricket/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        HandCricketScreen(
            onExitGame = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.phase3GameRoutes(navController: NavHostController) {
    composable("game/mafia_werewolf/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 6
        MafiaWerewolfScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/scribble_and_pass/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        ScribbleAndPassScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/wavelength/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        WavelengthScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/codenames/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        CodenamesScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/twit/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
        TwitScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/fake_it/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
        val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 3
        FakeItScreen(
            playerCount = playerCount,
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/write_funny/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        WriteFunnyScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/name_place_animal/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        NamePlaceAnimalScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/battleship/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        BattleshipScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/hangman/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        HangmanScreen(
            onExitGame = { navController.popBackStack() }
        )
    }

    composable("game/scrabble_league/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
        ScrabbleLeagueScreen(
            onExitGame = { navController.popBackStack() }
        )
    }
}
