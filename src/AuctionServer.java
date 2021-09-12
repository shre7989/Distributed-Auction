/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class AuctionServer implements Runnable{
    private AuctionHouse auctionHouse;
    private ServerSocket serverSocket;
    private Socket auctionClientSocket;

    /**
     * AucionServer: server for auction
     * @param auctionHouse - auction house
     * @param serverSocket - socket for sever
     */
    public AuctionServer(AuctionHouse auctionHouse, ServerSocket serverSocket){
        this.auctionHouse = auctionHouse;
        this.serverSocket = serverSocket;
    }
    @Override
    public void run() {
        while (true){
            try {
                Socket client = serverSocket.accept();
                System.out.println("Client Connected--> Port no: " + auctionHouse.getPort());

                Thread agentHandler = new Thread(new AuctionAgent(auctionHouse,client,auctionClientSocket));
                agentHandler.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setClientSocket(Socket clientSocket){
        this.auctionClientSocket = clientSocket;
    }


}
