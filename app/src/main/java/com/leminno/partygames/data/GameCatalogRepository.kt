package com.leminno.partygames.data

import com.leminno.partygames.ui.model.GameItem
import com.leminno.partygames.ui.model.GameRuleStep
import com.leminno.partygames.ui.model.SetupType
import com.leminno.partygames.ui.theme.GameCategory

object GameCatalogRepository {

    val allGames: List<GameItem> = listOf(
        // MVP Game 1
        GameItem(
            id = "who_am_i",
            title = "Who Am I?",
            tagLine = "Forehead guessing challenge",
            description = "Hold phone on your forehead! Your friends act or describe the secret person. Tilt down for Correct, tilt up to Skip!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.FOREHEAD_SENSOR,
            minPlayers = 2,
            maxPlayers = 12,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Keep screen facing away from your eyes during turn!",
            rules = listOf(
                GameRuleStep(1, "Place on Forehead", "Hold device facing outwards so players can see the secret word.", "📱"),
                GameRuleStep(2, "Ask & Listen", "Teammates shout clues without saying the secret word directly.", "🗣️"),
                GameRuleStep(3, "Tilt to Score", "Tilt phone DOWN when you guess right, tilt UP to skip!", "↔️")
            )
        ),
        // MVP Game 2
        GameItem(
            id = "truth_or_dare",
            title = "Truth or Dare",
            tagLine = "Physics bottle spinner & prompts",
            description = "Spin the physical canvas bottle with realistic haptics! Pick your intensity deck from Clean to Extreme.",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 10,
            estTimeMinutes = 15,
            isMvp = true,
            antiCheatNotice = "Player selected by bottle MUST answer or complete dare!",
            rules = listOf(
                GameRuleStep(1, "Flick to Spin", "Swipe across screen to spin the bottle with realistic physics.", "🍾"),
                GameRuleStep(2, "Select Challenge", "Chosen player picks Truth or Dare from deck.", "🃏"),
                GameRuleStep(3, "Execute or Pass", "Complete prompt to earn group approval!", "🔥")
            )
        ),
        // MVP Game 3
        GameItem(
            id = "never_have_i_ever",
            title = "Never Have I Ever",
            tagLine = "10 Lives elimination tracker",
            description = "Swipe through spicy & fun prompts. Everyone starts with 10 lives — lose a life if you have done it!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 15,
            estTimeMinutes = 10,
            isMvp = true,
            antiCheatNotice = "Be honest! Tap your heart to drop a life when guilty.",
            rules = listOf(
                GameRuleStep(1, "Read Prompt", "App displays statement: 'Never have I ever...'", "📖"),
                GameRuleStep(2, "Tap to Lose Life", "If you've done it, tap your life heart to drop 1 life.", "💔"),
                GameRuleStep(3, "Last Standing Wins", "Survive with the most remaining lives!", "👑")
            )
        ),
        // MVP Game 4
        GameItem(
            id = "undercover_spy",
            title = "Undercover Spy",
            tagLine = "Find the imposter among you",
            description = "Everyone gets the secret location except 1 Spy! Ask subtle questions to uncover the spy before they guess the location.",
            category = GameCategory.MYSTERY,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 10,
            isMvp = true,
            antiCheatNotice = "Long-press fingerprint scanner to view role secretly before passing phone!",
            rules = listOf(
                GameRuleStep(1, "Secret Role View", "Hold finger on scanner overlay to reveal your role safely.", "🔒"),
                GameRuleStep(2, "Cross-Examine", "Ask players questions about the location without giving it away.", "💬"),
                GameRuleStep(3, "Vote & Accuse", "Group votes on suspected spy before timer expires!", "🕵️")
            )
        ),
        // MVP Game 5
        GameItem(
            id = "hot_potato",
            title = "Hot Potato",
            tagLine = "Explosive physical passing game",
            description = "Answer prompt quickly and physically pass the phone! Random countdown trigger sets off camera LED burst & heavy rumbles.",
            category = GameCategory.ACTION,
            setupType = SetupType.PHYSICAL_PASS,
            minPlayers = 3,
            maxPlayers = 12,
            estTimeMinutes = 3,
            isMvp = true,
            antiCheatNotice = "You MUST speak answer out loud before passing phone!",
            rules = listOf(
                GameRuleStep(1, "Read Prompt", "Shout an answer fitting the category on screen.", "🗣️"),
                GameRuleStep(2, "Pass Phone", "Hand phone immediately to player on your left.", "🔄"),
                GameRuleStep(3, "Don't Get Caught", "If timer expires in your hands, phone explodes!", "💥")
            )
        ),
        // MVP Game 6
        GameItem(
            id = "chirya_uri",
            title = "Chirya Uri (Fly or Stay)",
            tagLine = "Multi-touch reaction battle",
            description = "Every player holds a corner touch target on screen! Lift finger ONLY when item actually flies (e.g., Sparrow Flies vs Cow Flies)!",
            category = GameCategory.ACTION,
            setupType = SetupType.MULTI_TOUCH,
            minPlayers = 2,
            maxPlayers = 4,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Touch targets track individual finger IDs down to the millisecond!",
            rules = listOf(
                GameRuleStep(1, "Hold Touch Zone", "Each player puts a index finger on a screen corner.", "👇"),
                GameRuleStep(2, "Listen / Watch Prompt", "Prompt calls: '[Object] FLIES!'", "🦅"),
                GameRuleStep(3, "Lift or Hold", "LIFT finger if item can fly. HOLD if item cannot fly!", "⚡")
            )
        ),
        // MVP Game 7
        GameItem(
            id = "would_you_rather",
            title = "Would You Rather",
            tagLine = "Dilemma voting & stats",
            description = "Swipe left or right between two hilarious options. Discover group choices with live animated percentage charts!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 10,
            estTimeMinutes = 8,
            isMvp = true,
            antiCheatNotice = "Vote secretly before group discussion!",
            rules = listOf(
                GameRuleStep(1, "Compare Options", "Read Option A vs Option B.", "⚖️"),
                GameRuleStep(2, "Swipe to Choose", "Swipe LEFT for Option A or RIGHT for Option B.", "👈"),
                GameRuleStep(3, "View Stats", "See live percentage breakdown of player choices!", "📊")
            )
        ),
        // MVP Game 8
        GameItem(
            id = "connect_four",
            title = "Connect Four",
            tagLine = "Gravity disc drop 2D grid",
            description = "Classic 7x6 board strategy with smooth gravity disc animation and dynamic turn indicators.",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 2,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Alternate turns side by side or pass device.",
            rules = listOf(
                GameRuleStep(1, "Select Column", "Tap column to drop your colored disc.", "🔴"),
                GameRuleStep(2, "Block & Line Up", "Connect 4 discs horizontally, vertically, or diagonally.", "🟡"),
                GameRuleStep(3, "Claim Victory", "First to connect four claims victory highlight!", "🏆")
            )
        ),

        // Phase 2 Expansion Games
        GameItem(
            id = "charades",
            title = "Charades",
            tagLine = "Acting & motion guessing",
            description = "Hold phone against forehead and act out words without speaking! Tilt down for Correct, tilt up to Skip.",
            category = GameCategory.ACTION,
            setupType = SetupType.FOREHEAD_SENSOR,
            minPlayers = 2,
            maxPlayers = 12,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Keep screen facing away from the actor's eyes!",
            rules = listOf(
                GameRuleStep(1, "Forehead Position", "Place phone on forehead facing your teammates.", "📱"),
                GameRuleStep(2, "Act Out Words", "Teammates act out the secret word without talking.", "🎭"),
                GameRuleStep(3, "Tilt Sensor", "Tilt DOWN on correct guess, tilt UP to skip!", "↕️")
            )
        ),
        GameItem(
            id = "i_want_to_be",
            title = "I Want to Be...",
            tagLine = "Career clue guessing",
            description = "Get a secret profession, reveal clues, and let your group guess your career before the timer expires!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Use the 2-second hold-to-reveal card so nearby players can't peak!",
            rules = listOf(
                GameRuleStep(1, "Secret Role Reveal", "Hold screen for 2 seconds to view secret profession safely.", "🔒"),
                GameRuleStep(2, "Give 3 Clues", "Give subtle clues without naming the profession.", "💡"),
                GameRuleStep(3, "Guess & Score", "Players guess before 30-second timer runs out!", "⏱️")
            )
        ),
        GameItem(
            id = "two_truths_and_a_lie",
            title = "Two Truths & A Lie",
            tagLine = "Spot the fake statement",
            description = "Active player enters 2 truths and 1 lie. Other players vote on which statement is fake to earn points!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 8,
            isMvp = true,
            antiCheatNotice = "Input statements privately using masked input mode.",
            rules = listOf(
                GameRuleStep(1, "Create Statements", "Type 2 true facts and 1 convincing lie.", "✍️"),
                GameRuleStep(2, "Group Vote", "Pass phone and let players select the statement they think is a lie.", "🗳️"),
                GameRuleStep(3, "Reveal & Tally", "Score points for spotting lies and tricking others!", "🏆")
            )
        ),
        GameItem(
            id = "most_likely_to",
            title = "Most Likely To",
            tagLine = "Group voting showdown",
            description = "Read funny scenarios, count down '3, 2, 1', vote secretly, and reveal live animated percentage breakdowns!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 12,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Cast secret votes on phone before group discussion!",
            rules = listOf(
                GameRuleStep(1, "Read Scenario", "App shows: 'Who is most likely to...'", "📜"),
                GameRuleStep(2, "Secret Vote", "Each player selects who fits the prompt best.", "👆"),
                GameRuleStep(3, "View Stats", "See live percentage charts of everyone's votes!", "📊")
            )
        ),
        GameItem(
            id = "decibel_scream",
            title = "Decibel Scream",
            tagLine = "Microphone volume party",
            description = "Compete in quiet whisper challenges (<15dB), maximum sudden scream spikes, or steady hum control!",
            category = GameCategory.ACTION,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 10,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Hold mic 6 inches from mouth during sound challenges!",
            rules = listOf(
                GameRuleStep(1, "Choose Mode", "Select Whisper (<15dB), Max Scream, or Steady Hum.", "🎙️"),
                GameRuleStep(2, "Make Noise", "Perform challenge when countdown hits zero.", "🔊"),
                GameRuleStep(3, "Check Gauge", "Live decibel meter measures peak volume!", "📈")
            )
        ),
        GameItem(
            id = "silent_library",
            title = "Silent Library",
            tagLine = "Face control challenge",
            description = "Perform ridiculous tasks without cracking a smile while funny distracting sound effects play!",
            category = GameCategory.ACTION,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 10,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "No smiling or laughing! Front camera tracks player expression.",
            rules = listOf(
                GameRuleStep(1, "Draw Task", "Active player receives a funny task card.", "🃏"),
                GameRuleStep(2, "Hold Straight Face", "Perform task for 30 seconds with straight face.", "😐"),
                GameRuleStep(3, "Distraction Wave", "App triggers funny sounds to break your focus!", "🦆")
            )
        ),
        GameItem(
            id = "ultimate_ttt",
            title = "Power-Up Tic Tac Toe",
            tagLine = "Strategic grid battle",
            description = "Upgraded Tic-Tac-Toe with tactical Power-Up cards: Erase opponent cells, Shield grid cells, or Double Turn!",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 2,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Power-ups are limited to 1 per game round per player!",
            rules = listOf(
                GameRuleStep(1, "Place or Power-Up", "Tap cell to place mark OR deploy a Power-Up card.", "⚡"),
                GameRuleStep(2, "Erase & Shield", "Use Erase to wipe cell, or Shield to lock your square.", "🛡️"),
                GameRuleStep(3, "Connect Three", "First to align 3 symbols in a row wins!", "🎉")
            )
        ),
        GameItem(
            id = "hand_cricket",
            title = "Hand Cricket",
            tagLine = "Split-screen duel & online room",
            description = "Simultaneous finger tap count (1 to 6) duel! Play 1v1 Split Screen or Team Match (Multi-Device / Online Room).",
            category = GameCategory.ACTION,
            setupType = SetupType.SPLIT_SCREEN,
            minPlayers = 2,
            maxPlayers = 12,
            estTimeMinutes = 8,
            isMvp = true,
            antiCheatNotice = "In split-screen, tap your choice at the same time!",
            rules = listOf(
                GameRuleStep(1, "Select Mode", "Choose 1v1 Split-Screen or Team Match (Multi-Device / Online Room).", "📲"),
                GameRuleStep(2, "Tap 1 to 6", "Batter & Bowler pick finger numbers simultaneously.", "🔢"),
                GameRuleStep(3, "Runs vs OUT!", "Same number = OUT! Different numbers = Runs added to total!", "🏏")
            )
        ),

        // Phase 3 Games
        GameItem("mafia_werewolf", "Mafia / Werewolf", "Narrator mystery night", "App guides night cycles with audio masking.", GameCategory.MYSTERY, SetupType.PASS_AND_PLAY, 5, 16, 20, false, null, emptyList()),
        GameItem("scribble_and_pass", "Scribble & Pass", "Canvas drawing chain", "Alternate prompts and drawing chains!", GameCategory.ACTION, SetupType.PASS_AND_PLAY, 3, 10, 15, false, null, emptyList()),
        GameItem("wavelength", "Wavelength", "Spectrum dial wheel", "Guess where clue lands on target dial.", GameCategory.BOARD, SetupType.PASS_AND_PLAY, 2, 10, 10, false, null, emptyList()),
        GameItem("codenames", "Codenames", "Spymaster word grid", "Connect secret words using one-word clues.", GameCategory.MYSTERY, SetupType.DUAL_DEVICE, 4, 10, 15, false, null, emptyList()),
        GameItem("twit", "Twit", "Numeric estimation betting", "Guess numerical trivia and bet on closest answer.", GameCategory.BOARD, SetupType.PASS_AND_PLAY, 3, 8, 10, false, null, emptyList()),
        GameItem("fake_it", "Fake It", "Psych! trivia bluff", "Shuffle fake answers with real answer.", GameCategory.TRIVIA, SetupType.PASS_AND_PLAY, 3, 10, 12, false, null, emptyList()),
        GameItem("write_funny", "Write Funny (Quiplash)", "Prompt voting battle", "Write funny answers to absurd questions.", GameCategory.TRIVIA, SetupType.PASS_AND_PLAY, 3, 10, 12, false, null, emptyList()),
        GameItem("name_place_animal", "Name Place Animal", "Speed dictionary race", "Fill text categories with random letter generator.", GameCategory.ACTION, SetupType.PASS_AND_PLAY, 2, 8, 8, false, null, emptyList()),
        GameItem("battleship", "Battleship", "Naval fleet strategy", "Position fleet and call out grid strikes.", GameCategory.BOARD, SetupType.PASS_AND_PLAY, 2, 2, 10, false, null, emptyList()),
        GameItem("hangman", "Hangman", "Virtual keyboard word guess", "Custom word guessing with graphic execution.", GameCategory.TRIVIA, SetupType.PASS_AND_PLAY, 2, 6, 5, false, null, emptyList()),
        GameItem("scrabble_league", "Letter League", "Tile word board", "Classic word building with auto-hide tile overlay.", GameCategory.BOARD, SetupType.PASS_AND_PLAY, 2, 4, 15, false, null, emptyList())
    )
}
