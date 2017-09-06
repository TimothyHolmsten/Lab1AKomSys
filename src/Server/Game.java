package Server;

/**
 * Created by timothy on 2017-09-05, Server.
 */
public class Game {

    private String secret;
    private int guesses;
    private char[] guessedString;

    public Game() {
        secret = "ANVIL";
        guesses = 0;
        guessedString = new char[secret.length()];
        for (int i = 0; i < secret.length(); i++)
            guessedString[i] = '*';
    }

    public String getSecret() {
        return secret;
    }

    public int getGuesses() {
        return guesses;
    }

    public void setGuesses(int guesses) {
        this.guesses = guesses;
    }

    public String getGuessedString() {
        StringBuilder sb = new StringBuilder();
        for (char c : guessedString)
            sb.append(c);
        return sb.toString();
    }

    public void play(char ch) {
        for (int i = 0; i < secret.length(); i++)
            if (Character.toUpperCase(ch) == secret.charAt(i)) {
                guessedString[i] = Character.toUpperCase(ch);
                break;
            }
    }
}
