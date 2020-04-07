package gui.gamescene;

import gui.gamescene.Card.Suit;
import java.util.ArrayList;
import java.util.Random;


/**
 * >> TEMPORARY CLASS <<
 * Random State generator for testing
 */
class GameStateGenerator {

    /**
     * Generate game state from random seed
     * The state does not adhere to the game rules
     * (the cards in the different piles are completely random)
     */
    static GameState generateGameState() {
        return generateGameState(System.currentTimeMillis());
    }

    /**
     * Generate game state from specific seed
     * The state does not adhere to the game rules
     * (the cards in the different piles are completely random)
     */
    static GameState generateGameState(long seed) {
        Random rand = new Random(seed);

        // Generate Deck
        ArrayList<Card> cards = new ArrayList<>();
        for( Suit suit : Suit.values() ){
            if( suit == Suit.UNKNOWN ) continue;
            for( int i=1; i<=13; i++ ){
                cards.add(new Card(suit, i));
            }
        }

        GameState game = new GameState();

        // Add 3 cards to flipped:
        game.addToFlipped(cards.remove(rand.nextInt(cards.size())));
        game.addToFlipped(cards.remove(rand.nextInt(cards.size())));
        game.addToFlipped(cards.remove(rand.nextInt(cards.size())));

        while( cards.size() > 15 ){
            int i = rand.nextInt(3);

            // Pick and remove random card
            Card card = cards.remove(rand.nextInt(cards.size()));

            // Small chance of the card being replaced by unknown
            if( rand.nextInt(8) == 1 )
                card = Card.createUnknown();

            // Add to random tableau
            if( i < 2 ) {
                int tableauIndex = rand.nextInt(7);
                if (game.getTableaus().get(tableauIndex).size() < 13) {
                    game.addToTableau(tableauIndex, card);
                    continue;
                }
            }

            // Add to random foundation
            int foundationIndex = rand.nextInt(4);
            if (game.getFoundations().get(foundationIndex).size() < 13) {
                game.addToFoundations(foundationIndex, card);
                continue;
            }

            // Couldn't add card (readd it to the stack)
            cards.add(card);
        }

        for( Card card : cards) game.addToStock(card);

        return game;
    }
}
