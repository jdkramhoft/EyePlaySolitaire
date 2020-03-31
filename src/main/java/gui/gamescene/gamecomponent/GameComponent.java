package gui.gamescene.gamecomponent;

import gui.gamescene.Card;
import gui.gamescene.Card.Suit;
import gui.gamescene.GameState;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class GameComponent implements IGameComponent {

    private GridPane grid = new GridPane();

    private HashMap<Suit, CardPane[]> cardPanesMap = new HashMap<>();
    private CardPane cardBackPane;
    private CardImageLoader imageLoader = new CardImageLoader();


    public GameComponent(){

        // Load Card Images
        HashMap<Suit, Image[]> suitImages = imageLoader.getAllSuits();
        for( Suit suit : suitImages.keySet() ){
            CardPane[] cardPanes = new CardPane[13];
            Image[] images = suitImages.get(suit);
            for( int i=0; i<13; i++ ){
                cardPanes[i] = new CardPane(images[i]);
            }
            cardPanesMap.put(suit, cardPanes);
        }
        cardBackPane = new CardPane(imageLoader.getCardBack());



        // Set background color
        BackgroundFill fill = new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(fill);
        grid.setBackground(background);



        for(int i=0; i<13; i++){
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(100./13);
            grid.getColumnConstraints().add(constraints);
        }

        for(int i=0; i<4; i++){
            RowConstraints constraints = new RowConstraints();
            constraints.setPercentHeight(25);
            grid.getRowConstraints().add(constraints);
        }


        int row=0;
        for( Suit suit : cardPanesMap.keySet() ){
            int value = 0;
            for( CardPane pane : cardPanesMap.get(suit)){
                CardContainer container = new CardContainer(0.05);
                container.setCard(pane);
                grid.add(container, value, row);
                value++;
            }
            row++;
        }
    }

    /**
     * Return the Card Pane matching the given card.
     */
    private CardPane getCardPane(Card card){
        return cardPanesMap.get(card.getSuit())[card.getValue()-1];
    }

    @Override
    public void updateGameState(GameState gameState) {

    }

    @Override
    public Node getNode() {
        return grid;
    }

}