package cv;

import cv.communication.ClientStarter;
import cv.communication.Message;
import cv.communication.Server;
import gui.gamescene.cvinterface.ISolitaireCV;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;


public class SolitaireCV implements ISolitaireCV, Server.ClientConnectCallback, Server.MessageListener, Server.ErrorListener {

    private Server server;
    private ImageUpdateListener imageUpdateListener;
    private GameStateAnalyzer gameStateAnalyzer = new GameStateAnalyzer(416,416);
    private FinishedCallback finishedCallback;
    private boolean paused = false;


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
            clientStarter.setProcessFinishedCallback(this::clientFinished);

            clientStarter.start(port);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Add some real error here
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void unpause() {
        paused = false;
    }

    @Override
    public void setImageUpdateListener(ImageUpdateListener imageUpdateListener) {
        this.imageUpdateListener = imageUpdateListener;
    }


    @Override
    public void setGameStateUpdateListener(GameStateUpdateListener gameStateUpdateListener) {
        gameStateAnalyzer.setUpdateListener(gameStateUpdateListener);
    }

    @Override
    public void setFinishedCallback(FinishedCallback callback) {
        finishedCallback = callback;
    }


    @Override
    public void onClientConnect() {
        try {
            // TODO: Remove this at some point
            server.sendMessage(new Message(1, new JSONObject("{\"msg\" : \"Hello client!\"}")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handles incoming messages from client
    @Override
    public void onServerMessage(Message message) throws IOException { // TODO: Fix exception in method signature

        switch(message.getCode()){
            case 100:
                // Client is ready
                System.out.println("Client is ready!");
                server.sendMessage(new Message(201, null));
                break;
            case 101: // New Detections
                decodeDetections(message.getData());
                System.out.println("Received game state: " + message.getData());
                break;
            case 102: // New Image
                JSONObject data = message.getData();
                if( data.length() == 0 ){
                    server.sendMessage(new Message(201, null));
                }else{
                    decodeImageMessage(data.getString("image"), data.getInt("width"), data.getInt("height"));
                }
                break;

            default:
                System.out.printf("CV: Recieved message with unknown code %d from client\n", message.getCode());

        }
    }




    /*  Decodes an image encoded as base64 into a JavaFX image and notify
    *   the ImageUpdateListener that a new image has been received */
    private void decodeImageMessage(String imageStringData, int width, int height){

        try {
            // Convert from base64 string to byte array
            byte[] imageData = Base64.getDecoder().decode(imageStringData);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

            final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(imageData, 0, targetPixels, 0, imageData.length);

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



    private void decodeDetections(JSONObject data){
        JSONArray jsonDetections = data.getJSONArray("detections");
        List<Detection> detections = new LinkedList<>();
        for(Object detection : jsonDetections ){
            detections.add(Detection.fromJSON((JSONObject) detection));
        }

    }


    public void stop(boolean error){
        finishedCallback.onFinish(error);
    }

    private void clientFinished(int exitCode){
        System.out.println("CV Client has stopped with exit code " + exitCode);
        stop(true);
    }

    @Override
    public void onServerError(String errorMessage) {

    }
}
