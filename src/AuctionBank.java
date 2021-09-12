/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class AuctionBank implements Runnable{
    private AuctionHouse auctionHouse;
    private Socket auctionSocket;

    /**
     * AuctionBank
     * @param auctionHouse - auction House
     * @param socket- socket
     */
    public AuctionBank(AuctionHouse auctionHouse, Socket socket){
        this.auctionHouse = auctionHouse;
        this.auctionSocket = socket;
    }
    @Override
    public void run() {
        try { init();} catch (IOException e) {e.printStackTrace();}
    }

    @SuppressWarnings("unchecked")
    public void init() throws IOException {

        ArrayList<Object> dataOut = new ArrayList<>();
        dataOut.add("Register");
        dataOut.add(auctionHouse.getName());
        dataOut.add(0); /* initial deposit of this auction house is 0 */
        dataOut.add(auctionHouse.getPort());

        ObjectOutputStream out = new ObjectOutputStream(auctionSocket.getOutputStream());
        out.writeObject(dataOut);

        ObjectInputStream in = new ObjectInputStream(auctionSocket.getInputStream());
        ArrayList<Object> dataIn = null;

        try { dataIn = (ArrayList<Object>) in.readObject(); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }

        int accountId = (Integer) dataIn.get(0);
        auctionHouse.setAuctionId(accountId);
        auctionHouse.fillItems("res/Items.txt");
    }
}
