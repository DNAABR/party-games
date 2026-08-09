package com.leminno.partygames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.leminno.partygames.ui.theme.PartyGamesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PartyGamesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PartyGamesAppNavHost()
                }
            }
        }
    }
}

@Composable
fun PartyGamesAppNavHost() {
    val navController = rememberNavController()

    // Shared typed argument definitions for all game routes
    val gameRouteArgs = listOf(
        navArgument("playerCount") { type = NavType.IntType },
        navArgument("timerSec") { type = NavType.IntType }
    )

    NavHost(navController = navController, startDestination = "hub") {
        // Main Arcade Hub
        composable("hub") {
            ArcadeHubScreen(
                onLaunchGame = { gameId, playerCount, timerSec ->
                    navController.navigate("game/$gameId/$playerCount/$timerSec")
                }
            )
        }

        // MVP Game 1: Who Am I?
        composable("game/who_am_i/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val timerSec = backStackEntry.arguments?.getInt("timerSec") ?: 60
            WhoAmIScreen(
                timerSec = timerSec,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 2: Truth or Dare
        composable("game/truth_or_dare/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            TruthOrDareScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 3: Never Have I Ever
        composable("game/never_have_i_ever/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            NeverHaveIEverScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 4: Undercover Spy
        composable("game/undercover_spy/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            UndercoverSpyScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 5: Hot Potato
        composable("game/hot_potato/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            HotPotatoScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 6: Chirya Uri
        composable("game/chirya_uri/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            ChiryaUriScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 7: Would You Rather
        composable("game/would_you_rather/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            WouldYouRatherScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 8: Connect Four
        composable("game/connect_four/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            ConnectFourScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 1: Charades
        composable("game/charades/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val timerSec = backStackEntry.arguments?.getInt("timerSec") ?: 60
            CharadesScreen(
                timerSec = timerSec,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 2: I Want to Be...
        composable("game/i_want_to_be/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            IWantToBeScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 3: Two Truths & A Lie
        composable("game/two_truths_and_a_lie/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            TwoTruthsAndALieScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 4: Most Likely To
        composable("game/most_likely_to/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            MostLikelyToScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 5: Decibel Scream
        composable("game/decibel_scream/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            DecibelScreamScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 6: Silent Library
        composable("game/silent_library/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            SilentLibraryScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 7: Power-Up Tic Tac Toe
        composable("game/ultimate_ttt/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            PowerUpTTTScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 2 Game 8: Hand Cricket
        composable("game/hand_cricket/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            HandCricketScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 1: Mafia / Werewolf
        composable("game/mafia_werewolf/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 6
            MafiaWerewolfScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 2: Scribble & Pass
        composable("game/scribble_and_pass/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            ScribbleAndPassScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 3: Wavelength
        composable("game/wavelength/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            WavelengthScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 4: Codenames
        composable("game/codenames/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            CodenamesScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 5: Twit (Wits & Wagers)
        composable("game/twit/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 4
            TwitScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 6: Fake It
        composable("game/fake_it/{playerCount}/{timerSec}", arguments = gameRouteArgs) { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getInt("playerCount") ?: 3
            FakeItScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 7: Write Funny (Quiplash)
        composable("game/write_funny/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            WriteFunnyScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 8: Name Place Animal
        composable("game/name_place_animal/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            NamePlaceAnimalScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 9: Battleship
        composable("game/battleship/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            BattleshipScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 10: Hangman
        composable("game/hangman/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            HangmanScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // Phase 3 Game 11: Letter League
        composable("game/scrabble_league/{playerCount}/{timerSec}", arguments = gameRouteArgs) {
            ScrabbleLeagueScreen(
                onExitGame = { navController.popBackStack() }
            )
        }
    }
}
