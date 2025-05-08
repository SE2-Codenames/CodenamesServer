import model.Card.*;
import model.GameState;
import model.Player.Player;
import model.Player.Team;
import model.Player.TeamColor;

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
        if (state != GameState.LOBBY) {
            throw new IllegalStateException("Game has already started");
        }

        if (players.size() < 4) {
            throw new IllegalStateException("Need at least 4 players (2 per team)");
        } else if (players.stream().filter(p -> p.getSpymaster()).count() < 2) {
            throw new IllegalStateException("Both teams need a spymaster");
        }
        // Assign starting team randomly if not set
        if (startingTeam == null) {
            startingTeam = TeamColor.RED;
        }
        currentTurn = startingTeam;

        state = GameState.SPYMASTER_TURN;
    }

    /*public void addPlayer(Player newPlayer) throws GameException {
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
    }*/

    private Optional<Player> getPlayerByUsername(String username) {
        //Looks for player with username, compares it ignoring upper/lower case differences (equalsIgnoreCase)
        //If found: Returns the Player object wrapped in an Optional, If not found: Returns an empty Optional
        //It's a safe way to handle "might not exist" situations,Forces you to think about the case where the player isn't found
        //Better than returning null which can cause errors
        return players.stream().filter(p -> p.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    private String validateClue(String clueWord) throws GameException {
        clueWord = clueWord.trim();

        if (clueWord.isEmpty()) {
            throw new GameException("Clue cannot be empty");
        }
        if (clueWord.matches(".*\\d.*")) {
            throw new GameException("Clue cannot contain numbers");
        }

        String finalClueWord = clueWord;
        if (board.stream().anyMatch(card ->
                card.getWord().equalsIgnoreCase(finalClueWord))) {
            throw new GameException("Clue cannot be a word on the board");
        }

        return clueWord;
    }

    public void giveClue(String clueWord, int number) throws GameException {
        if (state != GameState.SPYMASTER_TURN) {
            throw new GameException("Not spymaster's turn");
        }

        // Validate spymaster is giving the clue
        Player currentSpymaster = players.stream()
                .filter(p -> p.getTeamColor() == currentTurn && p.getSpymaster())
                .findFirst()
                .orElseThrow(() -> new GameException("No spymaster for current team"));

        // Validate clue content
        clueWord = validateClue(clueWord);

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
        if (!guessingPlayer.getSpymaster()) {
            throw new GameException("Only operatives can guess");
        }

        Card guessedCard = findCardOnBoard(word);
        handleCardReveal(guessedCard);
    }

    private Card findCardOnBoard(String word) throws GameException {
        return board.stream()
                .filter(card -> card.getWord().equalsIgnoreCase(word))
                .findFirst()
                .orElseThrow(() -> new GameException("Word not found on board"));
    }

    private void handleCardReveal(Card card) throws GameException {
        if (card.isRevealed()) {
            throw new GameException("Card already revealed");
        }

        card.reveal();
        remainingGuesses--;

        switch (card.getCardRole()) {
            case ASSASSIN:
                handleAssassinReveal();
                break;
            case NEUTRAL:
                handleNeutralReveal(card);
                break;
            default:
                handleTeamCardReveal(card);
        }
    }

    private void handleAssassinReveal() {
        state = GameState.GAME_OVER;
        // Current team loses when revealing assassin
        if (currentTurn == TeamColor.RED) {
            redTeam.setCardsRemaining(0);
        } else {
            blueTeam.setCardsRemaining(0);
        }
        notifyGameOver();
    }

    private void handleNeutralReveal(Card card) {
        card.reveal();
        System.out.println("Neutral card revealed: " + card.getWord());
        endTurn();

    }

    private void handleTeamCardReveal(Card card) {
        Team currentTeam = getCurrentTeam();
        Team opponentTeam = getOpponentTeam();

        if (card.getCardRole().name().equals(currentTurn.name())) {
            // Correct guess
            currentTeam.setCardsRemaining(currentTeam.getCardsRemaining() - 1);

            if (currentTeam.hasWon()) {
                state = GameState.GAME_OVER;
                notifyGameOver();
            } else if (remainingGuesses <= 0) {
                endTurn();
            }
        } else {
            // Wrong team's card
            opponentTeam.setCardsRemaining(opponentTeam.getCardsRemaining() - 1);
            endTurn();

            if (opponentTeam.hasWon()) {
                state = GameState.GAME_OVER;
                notifyGameOver();
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

    private void notifyGameOver() {
        Team winner = redTeam.hasWon() ? redTeam :
                blueTeam.hasWon() ? blueTeam : null;

        if (winner != null) {
            System.out.printf("Game over! %s team wins!%n", winner.getColor());
        } else {
            System.out.println("Game over! No winner!");
        }
    }

    public void resetGame() {
        // Reset board
        Collections.shuffle(board);
        board.forEach(card -> card.revealed = false);

        // Reset teams
        redTeam.setCardsRemaining(9);
        blueTeam.setCardsRemaining(8);
        redTeam.setCurrentTurn(false);
        blueTeam.setCurrentTurn(false);

        // Reset game state
        state = GameState.LOBBY;
        currentTurn = startingTeam;
        currentClue = null;
        currentClueNumber = 0;
        currentClueTeam = null;
        remainingGuesses = 0;

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
                    player.getSpymaster() &&
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

    private Team getCurrentTeam() {
        return currentTurn == TeamColor.RED ? redTeam : blueTeam;
    }

    private Team getOpponentTeam() {
        return currentTurn == TeamColor.RED ? blueTeam : redTeam;
    }
}


