package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Game {
    private final List<Card> board;
    private final Team redTeam;
    private final Team blueTeam;
    private final List<Player> players;
    private TeamColor startingTeam; // Which team starts first (usually RED)
    private GameState state;
    private final WordBank wordBank;
    private TeamColor currentTurn;
    private String currentClue;
    private int currentClueNumber;
    private TeamColor currentClueTeam;
    private int remainingGuesses;

    public Game(WordBank wordBank) {
        this.redTeam = new Team(TeamColor.RED);
        this.blueTeam = new Team(TeamColor.BLUE);
        this.players = new ArrayList<>();
        this.state = GameState.LOBBY;
        this.wordBank = wordBank;

        this.board = createBoard(wordBank.getRandomWords(25));
    }

    private List<Card> createBoard(List<String> randomWords) {
        List<Card> board = new ArrayList<>();

        // 1. Assign card types (9 red, 8 blue, 7 neutral, 1 assassin)
        for (int i = 0; i < 25; i++) {
            CardRole cardType;
            if (i < 9) cardType = CardRole.RED;          // First 9 = Red Team
            else if (i < 17) cardType = CardRole.BLUE;    // Next 8 = Blue Team
            else if (i < 24) cardType = CardRole.NEUTRAL; // Next 7 = Neutral
            else cardType = CardRole.ASSASSIN;            // Last 1 = Assassin

            board.add(new Card(randomWords.get(i), cardType));
        }
        // 2. Shuffle to randomize positions
        Collections.shuffle(board);
        return board;
    }

    public void startGame() {
        if (players.size() < 4) {
            throw new IllegalStateException("Need at least 4 players (2 per team)");
        } else if (players.stream().filter(p -> p.getPlayerRole() == PlayerRole.SPYMASTER).count() < 2) {
            throw new IllegalStateException("Both teams need a spymaster");
        }
        // Assign starting team randomly if not set
        if (startingTeam == null) {
            startingTeam = TeamColor.RED;
        }
        currentTurn = startingTeam;

        state = GameState.SPYMASTER_TURN;
    }

    public void addPlayer(Player newPlayer) throws GameException {
        validatePlayerAdd(newPlayer);
        players.add(newPlayer);
    }

    private void validatePlayerAdd(Player newPlayer) throws GameException {
        // Check if username is taken
        if (players.stream().anyMatch(p -> p.equals(newPlayer))) {
            throw new GameException("Username '" + newPlayer.getUsername() + "' is already taken");
        }

        // Check if role is taken per team
        if (newPlayer.getTeamColor() != null && newPlayer.getPlayerRole() != PlayerRole.NONE) {
            boolean roleTaken = players.stream().filter(p -> p.getTeamColor() == newPlayer.getTeamColor()).anyMatch(p -> p.getPlayerRole() == newPlayer.getPlayerRole());

            if (roleTaken) {
                String message = String.format(
                        "%s role for %s team is already taken", // exp.: Spymaster for Red team is already taken
                        newPlayer.getPlayerRole(),
                        newPlayer.getTeamColor()
                );
                throw new GameException(message);
            }
        }
    }

    public void assignTeamAndRole(String username, TeamColor team, PlayerRole role) throws GameException {
        Player player = getPlayerByUsername(username).orElseThrow(() -> new GameException("Player not found"));

        // Check if role is already taken in this team
        boolean roleTaken = players.stream().filter(p -> p.getTeamColor() == team).anyMatch(p -> p.getPlayerRole() == role && !p.equals(player));
        if (roleTaken) {
            throw new GameException(role + " role for " + team + " team is already taken");
        }

        player.setTeamColor(team);
        player.setPlayerRole(role);
    }

    private Optional<Player> getPlayerByUsername(String username) {
        //Looks for player with username, compares it ignoring upper/lower case differences (equalsIgnoreCase)
        //If found: Returns the Player object wrapped in an Optional, If not found: Returns an empty Optional
        //It's a safe way to handle "might not exist" situations,Forces you to think about the case where the player isn't found
        //Better than returning null which can cause errors
        return players.stream().filter(p -> p.getUsername().equalsIgnoreCase(username)).findFirst();
    }


    public void giveClue(String clueWord, int number) throws GameException {
        if (state != GameState.SPYMASTER_TURN) {
            throw new GameException("Not spymaster's turn");
        }

        // Validate spymaster is giving the clue
        Player currentSpymaster = players.stream()
                .filter(p -> p.getTeamColor() == currentTurn && p.getPlayerRole() == PlayerRole.SPYMASTER)
                .findFirst()
                .orElseThrow(() -> new GameException("No spymaster for current team"));

        // Trim and validate clue word
        clueWord = clueWord.trim();
        if (clueWord.isEmpty()) {
            throw new GameException("Clue cannot be empty");
        }

        // Validate clue isn't a single letter (unless it's specifically allowed)
        if (clueWord.length() == 1 && !clueWord.matches("[a-zA-Z]")) {
            throw new GameException("Single-letter clues must be alphabetic");
        }

        // Check if clue word is on the board (case-insensitive)
        String finalClueWord = clueWord;
        boolean isWordOnBoard = board.stream()
                .anyMatch(card -> card.getWord().equalsIgnoreCase(finalClueWord));
        if (isWordOnBoard) {
            throw new GameException("Clue cannot be a word on the board");
        }

        // Check for compound words (optional rule)
        if (clueWord.contains(" ")) {
            String[] parts = clueWord.split(" ");
            for (String part : parts) {
                if (board.stream().anyMatch(card -> card.getWord().equalsIgnoreCase(part))) {
                    throw new GameException("Clue cannot contain words from the board");
                }
            }
        }

        // Validate number
        if (number < 0) {
            throw new GameException("Number must be positive (use 0 for unlimited)");
        }

        // Optional: Limit maximum number
        if (number > 10) {
            throw new GameException("Number cannot exceed 10");
        }

        // Store the clue information
        this.currentClue = clueWord;
        this.currentClueNumber = number;
        this.currentClueTeam = currentTurn;

        // Reset remaining guesses (number + 1 for the "zero means unlimited" case)
        remainingGuesses = (number == 0) ? Integer.MAX_VALUE : number + 1;

        // Transition to operative turn
        state = GameState.OPERATIVE_TURN;

        // Notify players about the new clue
        // TODO: Implement player notification system
    }

    // Modify the guessCard method to handle clue-based guessing
    public void guessCard(String word, Player guessingPlayer) throws GameException {
        if (state == GameState.GAME_OVER) {
            throw new GameException("Game has already ended");
        }
        if (state != GameState.OPERATIVE_TURN) {
            throw new GameException("Not operative's turn");
        }
        if (guessingPlayer.getTeamColor() != currentTurn) {
            throw new GameException("It's not your team's turn");
        }
        if (guessingPlayer.getPlayerRole() != PlayerRole.OPERATIVE) {
            throw new GameException("Only operatives can guess");
        }

        // Check if team has guesses remaining
        if (remainingGuesses <= 0) {
            throw new GameException("No guesses remaining for this clue");
        }

        // Find the card that was guessed
        Card guessedCard = board.stream()
                .filter(c -> c.getWord().equalsIgnoreCase(word) && !c.isRevealed())
                .findFirst()
                .orElseThrow(() -> new GameException("Invalid word or already revealed"));

        // Reveal the card
        guessedCard.reveal();
        remainingGuesses--;

        // Handle the result of the guess
        Team currentTeam = currentTurn == TeamColor.RED ? redTeam : blueTeam;
        Team opponentTeam = currentTurn == TeamColor.RED ? blueTeam : redTeam;

        switch (guessedCard.getCardRole()) {
            case ASSASSIN:
                // Game over - current team loses
                state = GameState.GAME_OVER;
                currentTeam.setCardsRemaining(0); // Mark as lost
                break;

            case NEUTRAL:
                // End turn regardless of remaining guesses
                endTurn();
                break;

            default:
                if (guessedCard.getCardRole().name().equals(currentTurn.name())) {
                    // Correct guess
                    currentTeam.setCardsRemaining(currentTeam.getCardsRemaining() - 1);

                    // Check for win condition
                    if (currentTeam.hasWon()) {
                        state = GameState.GAME_OVER;
                    }
                    // Continue turn if guesses remain and not won
                    else if (remainingGuesses <= 0) {
                        endTurn();
                    }
                } else {
                    // Wrong team's card
                    opponentTeam.setCardsRemaining(opponentTeam.getCardsRemaining() - 1);
                    endTurn();

                    // Check if opponent won by mistake
                    if (opponentTeam.hasWon()) {
                        state = GameState.GAME_OVER;
                    }
                }
        }
    }

    private void endTurn() {
        if (state == GameState.OPERATIVE_TURN) {
            // Operative turn ends -> switch to other team's spymaster turn
            currentTurn = (currentTurn == TeamColor.RED) ? TeamColor.BLUE : TeamColor.RED;
            state = GameState.SPYMASTER_TURN;
        }
        else if (state == GameState.SPYMASTER_TURN) {
            // Spymaster gave clue -> switch to same team's operative turn
            state = GameState.OPERATIVE_TURN;
        }

        // Reset clue information when switching teams
        if (state == GameState.SPYMASTER_TURN) {
            currentClue = null;
            currentClueNumber = 0;
            remainingGuesses = 0;
        }

        // Notify players about turn change
        notifyTurnChange();
    }

    // Add this helper method
    private void notifyTurnChange() {
        // TODO: Implement actual player notification
        System.out.println("Turn changed: " + currentTurn + " - " + state);
    }

    public List<Card> getBoardState(Player player) {
        // Return the board state appropriate for the player's role
        List<Card> visibleCards = new ArrayList<>();

        for (Card card : board) {
            if (card.isRevealed()) {
                // Show all revealed cards to everyone
                visibleCards.add(new Card(card.getWord(), card.getCardRole()));
            }
            else if (player != null &&
                    player.getPlayerRole() == PlayerRole.SPYMASTER &&
                    player.getTeamColor() != null &&
                    player.getTeamColor().name().equals(card.getCardRole().name())) {
                // Show spymaster their own team's hidden cards
                visibleCards.add(new Card(card.getWord(), card.getCardRole()));
            }
            else {
                // Hide unrevealed cards (null indicates hidden)
                visibleCards.add(new Card(card.getWord(), null));
            }
        }
        return visibleCards;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}


