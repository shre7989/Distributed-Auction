/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Proxy for the auction house
 */
public class AuctionHouseProxy {
    public static void main(String[] args){
        String name = args[0];
        AuctionHouse auctionHouse = new AuctionHouse(name);

        try {

            /* Server side of AuctionHouse */
            ServerSocket auctionServerSocket = new ServerSocket(0); /* start listening on a random port */

            auctionHouse.setPort(auctionServerSocket.getLocalPort());

            AuctionServer auctionServer = new AuctionServer(auctionHouse,auctionServerSocket);
            Thread auctionServerThread = new Thread(auctionServer);
            auctionServerThread.start();

            /* Client side of AuctionHouse */
            Socket auctionClientSocket = new Socket("localhost",4242);

            PrintWriter writer = new PrintWriter(auctionClientSocket.getOutputStream(),true);
            writer.println("Auction");

            Thread auctionRequestThread = new Thread(new AuctionBank(auctionHouse,auctionClientSocket));
            auctionRequestThread.start();

            auctionServer.setClientSocket(auctionClientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
