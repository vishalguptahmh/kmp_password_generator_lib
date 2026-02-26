package io.github.kotlin.passwordgenerator

/**
 * Use case for generating secure passphrases using word lists.
 * 
 * @param repository Repository for persisting passphrase generation settings
 */
class PassphraseGeneratorUseCase(private val repository: PasswordGeneratorRepository) {



    /**
     * Generates a secure passphrase using a word list.
     * 
     * @param wordCount Number of words in passphrase (must be between 1 and 20)
     * @param separator String to separate words
     * @param includeUppercase Randomly capitalize some words
     * @param includeLowercase Keep some words lowercase
     * @param includeNumbers Add numbers to some words
     * @param includeSpecialChars Add special characters to some words
     * @param isPasswordRegenerate If true, doesn't save settings to repository
     * @return Generated passphrase
     * @throws IllegalArgumentException if wordCount is invalid
     */
    suspend operator fun invoke(
        wordCount: Int,
        separator: String,
        includeUppercase: Boolean,
        includeLowercase: Boolean,
        includeNumbers: Boolean,
        includeSpecialChars: Boolean,
        isPasswordRegenerate: Boolean = false
    ): String {
        require(wordCount in PassphraseConstants.MIN_WORD_COUNT..PassphraseConstants.MAX_WORD_COUNT) {
            "Word count must be between ${PassphraseConstants.MIN_WORD_COUNT} and ${PassphraseConstants.MAX_WORD_COUNT}"
        }
        
        // Save settings if not regenerating
        if (!isPasswordRegenerate) {
            repository.savePassphraseSettings(
                PassphraseSettings(
                    wordCount = wordCount,
                    separator = separator,
                    includeUppercase = includeUppercase,
                    includeLowercase = includeLowercase,
                    includeNumbers = includeNumbers,
                    includeSpecialChars = includeSpecialChars
                )
            )
        }
        val words = listOf(
            "ability", "able", "about", "above", "accept", "account", "across", "action",
            "activity", "address", "admit", "adult", "affect", "after", "again", "against",
            "agency", "agent", "agree", "ahead", "allow", "almost", "alone", "along",
            "already", "also", "although", "always", "amazing", "among", "amount", "analysis",
            "animal", "another", "answer", "anyone", "anything", "appear", "apply", "approach",
            "area", "argue", "around", "arrive", "article", "artist", "assume", "attack",
            "attention", "attorney", "audience", "author", "authority", "available", "avoid",
            "away", "baby", "back", "ball", "bank", "base", "beautiful", "because", "become",
            "before", "begin", "behavior", "behind", "believe", "benefit", "best", "better",
            "between", "beyond", "billion", "birth", "black", "blood", "blue", "board", "body",
            "book", "born", "both", "break", "bring", "brother", "budget", "build", "building",
            "business", "camera", "campaign", "cancer", "candidate", "capital", "career",
            "carry", "case", "catch", "cause", "cell", "center", "central", "century",
            "certain", "certainly", "chair", "challenge", "chance", "change", "character",
            "charge", "check", "child", "choice", "choose", "church", "citizen", "city",
            "civil", "claim", "class", "clear", "clearly", "close", "coach", "cold",
            "collection", "college", "color", "come", "commercial", "common", "community",
            "company", "compare", "computer", "concern", "condition", "conference", "congress",
            "consider", "consumer", "contain", "continue", "control", "cost", "could",
            "country", "couple", "course", "court", "cover", "create", "crime", "cultural",
            "culture", "current", "customer", "dark", "data", "daughter", "dead", "deal",
            "death", "debate", "decade", "decide", "decision", "deep", "defense", "degree",
            "democratic", "describe", "design", "despite", "detail", "determine", "develop",
            "development", "difference", "different", "difficult", "dinner", "direction",
            "director", "discover", "discuss", "discussion", "disease", "doctor", "door",
            "down", "draw", "dream", "drive", "drop", "drug", "during", "each", "early",
            "east", "easy", "economic", "economy", "edge", "education", "effect", "effort",
            "eight", "either", "election", "else", "employee", "energy", "enjoy", "enough",
            "enter", "entire", "environment", "environmental", "especially", "establish",
            "even", "evening", "event", "ever", "every", "everybody", "everyone", "everything",
            "evidence", "exactly", "example", "executive", "exist", "expect", "experience",
            "expert", "explain", "face", "fact", "factor", "fail", "fall", "family", "famous",
            "father", "fear", "federal", "feel", "feeling", "festive", "field", "fight",
            "figure", "fill", "film", "final", "finally", "financial", "find", "fine",
            "finger", "finish", "fire", "firm", "first", "fish", "five", "floor", "focus",
            "follow", "food", "foot", "force", "foreign", "forget", "form", "format", "former",
            "forward", "four", "free", "freedom", "friend", "from", "front", "full", "fund",
            "future", "game", "garden", "general", "generation", "girl", "give", "glass",
            "goal", "good", "government", "great", "green", "ground", "group", "grow",
            "growth", "guess", "half", "hand", "hang", "happen", "happy", "hard", "have",
            "head", "health", "hear", "heart", "heat", "heavy", "help", "here", "herself",
            "high", "himself", "history", "hold", "home", "hope", "hospital", "hotel", "hour",
            "house", "however", "huge", "human", "hundred", "husband", "idea", "identify",
            "image", "imagine", "impact", "important", "improve", "include", "including",
            "increase", "indeed", "indicate", "individual", "industry", "information",
            "inside", "instead", "institution", "interest", "interesting", "international",
            "interview", "into", "investment", "involve", "issue", "item", "itself", "join",
            "just", "keep", "keynote", "kind", "kitchen", "know", "knowledge", "land",
            "language", "large", "last", "late", "later", "laugh", "lawyer", "lead", "leader",
            "learn", "least", "leave", "left", "legal", "less", "letter", "level", "library",
            "life", "light", "like", "likely", "line", "list", "listen", "little", "live",
            "local", "long", "look", "lose", "loss", "love", "machine", "magazine", "main",
            "maintain", "major", "majority", "make", "manage", "management", "manager",
            "many", "market", "marriage", "material", "matter", "maybe", "mean", "measure",
            "media", "medical", "meet", "meeting", "member", "memory", "mention", "message",
            "method", "middle", "might", "military", "million", "mind", "minute", "miss",
            "mission", "model", "modern", "moment", "money", "month", "more", "morning",
            "most", "mother", "mountain", "move", "movement", "movie", "much", "music",
            "must", "myself", "name", "nation", "national", "natural", "nature", "near",
            "nearly", "necessary", "need", "network", "never", "news", "newspaper", "next",
            "nice", "night", "none", "north", "note", "nothing", "notice", "number", "occur",
            "offer", "office", "officer", "official", "often", "once", "only", "onto",
            "open", "operation", "opportunity", "option", "order", "organization", "other",
            "others", "otherwise", "outside", "over", "owner", "page", "pain", "painting",
            "paper", "parent", "part", "participant", "particular", "particularly", "partner",
            "party", "pass", "past", "patient", "pattern", "peace", "people", "perform",
            "performance", "perhaps", "period", "person", "personal", "phone", "physical",
            "pick", "picture", "piece", "place", "plan", "plant", "play", "player", "point",
            "police", "policy", "political", "politics", "poor", "popular", "population",
            "position", "positive", "possible", "power", "practice", "prepare", "present",
            "president", "pressure", "pretty", "prevent", "price", "private", "probably",
            "problem", "process", "produce", "product", "production", "professional",
            "professor", "program", "project", "property", "protect", "prove", "provide",
            "public", "pull", "purpose", "push", "quality", "question", "quickly", "quite",
            "race", "radio", "raise", "range", "rate", "rather", "reach", "read", "ready",
            "real", "reality", "realize", "really", "reason", "receive", "recent", "recently",
            "recognize", "record", "reduce", "reflect", "reform", "region", "relate",
            "relationship", "religious", "remain", "remember", "remove", "report", "represent",
            "republican", "require", "research", "resource", "respond", "response",
            "responsibility", "rest", "result", "return", "reveal", "rich", "right", "rise",
            "risk", "road", "rock", "role", "room", "rule", "safe", "same", "save", "scene",
            "school", "science", "scientist", "score", "season", "seat", "second", "section",
            "security", "seek", "seem", "sell", "send", "senior", "sense", "series", "serious",
            "serve", "service", "seven", "several", "shake", "share", "shoot", "short",
            "shot", "should", "shoulder", "show", "side", "sign", "significant", "similar",
            "simple", "simply", "since", "sing", "single", "sister", "site", "situation",
            "size", "skill", "skin", "small", "smile", "social", "society", "soldier", "some",
            "somebody", "someone", "something", "sometimes", "song", "soon", "sort", "sound",
            "source", "south", "southern", "space", "speak", "special", "specific", "speech",
            "spend", "sport", "spring", "staff", "stage", "stand", "standard", "star",
            "start", "state", "statement", "station", "stay", "step", "still", "stock",
            "stop", "store", "story", "strategy", "street", "strong", "structure", "student",
            "study", "stuff", "style", "subject", "success", "successful", "such", "suddenly",
            "suffer", "suggest", "summer", "support", "sure", "surface", "system", "table",
            "take", "talk", "task", "teacher", "team", "technology", "television", "tell",
            "tend", "term", "test", "than", "thank", "that", "their", "them", "themselves",
            "then", "theory", "there", "these", "they", "thing", "think", "third", "this",
            "those", "though", "thought", "thousand", "threat", "three", "through",
            "throughout", "throw", "thus", "time", "today", "together", "tonight", "total",
            "tough", "toward", "town", "trade", "traditional", "training", "travel", "treat",
            "treatment", "tree", "trial", "trip", "trouble", "true", "truth", "turn", "type",
            "under", "understand", "unit", "until", "upon", "usually", "value", "various",
            "very", "victim", "view", "violence", "visit", "voice", "vote", "wait", "walk",
            "wall", "want", "watch", "water", "weapon", "wear", "week", "weight", "well",
            "west", "western", "what", "whatever", "when", "where", "whether", "which",
            "while", "white", "whole", "whom", "whose", "wide", "wife", "will", "window",
            "wish", "with", "within", "without", "woman", "wonder", "word", "work", "worker",
            "world", "worry", "would", "write", "writer", "wrong", "yard", "yeah", "year",
            "yellow", "young", "yourself", "zealous", "zigzag", "zone"
        )

        val whereToAttach = listOf(PassphraseConstants.ATTACH_FRONT, PassphraseConstants.ATTACH_REAR)
        val specialChars = "!#$%&()*+-./:;<=>?@[]^_{|}~\'"
        val availableConfigs = mutableListOf<Int>().apply {
            add(PassphraseConstants.SKIP_ACTION)
            if (includeUppercase) add(PassphraseConstants.INCLUDE_UPPERCASE)
            if (includeLowercase) add(PassphraseConstants.INCLUDE_LOWERCASE)
            if (includeNumbers) add(PassphraseConstants.INCLUDE_NUMBERS)
            if (includeSpecialChars) add(PassphraseConstants.INCLUDE_SPECIAL_CHARACTERS)
        }

        val random = SecureRandom()
        val selectedWords = List(wordCount) { words[random.nextInt(words.size)] }

        val processedWords = selectedWords.map { word ->
            val selectRandomConfig = availableConfigs[random.nextInt(availableConfigs.size)]
            val randomPosToAttach = whereToAttach[random.nextInt(whereToAttach.size)]

            when (selectRandomConfig) {
                PassphraseConstants.INCLUDE_UPPERCASE -> word.replaceFirstChar { it.uppercase() }
                PassphraseConstants.INCLUDE_LOWERCASE -> word.lowercase()
                PassphraseConstants.INCLUDE_NUMBERS -> {
                    val number = random.nextInt(1000)
                    if (randomPosToAttach == PassphraseConstants.ATTACH_FRONT) {
                        "$number$word"
                    } else {
                        "$word$number"
                    }
                }
                PassphraseConstants.INCLUDE_SPECIAL_CHARACTERS -> {
                    val specialChar = specialChars[random.nextInt(specialChars.length)]
                    if (randomPosToAttach == PassphraseConstants.ATTACH_FRONT) {
                        "$specialChar$word"
                    } else {
                        "$word$specialChar"
                    }
                }
                else -> word
            }
        }

        return processedWords.joinToString(separator)
    }

}