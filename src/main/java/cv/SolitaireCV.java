package cv;

import cv.communication.ClientStarter;
import cv.communication.Message;
import cv.communication.Server;
import gui.gamescene.cvinterface.ISolitaireCV;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class SolitaireCV implements ISolitaireCV, Server.ClientConnectCallback, Server.MessageListener, Server.ErrorListener {

    private Server server;
    private ImageUpdateListener imageUpdateListener;
    private GameStateUpdateListener gameStateUpdateListener;


    @Override
    public void start() {
        try {
            server = new Server(this, this);
            int port = 0;
            port = server.start();

            System.out.println("Started computer vision server on port " + port);

            ClientStarter clientStarter = new ClientStarter();

            // TODO: Consider if these should be removed
            clientStarter.setStandardOutputListener((msg) -> System.out.println("Message: " + msg));
            clientStarter.setErrorOutputListener((msg) -> System.out.println("Error: " + msg));
            clientStarter.setProcessFinishedCallback((exitCode) -> System.out.println("Client process finished with exit code " + exitCode));

            clientStarter.start(port);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Add some real error here
        }
    }

    @Override
    public void setImageUpdateListener(ImageUpdateListener imageUpdateListener) {
        this.imageUpdateListener = imageUpdateListener;
    }


    @Override
    public void setGameStateUpdateListener(GameStateUpdateListener gameStateUpdateListener) {
        this.gameStateUpdateListener = gameStateUpdateListener;
    }


    @Override
    public void onClientConnect() {
        try {
            server.sendMessage(new Message(1, new JSONObject("{\"msg\" : \"Hello client!\"}")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handles incoming messages from client
    @Override
    public void onMessage(Message message) {

        switch(message.getCode()){
            case 101: // New Game State
                break;
            case 102: // New Image
                decodeImageMessage(message.getData().getString("image"));
                break;
            default:
                System.out.printf("CV: Recieved message with unknown code %d from client\n", message.getCode());

        }
    }


    /*  Decodes an image encoded as base64 into a JavaFX image and notify
    *   the ImageUpdateListener that a new image has been received */
    private void decodeImageMessage(String imageStringData){

        try {
            // Convert from base64 string to byte array
            byte[] imageData = Base64.getDecoder().decode(imageStringData);

            // Read in bytes as image
            ByteArrayInputStream byteStream = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(byteStream);
            System.out.println("IMAGE IS NULL: " + (image == null));

            // Convert to JavaFX image and notify
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            if( imageUpdateListener != null )
                imageUpdateListener.onImageUpdate(fxImage);

            // Signal to client that image was received
            server.sendMessage(new Message(201, null));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onError(String errorMessage) {

    }
}
