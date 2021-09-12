/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class AgentGUI extends Scene {
    private final Agent agent;
    private Socket socket;
    private ComboBox<String> agentItems;
    private ComboBox<String> connectedAuctions;
    private Label balance;

    public AgentGUI(VBox layout, int width, int height, Agent agent){
        super(layout,width,height);
        this.agent = agent;
        setup(layout);
        System.out.println("DONE!");
    }

    /**
     * setup GUI
     * @param layout
     */
    public void setup(VBox layout){
        /************************ top part *************************/
        HBox topBox = new HBox(20);
        topBox.setAlignment(Pos.CENTER_LEFT);

        HBox blankBox = new HBox();
        blankBox.getChildren().add(new Label("  "));

        Canvas profile = new Canvas(70,70);
        GraphicsContext paint = profile.getGraphicsContext2D();
        paint.setFill(Color.rgb(0,0,0));
        paint.fillOval(0,0,70,70);

        Label agentName = new Label(agent.getName());
        agentName.setStyle("-fx-font-weight: bold; -fx-stroke: black;");
        agentName.setFont(new Font("Sans Serif",20));

        VBox nameBox = new VBox();
        nameBox.setAlignment(Pos.CENTER);
        nameBox.getChildren().add(agentName);

        Rectangle space1 = new Rectangle();
        space1.setWidth(20);
        Rectangle space2 = new Rectangle();
        space2.setWidth(240);

        /************************ bottom part below ************************/
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.TOP_LEFT);

        VBox rightColumn = new VBox(20);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setStyle("-fx-background-color: #000000");

        TextArea log = new TextArea();
        log.setFont(new Font("Sans Serif", 15));
        log.setWrapText(true);
        log.setMaxWidth(650);
        log.setEditable(false);
        log.appendText("******************************************* Log ");
        log.appendText("****************************************************\n\n");

        /*************************** tabs *********************************/
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #405de6;");
        /* log tab */
        VBox logBox = new VBox(10);
        logBox.setAlignment(Pos.CENTER);

        HBox logButtonBox = new HBox(20);
        logButtonBox.setAlignment(Pos.CENTER);

        Label amount = new Label("Amount:");
        amount.setTextFill(Color.WHITE);

        TextField amountField = new TextField();
        amountField.setPromptText("0");
        amountField.setMaxWidth(50);
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Label itemId = new Label("Item Id:");
        itemId.setTextFill(Color.WHITE);

        TextField itemIdField = new TextField();
        itemIdField.setPromptText("Id");
        itemIdField.setMaxWidth(100);
        itemIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                itemIdField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Label auctionId = new Label("Auction Id:");
        auctionId.setTextFill(Color.WHITE);

        TextField auctionIdField = new TextField();
        auctionIdField.setPromptText("Id");
        auctionIdField.setMaxWidth(100);
        auctionIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                auctionIdField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Label blank = new Label("");
        Label blank2 = new Label("");
        Label blank3 = new Label("");
        Label blank4 = new Label("");
        Label blank5 = new Label("");

        Button bidButton = new Button("Make Bid");
        bidButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px");
        bidButton.setOnAction(event-> {

            if (!amountField.getText().isEmpty() && !itemIdField.getText().isEmpty() && !auctionIdField.getText().isEmpty()) {
                int actId = Integer.parseInt(auctionIdField.getText());
                if (agent.isConnected(actId)) { /* checks if the agent is connected to the given auction Id */

                    log.appendText("[Auction House]: (update) Bid initiated by agent\n");
                    log.appendText("[Auction House]: (information) --> bid will be held for 10 seconds\n");
                    log.appendText("---------------------------------------------");
                    log.appendText("-----------------------------------------------------------\n");

                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            try {
                                agent.updateBidCount(); /* agent starts bidding */
                                int amount1 = Integer.parseInt(amountField.getText());
                                int itemId1 = Integer.parseInt(itemIdField.getText());
                                int auctionId12 = Integer.parseInt(auctionIdField.getText());

                                Socket auctionSocket = agent.getAuctionSocket(auctionId12);
                                ArrayList<Object> data = new ArrayList<>();

                                /* Initiate Bid */
                                data.add("Bid");
                                data.add(amount1);
                                data.add(itemId1);
                                data.add(agent.getAgentId());
                                data.add(agent.getName());
                                ObjectOutputStream out = new ObjectOutputStream(auctionSocket.getOutputStream());
                                out.writeObject(data);

                                /* get bid status */
                                ObjectInputStream in = new ObjectInputStream(auctionSocket.getInputStream());
                                data = (ArrayList<Object>) in.readObject();
                                String input = (String) data.get(0);
                                /* check if bid was valid */
                                if (input.equals("Valid")) {

                                    in = new ObjectInputStream(auctionSocket.getInputStream());
                                    data = (ArrayList<Object>) in.readObject();
                                    String result = (String) data.get(0);
                                    String message = (String) data.get(1);

                                    if (result.equals("Accepted")) {
                                        in = new ObjectInputStream(auctionSocket.getInputStream());
                                        data.clear(); /* reuse */
                                        data = (ArrayList<Object>) in.readObject();
                                        String status = (String) data.get(0);

                                        if (status.equals("Winner")) {
                                            Item item = (Item) data.get(1);
                                            message = (String) data.get(2);
                                            agent.updateItemsList(item);

                                            log.appendText(message);
                                            log.appendText("(Request) Fund transfer from ---> agentId: ");
                                            log.appendText(Integer.toString(agent.getAgentId()));
                                            log.appendText(" -to-> Auction Id: " + auctionId12 + " :[You]\n\n");

                                            /* transfer funds from Agent account to AuctionHouse Account */
                                            data.clear();
                                            data.add("Transfer");
                                            data.add(amount1);
                                            data.add(agent.getAgentId());
                                            data.add(auctionId12);

                                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                            oos.writeObject(data);

                                            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                                            ArrayList<Object> dataInput = (ArrayList<Object>) ois.readObject();
                                            String message1 = (String) dataInput.get(0);
                                            String message22 = (String) dataInput.get(1);

                                            data.clear();
                                            data.add("Funds");
                                            data.add(agent.getAgentId());
                                            ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
                                            oo.writeObject(data);

                                            data.clear();
                                            ObjectInputStream ii = new ObjectInputStream(socket.getInputStream());
                                            data = (ArrayList<Object>) ii.readObject();
                                            int funds = (Integer) data.get(0);

                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    balance.setText("$" + funds);
                                                }
                                            });

                                            log.appendText(message1);
                                            log.appendText(message22);
                                            log.appendText("---------------------------------------------------");
                                            log.appendText("-----------------------------------------------------\n");
                                            agent.decBidCount();
                                            updateMyItems(agentItems, item.getName());
                                        } else {
                                            log.appendText(status);
                                            log.appendText("------------------------------------------------------");
                                            log.appendText("--------------------------------------------------\n");
                                            agent.decBidCount();
                                        }
                                    } else {
                                        log.appendText(message);
                                        log.appendText("---------------------------------------------------------");
                                        log.appendText("-----------------------------------------------\n");
                                        agent.decBidCount();
                                    }
                                } else {
                                    log.appendText(input);
                                    log.appendText("----------------------------------------------------------");
                                    log.appendText("----------------------------------------------\n");
                                    agent.decBidCount();
                                }
                            } catch (IOException |
                                    ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                    ProgressBar bar = new ProgressBar();
                    bar.progressProperty().bind(task.progressProperty());
                    new Thread(task).start();
                }
            }
        });

        HBox ComboBox = new HBox(20);
        ComboBox.setAlignment(Pos.CENTER_RIGHT);

        agentItems = new ComboBox<>();
        agentItems.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-font-size: 15px");
        agentItems.setPromptText("List of Items");

        connectedAuctions = new ComboBox<>();
        connectedAuctions.setPromptText("List of Auction Ids");
        connectedAuctions.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-font-size: 15px");

        balance = new Label("$1000");
        balance.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-stroke: black; -fx-font-size: 30px");

        Rectangle rect11 = new Rectangle();
        rect11.setWidth(55);

        Rectangle rec12 = new Rectangle();
        rec12.setWidth(190);
        ComboBox.getChildren().addAll(balance,rec12,agentItems,connectedAuctions,rect11);

        logButtonBox.getChildren().addAll(blank,amount,amountField,itemId,itemIdField,auctionId,auctionIdField,blank2);
        logBox.getChildren().addAll(blank3,ComboBox,log,blank5,logButtonBox, bidButton,blank4);

        Tab logTab = new Tab("Log",logBox);

        /* other tabs */
        Tab tasks = new Tab("Tasks");
        Tab info = new Tab("Info");

        /* auction house tab*/
        TextArea auctionList = new TextArea();
        auctionList.setWrapText(true);
        auctionList.setMaxWidth(350);
        auctionList.setMaxHeight(750);
        auctionList.setStyle("-fx-background-color: #444444; -fx-font-size: 15px");

        VBox auctionBox = new VBox(20);
        auctionBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button refresh = new Button("Refresh Auction List");
        refresh.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px");
        refresh.setOnAction(event -> {
            auctionList.clear();
            Thread getList = new Thread(() -> {
                try {
                    ArrayList<Object> dataOut = new ArrayList<>();
                    dataOut.add("Auction List");

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(dataOut);

                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                    ArrayList<Object> data = (ArrayList<Object>) in.readObject();

                    ArrayList<Integer> Ids = (ArrayList<Integer>) data.get(0);
                    HashMap<Integer, String> auctionBook = (HashMap<Integer, String>) data.get(1);

                    showAuctionList(Ids,auctionBook,auctionList);

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            getList.start();
        });

        /* connect box at top of available auction house tab */
        HBox connectBox = new HBox(20);
        connectBox.setAlignment(Pos.CENTER);

        TextField connectField = new TextField("");
        connectField.setPromptText("Auction Id");
        connectField.setMaxWidth(100);
        connectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                connectField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        Label connectUpdate = new Label("");
        connectUpdate.setStyle("-fx-background-color: #405de6; -fx-text-fill: white; -fx-font-size: 15");

        Button connect = new Button("Connect");
        connect.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px");
        connect.setOnAction(event ->{
            int actId = 0;
            if(!connectField.getText().isEmpty()) actId = Integer.parseInt(connectField.getText());
            if(!connectField.getText().isEmpty() && !agent.hasAuction(actId)){
                Platform.runLater(() -> {
                    try {
                        int auctionId1 = Integer.parseInt(connectField.getText());
                        connectField.clear();

                        ArrayList<Object> dataOut = new ArrayList<>();
                        dataOut.add("Connect");
                        dataOut.add(auctionId1);

                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(dataOut);

                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                        ArrayList<Object> dataIn = (ArrayList<Object>) in.readObject();

                        int portNo = (Integer) dataIn.get(0);

                        if(portNo != 0) {
                            agent.connectToAuction(auctionId1, portNo); /* stores the auction server port info */

                            Socket socket = new Socket("localhost", agent.getConnectedAuctions().get(auctionId1));
                            agent.updateAuctionSockets(auctionId1, socket); /* create a socket to listen from auction server */

                            String message = "[You]: (Connected) to Auction: " + auctionId1 + " @Port: " + portNo + "\n";

                            log.appendText(message);
                            log.appendText("--------------------------------------------------");
                            log.appendText("------------------------------------------------------\n");

                            connectUpdate.setText("Connected to Auction at port: " + portNo);
                            String auction = "Act: " + Integer.toString(auctionId1);
                            updateMyItems(connectedAuctions, auction);
                        }
                        else{
                            connectUpdate.setText("Auction not Found");
                        }

                    } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
                });
            }
        });


        connectBox.getChildren().addAll(connect,connectField,connectUpdate);

        HBox itemsBox = new HBox(20);
        itemsBox.setAlignment(Pos.CENTER);

        TextField itemField = new TextField();
        itemField.setMaxWidth(80);
        itemField.setPromptText("Auction Id");
        itemField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                itemField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        TextArea itemsList = new TextArea();
        itemsList.setWrapText(true);
        itemsList.setEditable(false);
        itemsList.setMaxWidth(350);
        itemsList.setMaxHeight(750);
        itemsList.setStyle("-fx-background-color: #444444; -fx-font-size: 15px");

        HBox listBox = new HBox(20);
        listBox.setAlignment(Pos.CENTER);
        listBox.setMaxHeight(750);
        listBox.getChildren().addAll(auctionList,itemsList);

        Button getItems = new Button("Get Items");
        getItems.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 15px");
        getItems.setOnAction(event->{
            if(!itemField.getText().isEmpty() && agent.hasAuction(Integer.parseInt(itemField.getText())) && agent.isRegistered()){
                final int auctionId2 = Integer.parseInt(itemField.getText());

                try{
                    itemsList.clear();
                    Socket socket = agent.getAuctionSocket(auctionId2);

                    ArrayList<Object> commands = new ArrayList<>();
                    commands.add("Items");

                    synchronized (this) {
                        System.out.println("1");
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(commands);

                        System.out.println("2>");
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        Object data = in.readObject();

                        ArrayList<Item> items = (ArrayList<Item>) data;

                        System.out.println("3");
                        showItemsList(items, itemsList);
                    }

                } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                }
            }
        });

        Rectangle spacer = new Rectangle();
        spacer.setWidth(150);

        itemsBox.getChildren().addAll(refresh,spacer,getItems,itemField);
        auctionBox.getChildren().addAll(connectBox,listBox, itemsBox);
        Tab availableAuctions = new Tab("Available Auctions",auctionBox);

        /* available auction house tab above */
        tabs.getTabs().addAll(logTab,availableAuctions);

        rightColumn.getChildren().add(tabs);

        Button registerAccount = new Button("Register Account");
        registerAccount.setStyle("-fx-background-color: #499c54; -fx-text-fill: white; -fx-font-size: 15px");
        registerAccount.setOnAction(event->{
            auctionList.clear();
            if(!agent.isRegistered()) {
                Thread register = new Thread(() -> {
                    try {

                        ArrayList<Object> data = new ArrayList<>();
                        data.add("Register");
                        data.add(agent.getName());
                        data.add(1000);

                        ObjectOutputStream dataOutput = new ObjectOutputStream(socket.getOutputStream());
                        dataOutput.writeObject(data);

                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        ArrayList<Object> dataIn = (ArrayList<Object>) in.readObject();

                        int accountId = (Integer) dataIn.get(0);
                        String message1 = (String) dataIn.get(1);
                        String message2 = (String) dataIn.get(2);

                        agent.setAccountId(accountId);
                        agent.setRegistered();

                        log.appendText(message1);
                        log.appendText(message2);
                        log.appendText("---------------------------------------------------------");
                        log.appendText("-----------------------------------------------\n");
                        System.out.println("Agent account no is: " + agent.getAgentId());

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                register.start();
            }
            else{
                log.appendText("[Update]: Agent Already registered!!");
            }
        });

        Button closeAccount = new Button("Close Account");
        closeAccount.setStyle("-fx-background-color: #9c1e00; -fx-text-fill: white; -fx-font-size: 15px");
        closeAccount.setOnAction(event -> {
            if(!agent.isBidding()) {
                try {
                    ArrayList<Object> data = new ArrayList<>();
                    data.add("Close");
                    data.add(agent.getAgentId());

                    ObjectOutputStream outBank = new ObjectOutputStream(socket.getOutputStream());
                    outBank.writeObject(data);

                    agent.disconnectFromAuctions(); /* disconnects from all connected auctions */
                    Platform.exit();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                log.appendText("[Update]: (request) Agent exit ----> (declined) [Reason] Bid in progress!!\n\n");
            }
        });

        VBox topLeftBox = new VBox(10);
        topLeftBox.setAlignment(Pos.CENTER);

        HBox accButton = new HBox(20);
        accButton.setAlignment(Pos.CENTER);
        accButton.getChildren().addAll(registerAccount,closeAccount);


        /* bottom part above */

        HBox.setHgrow(rightColumn,Priority.ALWAYS);
        VBox.setVgrow(listBox,Priority.ALWAYS);
        VBox.setVgrow(bottomBox, Priority.ALWAYS);
        VBox.setVgrow(tabs,Priority.ALWAYS);
        VBox.setVgrow(log,Priority.ALWAYS);

        topBox.getChildren().addAll(space1,profile,agentName,space2,registerAccount,closeAccount);
        bottomBox.getChildren().addAll(rightColumn);
        layout.getChildren().addAll(topBox,bottomBox);

    }

    /**
     * showAuctionList : showing the auction list
     * @param Id - auction Id
     * @param auctionBook - mapping ID with the auction house
     * @param auctionBox - auctionBox
     */
    public void showAuctionList(ArrayList<Integer> Id, HashMap<Integer,String> auctionBook, TextArea auctionBox){
        auctionBox.appendText("-----------------Available Auctions----------------\n\n");
        for(Integer i: Id){
            auctionBox.appendText("Auction name: " + auctionBook.get(i) + "   " + "Auction Id: " + i + "\n\n");
            auctionBox.appendText("----------------------------------------------------\n\n");
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * showItemList is used for showing the list of the items
     * @param itemsList - items lists to be listed
     * @param area - text area where the items are listed
     */
    public void showItemsList(ArrayList<Item> itemsList, TextArea area){
        Task task = new Task() {
            @Override
            protected synchronized Object call() throws Exception {
                area.appendText("--> Item List @Auction: " + itemsList.get(0).getAuctionId() + "\n\n");
                for(Item i: itemsList){
                    String name = i.getName();
                    int itemId = i.getItemId();
                    int auctionId = i.getAuctionId();
                    int price = i.getBasePrice();
                    String description = i.getDescription();
                    int highestBid = i.getHighestBid();
                    int highestBidder = i.getHighestBidder();

                    area.appendText("Item Name: " + name + "\n");
                    area.appendText("Item Id: " + itemId + "\n") ;
                    area.appendText("Auction Id: " + auctionId + "\n");
                    area.appendText("Description: " + description + "\n");
                    area.appendText("Base Price: " + price + "$\n");
                    area.appendText("Highest Bid: " + Integer.toString(highestBid) + "\n");
                    area.appendText("Highest Bidder: " + Integer.toString(highestBidder) + "\n");
                    area.appendText("----------------------------------------------------\n\n");
                }
                return null;
            }
        };
        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     * updateMyItems is used for updating the items
     * @param agentItems - agent Items
     * @param item - items to be updated
     */
    public void updateMyItems(ComboBox<String> agentItems, String item ){
        agentItems.getItems().add(item);
    }

}
