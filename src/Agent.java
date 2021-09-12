/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Agent
 */
public class Agent{
    private final String name;
    private int agentId;
    private boolean registered;
    private int bidCount;
    private final HashMap<Integer,Integer> connectedAuctions;
    private final HashMap<Integer, Socket> auctionSockets;
    private final ArrayList<Integer> availableAuctionIds;
    private final HashMap<Integer, Item> itemsList;
    private final ArrayList<Item> myItems;
    private final ArrayList<String> myAuctions;

    public Agent(String name){
        this.name = name;
        this.availableAuctionIds = new ArrayList<>();
        this.registered = false;
        this.connectedAuctions = new HashMap<>();
        this.auctionSockets = new HashMap<>();
        this.itemsList = new HashMap<>();
        this.myItems = new ArrayList<>();
        this.myAuctions = new ArrayList<>();
        this.bidCount = 0;
    }

    /**
     * disconnectFromAuctions disconnects from the Auction House
     */
    public void disconnectFromAuctions(){
        for(Integer id: availableAuctionIds){
            Socket socket = auctionSockets.get(id);
            try {
                ArrayList<Object> data = new ArrayList<>();
                data.add("Close");
                data.add(this.getAgentId());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(data);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }



    /**
     * connectToAuction connects to the Auction House
     * @param auctionId - auction Id
     * @param portNo - connection number
     */
    public void connectToAuction(int auctionId,int portNo){
        this.connectedAuctions.put(auctionId,portNo);
        this.availableAuctionIds.add(auctionId);
    }

    /**
     * hasAuction checks for the auctionId
     * @param auctionId - auction Id
     * @returns true is contains the Id, false otherwise
     */
    public boolean hasAuction(int auctionId){
        if(availableAuctionIds.contains(auctionId)) return true;
        else return false;
    }

    /**
     * updateItemsList updates the item list
     * @param item - item
     */
    public void updateItemsList(Item item){
        this.itemsList.put(item.getItemId(),item);
        this.myItems.add(item);
    }

    /**
     * updateAuctionSockets updates the socket and auction iD
     * @param auctionId- auction id
     * @param socket - socket
     */
    public void updateAuctionSockets(int auctionId, Socket socket){
        synchronized (this) {this.auctionSockets.put(auctionId,socket);}
    }

    /**
     * isConnected checks if the connection is established
     * @param auctionId - auction Id
     * @returns true if connected, false otherwise
     */
    public boolean isConnected(int auctionId){
        return this.availableAuctionIds.contains(auctionId);
    }

    /**
     * isBidding checks for the condition
     * @returns true if greater than 0
     */
    public boolean isBidding() {
        return bidCount > 0;
    }

    /**
     * updateBidCount updates the bid count
     */
    public synchronized void updateBidCount(){
        bidCount++;
    }

    /**
     * decBidCount decreases the bidCount
     */
    public synchronized void decBidCount(){
        bidCount--;
    }

    /**
     * getters and setters
     */

    public Socket getAuctionSocket(int auctionId){
        return this.auctionSockets.get(auctionId);
    }

    public HashMap<Integer,Integer> getConnectedAuctions(){
        return this.connectedAuctions;
    }

    public void setRegistered(){
        this.registered = true;
    }

    public boolean isRegistered(){
        return this.registered;
    }

    public String getName(){
        return this.name;
    }

    public void setAccountId(int accountId) {
        this.agentId = accountId;
    }

    public int getAgentId() {
        return this.agentId;
    }

}
