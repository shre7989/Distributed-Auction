/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Proxy for the bank
 */
public class BankProxy {
    public static void main(String[] args){
        String name = args[0];
        ExecutorService executorService = Executors.newFixedThreadPool(7);
        Bank bank = new Bank(name);

        try {
            int portNo = 4242;
            ServerSocket bankServer = new ServerSocket(portNo);
            System.out.println("<----------------Server Starting-------------->\n\n");
            System.out.println("[Start] Bank server started at Port: 4242");
            System.out.println("[Message] Welcome to the " + name + "\n");
            System.out.println("----------------------------------------------------------------------------");
            while(true){
                Socket connections = bankServer.accept();

                InputStreamReader input = new InputStreamReader(connections.getInputStream());
                BufferedReader inputReader = new BufferedReader(input);

                if(inputReader.readLine().equals("Agent")) {
                    Thread agent = new Thread(new BankAgentHandler(bank,connections));
                    System.out.println("[Update]: New Agent Connected!");
                    System.out.println("----------------------------------------------------------------------------");
                    executorService.execute(agent);
                }
                else{
                    Thread auction = new Thread(new BankAuctionHandler(bank,connections));
                    System.out.println("[Update]: AuctionHouse Connected!");
                    System.out.println("----------------------------------------------------------------------------");
                    executorService.execute(auction);
                }
            }
        }
        catch (IOException e) { e.printStackTrace();}

    }
}
