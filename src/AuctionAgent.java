/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
/**
 * Auction Agent
 */
public class AuctionAgent implements Runnable{
    private final AuctionHouse auctionHouse;
    private final Socket agent;
    private final Socket auctionClientSocket;
    private boolean connection;

    public AuctionAgent(AuctionHouse auction, Socket agent, Socket auctionClient){
        this.auctionHouse = auction;
        this.agent = agent;
        this.auctionClientSocket = auctionClient;
        this.connection = true;
    }

    @Override
    public void run() {
        try {
            while(connection) {
                ArrayList<Object> data;

                ObjectInputStream in = new ObjectInputStream(agent.getInputStream());

                data = (ArrayList<Object>) in.readObject();
                String command = (String) data.get(0);

                switch (command) {
                    case "Items":
                        sendItemsList(auctionHouse, agent);
                    break;

                    case "Bid":
                        placeBid(data, auctionClientSocket, auctionHouse, agent);
                    break;

                    case "Close":
                        int agentId = (Integer) data.get(1);
                        disconnectAgent(agent);
                        System.out.println("[Update]: --> Agent: " + agentId + "(Disconnected)!");
                        System.out.print("-------------------------------");
                        System.out.println("---------------------------------------------");
                        this.connection = false;
                    break;
                }
            }
        } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

    /**
     * disconnectAgent disconnects from the port
     * @param agent - agent to be disconnected
     */
    private void disconnectAgent(Socket agent) {
        try {
            agent.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * sendItemList sends the list of items to the auction house
     * @param auctionHouse - auction House
     * @param client - client
     */
    private void sendItemsList(AuctionHouse auctionHouse, Socket client){
        ObjectOutputStream out;
        try {
            System.out.println("2");
            out = new ObjectOutputStream(client.getOutputStream());
            synchronized (this) {out.writeObject(auctionHouse.getListOfItems());}
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * placeBid helps for placing bets
     * @param inData - data required for the bid
     * @param auctionClientSocket - client socket for auction
     * @param auctionHouse - auction House
     * @param client - client
     */
    private void placeBid(ArrayList<Object> inData, Socket auctionClientSocket, AuctionHouse auctionHouse, Socket client){
        /* casting the incoming input to appropriate types */
        int amount = (Integer) inData.get(1);
        int itemId = (Integer) inData.get(2);
        int agentId = (Integer) inData.get(3);
        String agentName = (String) inData.get(4);

        if(auctionHouse.hasItem(itemId)){ /* checks if auciton has the item */
            try {
                inData.clear();
                inData.add("Valid");
                inData.add("[Auction House]: (valid)--> bid initiated by Agent: " + agentId + " | For item: " + itemId + "\n\n");
                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                out.writeObject(inData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(auctionHouse.validBidCheck(amount,itemId)){
                ArrayList<Object> data = new ArrayList<>();
                data.add("Bid");
                data.add(amount);
                data.add(itemId);
                data.add(agentId);
                data.add(auctionHouse.getAuctionId());

                try {
                    ObjectOutputStream outAuction = new ObjectOutputStream(auctionClientSocket.getOutputStream());
                    outAuction.writeObject(data);

                    ObjectInputStream inAuction = new ObjectInputStream(auctionClientSocket.getInputStream());
                    ArrayList<Object> dataIn = (ArrayList<Object>) inAuction.readObject();
                    boolean result = (boolean) dataIn.get(0);

                    System.out.println("S");
                    if(result){ /* bid for item was accepted */
                        ObjectOutputStream agentOut = new ObjectOutputStream(client.getOutputStream());
                        /* status update for agent that their bid was accepted */
                        data.clear(); /* reuse */
                        data.add("Accepted");
                        data.add("[Auction House]: Bid----> status (accepted) | Item: " + itemId + "\n");
                        agentOut.writeObject(data);

                        synchronized (this) {
                            Item item = auctionHouse.getItemBook().get(itemId);
                            item.setHighestBid(amount);
                            item.setHighestBidder(agentId);
                            System.out.println("(Bid held) ----------------> Count down 10");
                            System.out.println("[Update]: Highest Bid: " + amount + " Highest Bidder: " + agentId);
                        }

                        boolean countDown = true;
                        double startTime = System.currentTimeMillis();

                        /* bid is held for 7 seconds at most */
                        while(countDown){
                            double currTime = System.currentTimeMillis();
                            if(currTime - startTime >= 7000) countDown = false;
                        }

                        synchronized (this) {
                            Item item = auctionHouse.getItemBook().get(itemId);
                            data.clear();
                            System.out.println("[Update]: Highest Bid: " + amount + " HIghest Bidder: " + agentId);
                            System.out.println("----------------------------------------------------------------------------");
                            if (item.getHighestBidder() == agentId) {
                                auctionHouse.removeItem(item);
                                data.add("Winner");
                                data.add(item);
                                data.add("[Auction House]: Bid-->status (winner)| Item: " + itemId + "| Amount: " + amount + "\n\n");
                                try {
                                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                                    oos.writeObject(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                data.add("Release");
                                data.add(amount);
                                data.add(agentId);
                                ObjectOutputStream oab = new ObjectOutputStream(auctionClientSocket.getOutputStream());
                                oab.writeObject(data);

                                data.add("[Auction House]: Bid-->status (outbid)| Item: " + itemId + "\n");
                                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                                oos.writeObject(data);
                            }
                        }

                    }
                    else{
                        data.clear();
                        data.add("Rejected");
                        data.add("[Auction House]: Bid status--> (rejected) due to insufficient funds!");
                        try {
                            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                            out.writeObject(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }

            }
            else{
                ArrayList<Object> data = new ArrayList<>();
                data.add("Rejected");
                data.add("[Auction House]: Bid status--> (invalid) enter a greater amount!\n\n");
                try {
                    ObjectOutputStream out = new ObjectOutputStream(agent.getOutputStream());
                    out.writeObject(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            inData.clear();
            try {
                inData.add("[Auction House]: Bid status--> (invalid) Item not found!!");
                ObjectOutputStream out = new ObjectOutputStream(agent.getOutputStream());
                out.writeObject(inData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
