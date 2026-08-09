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
            antiCheatNotice = "Use the 2-second hold-to-reveal card so nearby players can't peek!",
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
            antiCheatNotice = "Enter statements privately before passing the phone.",
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
            antiCheatNotice = "No smiling or laughing — let the group decide if the player cracked!",
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
        GameItem(
            id = "mafia_werewolf",
            title = "Mafia / Werewolf",
            tagLine = "Narrator mystery night",
            description = "The app acts as Narrator guiding night cycles (Mafia kill, Doctor save, Detective inspect) with background audio masking!",
            category = GameCategory.MYSTERY,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 5,
            maxPlayers = 16,
            estTimeMinutes = 20,
            isMvp = true,
            antiCheatNotice = "Background audio masking plays during night phase to cover phone tap sounds!",
            rules = listOf(
                GameRuleStep(1, "Assign Roles", "Pass phone so every player views their secret role.", "🕵️"),
                GameRuleStep(2, "Night Phase", "Narrator guides Mafia, Doctor, and Detective actions step-by-step.", "🌙"),
                GameRuleStep(3, "Day Voting", "Group discusses clues and votes to eliminate a suspect!", "☀️")
            )
        ),
        GameItem(
            id = "scribble_and_pass",
            title = "Scribble & Pass",
            tagLine = "Canvas drawing chain",
            description = "Vector stroke canvas drawing! Alternate prompt writing and drawing chains (Write -> Draw -> Guess) for hilarious album reveals.",
            category = GameCategory.ACTION,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 15,
            isMvp = true,
            antiCheatNotice = "Pass device between turns without showing the previous prompt!",
            rules = listOf(
                GameRuleStep(1, "Write Prompt", "Player 1 writes a hilarious secret prompt.", "✍️"),
                GameRuleStep(2, "Draw What You See", "Player 2 draws the prompt on the vector canvas.", "🎨"),
                GameRuleStep(3, "Album Reveal", "Pass phone and view the hilarious chain transformation!", "🖼️")
            )
        ),
        GameItem(
            id = "wavelength",
            title = "Wavelength",
            tagLine = "Spectrum dial wheel",
            description = "A target location is hidden on a spectrum (e.g. Hot - Cold). The Psychic gives a clue, and teammates turn the dial wheel!",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 10,
            estTimeMinutes = 10,
            isMvp = true,
            antiCheatNotice = "Psychic views hidden target slice before closing target cover!",
            rules = listOf(
                GameRuleStep(1, "Psychic Clue", "Psychic sees target position and gives a spectrum clue.", "🔮"),
                GameRuleStep(2, "Turn the Dial", "Teammates turn the physical wheel dial on screen.", "🎡"),
                GameRuleStep(3, "Score Zone", "Reveal target slice and score 2 to 4 points based on accuracy!", "🎯")
            )
        ),
        GameItem(
            id = "codenames",
            title = "Codenames",
            tagLine = "Spymaster word grid",
            description = "Grid of 25 words! Spymasters see secret color key matrix (Red, Blue, Civilian, Assassin); field agents guess words.",
            category = GameCategory.MYSTERY,
            setupType = SetupType.DUAL_DEVICE,
            minPlayers = 4,
            maxPlayers = 10,
            estTimeMinutes = 15,
            isMvp = true,
            antiCheatNotice = "Spymaster view is guarded by a 2-second hold-to-confirm screen dimming state.",
            rules = listOf(
                GameRuleStep(1, "Spymaster Key", "Spymaster views secret color key for 25 grid words.", "🔑"),
                GameRuleStep(2, "Give One Word Clue", "Give a single word clue and number of matching cards.", "🗣️"),
                GameRuleStep(3, "Contact Agents", "Field agents tap cards to reveal team colors!", "🕵️")
            )
        ),
        GameItem(
            id = "twit",
            title = "Twit (Wits & Wagers)",
            tagLine = "Numeric estimation betting",
            description = "App asks obscure numerical trivia questions. Everyone submits a secret numeric guess, then bets points on closest answer!",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 8,
            estTimeMinutes = 10,
            isMvp = true,
            antiCheatNotice = "Guesses are laid out on a spectrum without showing player names until bets are placed!",
            rules = listOf(
                GameRuleStep(1, "Submit Guess", "Write a secret numerical estimate to the trivia prompt.", "🔢"),
                GameRuleStep(2, "Place Bets", "Place betting chips on the guess closest without going over.", "🎰"),
                GameRuleStep(3, "Payout", "Correct answer pays out points to closest guess and bettors!", "💰")
            )
        ),
        GameItem(
            id = "fake_it",
            title = "Fake It (Psych! Bluff)",
            tagLine = "Trivia bluffing battle",
            description = "App asks obscure trivia questions. Players write believable fake answers. App shuffles fake answers with real answer!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 12,
            isMvp = true,
            antiCheatNotice = "Type fake answers privately on screen.",
            rules = listOf(
                GameRuleStep(1, "Write Bluff", "Type a convincing fake answer to the obscure question.", "🤥"),
                GameRuleStep(2, "Vote Answer", "Select the answer you believe is the genuine truth.", "🗳️"),
                GameRuleStep(3, "Score Points", "Earn points for guessing truth and fooling friends with your bluff!", "🏆")
            )
        ),
        GameItem(
            id = "write_funny",
            title = "Write Funny (Quiplash)",
            tagLine = "Prompt voting battle",
            description = "Two players write funny responses to the same prompt. The rest of the group votes on the funnier answer anonymously!",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 3,
            maxPlayers = 10,
            estTimeMinutes = 12,
            isMvp = true,
            antiCheatNotice = "Responses are voted on anonymously without showing author names!",
            rules = listOf(
                GameRuleStep(1, "Write Answer", "Two players write hilarious answers to absurd question.", "⚡"),
                GameRuleStep(2, "Head-to-Head Duel", "App displays both answers side by side.", "⚔️"),
                GameRuleStep(3, "Group Vote", "Rest of group votes on funnier answer!", "👏")
            )
        ),
        GameItem(
            id = "name_place_animal",
            title = "Name Place Animal",
            tagLine = "Speed dictionary race",
            description = "App generates a random letter. Players fill in text boxes for Name, Place, Animal, and Thing under a 30-second timer!",
            category = GameCategory.ACTION,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 8,
            estTimeMinutes = 8,
            isMvp = true,
            antiCheatNotice = "30-second timer auto-locks fields when countdown ends!",
            rules = listOf(
                GameRuleStep(1, "Random Letter", "App generates random letter (e.g., 'S').", "🔤"),
                GameRuleStep(2, "Speed Fill", "Type Name, Place, Animal, Thing starting with letter.", "✍️"),
                GameRuleStep(3, "Auto Score & Vote", "App scores answers with dispute appeal button!", "⭐")
            )
        ),
        GameItem(
            id = "battleship",
            title = "Battleship",
            tagLine = "Naval fleet strategy",
            description = "Secretly place your fleet on a 10x10 grid, then take turns calling out coordinates to sink opponent's fleet!",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 2,
            estTimeMinutes = 10,
            isMvp = true,
            antiCheatNotice = "Full black screen appears between turns to protect grid privacy.",
            rules = listOf(
                GameRuleStep(1, "Deploy Ships", "Place Aircraft Carrier, Battleship, Submarine, Patrol Boat.", "⚓"),
                GameRuleStep(2, "Fire Salvo", "Tap target grid coordinates to strike opponent.", "💥"),
                GameRuleStep(3, "Sink Fleet", "First to sink all 4 opponent ships claims victory!", "🏆")
            )
        ),
        GameItem(
            id = "hangman",
            title = "Hangman",
            tagLine = "Virtual keyboard word guess",
            description = "Type a hidden custom word or let app generate one! Guessers tap virtual QWERTY keys while visual hangman fills in.",
            category = GameCategory.TRIVIA,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 6,
            estTimeMinutes = 5,
            isMvp = true,
            antiCheatNotice = "Custom word entry is masked before turn passes.",
            rules = listOf(
                GameRuleStep(1, "Set Secret Word", "Type custom hidden word or generate random prompt.", "🔑"),
                GameRuleStep(2, "Tap Keyboard", "Guessers tap letters on virtual QWERTY keyboard.", "⌨️"),
                GameRuleStep(3, "Avoid Execution", "Uncover word before 6 wrong guesses complete hangman!", "😵")
            )
        ),
        GameItem(
            id = "scrabble_league",
            title = "Letter League",
            tagLine = "Tile word board",
            description = "Classic word building on a tile board with pop-up tile rack overlay and auto-dictionary validation!",
            category = GameCategory.BOARD,
            setupType = SetupType.PASS_AND_PLAY,
            minPlayers = 2,
            maxPlayers = 4,
            estTimeMinutes = 15,
            isMvp = true,
            antiCheatNotice = "Tile racks are hidden behind 'View My Tiles' pop-up overlay that auto-hides.",
            rules = listOf(
                GameRuleStep(1, "View Tile Rack", "Tap 'View My Tiles' overlay to check your 7 letter tiles.", "🔠"),
                GameRuleStep(2, "Place Word", "Drag or tap tiles onto board grid.", "🧩"),
                GameRuleStep(3, "Submit & Score", "Submit turn for dictionary score validation!", "🏆")
            )
        )
    )
}
