package com.leminno.partygames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.leminno.partygames.ui.games.chiryauri.ChiryaUriScreen
import com.leminno.partygames.ui.games.connectfour.ConnectFourScreen
import com.leminno.partygames.ui.games.hotpotato.HotPotatoScreen
import com.leminno.partygames.ui.games.neverhaveiever.NeverHaveIEverScreen
import com.leminno.partygames.ui.games.truthordare.TruthOrDareScreen
import com.leminno.partygames.ui.games.undercover.UndercoverSpyScreen
import com.leminno.partygames.ui.games.whoami.WhoAmIScreen
import com.leminno.partygames.ui.games.wouldyourather.WouldYouRatherScreen
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
        composable("game/who_am_i/{playerCount}/{timerSec}") { backStackEntry ->
            val timerSec = backStackEntry.arguments?.getString("timerSec")?.toIntOrNull() ?: 60
            WhoAmIScreen(
                timerSec = timerSec,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 2: Truth or Dare
        composable("game/truth_or_dare/{playerCount}/{timerSec}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toIntOrNull() ?: 4
            TruthOrDareScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 3: Never Have I Ever
        composable("game/never_have_i_ever/{playerCount}/{timerSec}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toIntOrNull() ?: 4
            NeverHaveIEverScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 4: Undercover Spy
        composable("game/undercover_spy/{playerCount}/{timerSec}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toIntOrNull() ?: 4
            UndercoverSpyScreen(
                playerCount = playerCount,
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 5: Hot Potato
        composable("game/hot_potato/{playerCount}/{timerSec}") { backStackEntry ->
            HotPotatoScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 6: Chirya Uri
        composable("game/chirya_uri/{playerCount}/{timerSec}") {
            ChiryaUriScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 7: Would You Rather
        composable("game/would_you_rather/{playerCount}/{timerSec}") {
            WouldYouRatherScreen(
                onExitGame = { navController.popBackStack() }
            )
        }

        // MVP Game 8: Connect Four
        composable("game/connect_four/{playerCount}/{timerSec}") {
            ConnectFourScreen(
                onExitGame = { navController.popBackStack() }
            )
        }
    }
}
