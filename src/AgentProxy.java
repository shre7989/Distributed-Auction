/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class AgentProxy extends Application{
    @Override
    public void start(Stage stage) throws Exception {
        Parameters parameters = getParameters();
        String[] args = parameters.getRaw().toArray(new String[0]);
        String name = args[0];
        Agent agent = new Agent(name);

        VBox layout = new VBox(20);

        AgentGUI agentGUI = new AgentGUI(layout,800,1000,agent);

        stage.setTitle("Agent");
        stage.setScene(agentGUI);
        stage.setResizable(false);
        stage.show();

        try {
            Socket socket = new Socket("localhost",4242);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
            writer.println("Agent");
            agentGUI.setSocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        launch(args);
    }

}
