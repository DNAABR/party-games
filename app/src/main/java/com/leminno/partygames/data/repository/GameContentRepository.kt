package com.leminno.partygames.data.repository

/**
 * Data repository providing prompts, words, and challenge content for party games.
 */
object GameContentRepository {

    val whoAmIWords: List<String> = listOf(
        "Harry Potter", "Spider-Man", "Albert Einstein", "Taylor Swift",
        "Mickey Mouse", "Batman", "Sherlock Holmes", "Barack Obama",
        "SpongeBob", "Elon Musk", "Cristiano Ronaldo", "Pikachu",
        "Leonardo DiCaprio", "Serena Williams", "Katy Perry", "Mario",
        "Beyoncé", "Darth Vader", "Dwayne Johnson", "Wonder Woman",
        "Michael Jordan", "Elsa", "Tom Cruise", "Oprah Winfrey",
        "Iron Man", "Lionel Messi", "Lady Gaga", "Shrek",
        "Drake", "Hermione Granger", "Will Smith", "Cleopatra",
        "Homer Simpson", "Naruto", "Rihanna", "Indiana Jones",
        "Gordon Ramsay", "Bugs Bunny", "Adele", "James Bond",
        "Goku", "Ed Sheeran", "Cinderella", "The Rock",
        "Ariana Grande", "Scooby-Doo", "Freddie Mercury", "Groot"
    )

    fun getTruthPrompts(deck: String): List<String> = when (deck) {
        "Clean" -> listOf(
            "What is your biggest fear?",
            "What is your most embarrassing school memory?",
            "What is one secret talent you have?",
            "What is the nicest thing someone has done for you?",
            "What is your most embarrassing habit?",
            "What is the weirdest food you secretly enjoy?",
            "What is your most irrational fear?",
            "What childhood toy do you still miss?",
            "What is the silliest thing you believed as a kid?",
            "What is the longest you have gone without showering?"
        )
        "Extreme" -> listOf(
            "What is the spiciest text you ever sent?",
            "Have you ever cheated on a test?",
            "What is your biggest regret in a relationship?",
            "What is the most embarrassing thing in your camera roll?",
            "Have you ever pretended to be sick to skip plans?",
            "What lie do you tell most often?",
            "What is the worst date you have ever been on?",
            "Have you ever stalked someone on social media?",
            "What is the most reckless thing you have ever done?",
            "What secret would ruin your reputation if it got out?"
        )
        else -> listOf(
            "What is the weirdest dream you ever had?",
            "Who in this room would you survive a zombie apocalypse with?",
            "What is your guilty pleasure song?",
            "What fictional world would you live in?",
            "Who was your most embarrassing celebrity crush?",
            "What is the most childish thing you still do?",
            "What is a movie that always makes you cry?",
            "If you could swap lives with someone here for a day, who?",
            "What is the worst gift you have ever received?",
            "What is something you are terrible at but love doing?"
        )
    }

    fun getDarePrompts(deck: String): List<String> = when (deck) {
        "Clean" -> listOf(
            "Do 10 jumping jacks while singing!",
            "Do your best impression of a chicken!",
            "Speak in a funny accent for 2 rounds.",
            "Do your best robot dance for 15 seconds.",
            "Talk in slow motion for the next minute.",
            "Sing the alphabet backwards.",
            "Do your best celebrity impression.",
            "Let someone draw on your hand with a pen.",
            "Speak only in questions for the next 2 rounds.",
            "Do a dramatic reading of the last text you sent."
        )
        "Extreme" -> listOf(
            "Let someone send a random emoji to your recent contact!",
            "Do 20 pushups right now!",
            "Eat a spoonful of hot sauce or mustard!",
            "Let the group pick a new profile photo for you.",
            "Call a random contact and compliment them.",
            "Show the group your most embarrassing photo.",
            "Do a plank until your next turn.",
            "Let someone post a story on your social media.",
            "Speak in a baby voice for the next 3 rounds.",
            "Dance with no music for 30 seconds."
        )
        else -> listOf(
            "Do an impression of someone in this room!",
            "Sing the chorus of your favorite song loudly!",
            "Let the group design a funny hair style for you.",
            "Do your best catwalk across the room.",
            "Hold an ice cube until it melts.",
            "Talk without closing your mouth for 1 minute.",
            "Act out a scene from your favorite movie.",
            "Let someone tickle you for 10 seconds.",
            "Do a dramatic slow-motion replay of tripping.",
            "Swap an item of clothing with someone for 3 rounds."
        )
    }
}
