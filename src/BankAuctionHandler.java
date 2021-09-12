/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * BankAuctionHandler - handles bank auction
 */
public class BankAuctionHandler implements Runnable{
    private Bank bank;
    private Socket auction;
    private boolean connection;
    private int connectedAuctionId;

    public BankAuctionHandler(Bank bank, Socket socket){
        this.bank = bank;
        this.auction = socket;
        this.connection = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            int count = 0;
            while(connection){
                ObjectInputStream in = new ObjectInputStream(auction.getInputStream());

                ArrayList<Object> data = (ArrayList<Object>) in.readObject();

                String command = (String) data.get(0);
                if(command.equals("Register") && count == 0){
                    count++;
                    connectedAuctionId = registerAccount(data,bank,auction);
                }
                else if(command.equals("Bid")){
                    requestHoldFunds(data,bank,auction);
                }
                else if(command.equals("Release")){
                    releaseFunds(data,bank);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            disconnectAuction(connectedAuctionId, bank); /* disconnect auction from the bank */
            this.connection = false;
            //e.printStackTrace();
        }
    }

    /**
     * releaseFunds is used for releasing the funds from the account
     * @param data - takes the data to be released
     * @param bank - bank
     */
    private void releaseFunds(ArrayList<Object> data, Bank bank) {
        int amount = (Integer) data.get(1);
        int agentId = (Integer) data.get(2);

        bank.releaseFunds(amount,agentId); /* release the funds */
    }

    /**
     * disconnectAuction for disconnecting from the Auction HOuse
     * @param auctionId - audtion ID
     * @param bank - bank
     */
    private void disconnectAuction(int auctionId, Bank bank) {
        bank.getAccounts().remove(auctionId);
        bank.getBankAccountIds().remove(auctionId);
        bank.getAuctionBook().remove(auctionId);
        bank.getAuctionHouses().remove(auctionId);

        System.out.println("[Update]: (remove) -->Account of Auction House: " + auctionId);
        System.out.println("[Update]: Total Accounts: " + this.bank.getAccounts().size());
        System.out.println("----------------------------------------------------------------------------");
    }

    /**
     * registerAccount for registering the account
     * @param data - data
     * @param bank - bank
     * @param socket - socket
     * @returns accountId
     */
    public int registerAccount(ArrayList<Object> data, Bank bank, Socket socket){
        try {

            String name = (String) data.get(1);
            int deposit =  (Integer) data.get(2);
            int port = (Integer) data.get(3);
            int accountId = bank.getRandomId(bank.getBankAccountIds());

            synchronized (this) {
                bank.registerAccount(accountId,new BankAccount(name,deposit,accountId,bank)); /* create bank account */
                bank.registerAuctionHouse(accountId,port); /* record port and id of auction house */

                bank.getIds().add(accountId); /* bank records id of each auction house */
                bank.getAuctionBook().put(accountId,name);

                ArrayList<Object> dataOut = new ArrayList<>();
                dataOut.add(accountId);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(dataOut);

            }
            System.out.println("[Bank]: Auction account Registered---> Acc: " + accountId + "| Balance: 0$");
            System.out.println("[Bank]: Total Accounts: " + this.bank.getAccounts().size());
            System.out.println("----------------------------------------------------------------------------");
            return accountId;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * requestHoldFunds requests the specified funds to be holded in the account
     * @param data - data
     * @param bank - bank
     * @param auction - auction
     */
    public void requestHoldFunds(ArrayList<Object> data, Bank bank, Socket auction){
        int amount = (Integer) data.get(1);
        int itemId = (Integer) data.get(2);
        int agentId = (Integer) data.get(3);
        int auctionId = (Integer) data.get(4);

        System.out.println("\n[Update] Hold Funds requested by--> Auction: " + auctionId + " amount: " + amount);
        System.out.println("[Reason] Bid initiated by Agent: " + agentId + " for item: " + itemId);
        System.out.println("----------------------------------------------------------------------------");

        boolean result = bank.holdFunds(amount,agentId);
        ArrayList<Object> dataOut = new ArrayList<>();

        if(result) {
            try {
                dataOut.add(true);
                ObjectOutputStream out = new ObjectOutputStream(auction.getOutputStream());
                out.writeObject(dataOut);
            } catch (IOException e) { e.printStackTrace(); }
        }
        else{
            try {
                dataOut.add(false);
                ObjectOutputStream out = new ObjectOutputStream(auction.getOutputStream());
                out.writeObject(dataOut);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
