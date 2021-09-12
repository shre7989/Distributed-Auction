/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


/**
 * BankHandler for the Bank
 */
public class BankAgentHandler implements Runnable{
    private Bank bank;
    private Socket agent;
    private boolean connection;

    public BankAgentHandler(Bank bank, Socket socket){
        this.bank = bank;
        this.agent = socket;
        this.connection = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            while(connection){
                ObjectInputStream in = new ObjectInputStream(agent.getInputStream());
                ArrayList<Object> data = (ArrayList<Object>) in.readObject();

                String command = (String) data.get(0);
                if(command.equals("Register")) {
                    String agentName = (String) data.get(1);
                    int deposit = (Integer) data.get(2);
                    registerAccount(agentName,deposit,bank,agent); /* register an account with the bank */
                }
                else if(command.equals("Auction List")){
                    sendAuctionList(bank,agent); /* send auction list from bank to agent */
                }
                else if(command.equals("Connect")){
                    int auctionId = (Integer) data.get(1);
                    connectToAuction(auctionId,bank,agent); /* send the agent the port number of the requested auction */
                }
                else if(command.equals("Transfer")){
                    int amount = (Integer) data.get(1);
                    int agentId = (Integer) data.get(2);
                    int auctionId = (Integer) data.get(3);
                    transferFunds(amount, agentId, auctionId, bank, agent); /* transfer funds */
                }
                else if(command.equals("Close")){
                    int agentId = (Integer) data.get(1);
                    disconnectAgent(agent, agentId, bank);
                    System.out.println("[Update]: --> Agent: " + agentId + "(Disconnected)!");
                    connection = false;
                }
                else if(command.equals("Funds")){
                    int agentId = (Integer) data.get(1);
                    int funds = bank.getTotalFunds(agentId);

                    data.clear();
                    data.add(funds);

                    ObjectOutputStream oos = new ObjectOutputStream(agent.getOutputStream());
                    oos.writeObject(data);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * registerAccount registers the account
     * @param agentName - name of the agent
     * @param deposit - amount to be deposited
     * @param bank - bank
     * @param socket - socket
     */
    public void registerAccount(String agentName, int deposit, Bank bank, Socket socket){

        int accountId = bank.getRandomId(bank.getBankAccountIds());
        bank.getBankAccountIds().remove(accountId);

        synchronized (this) {
            bank.registerAccount(accountId,new BankAccount(agentName, deposit, accountId, bank));
        }

        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(accountId);
            data.add("[Bank]: Thank you for registering an account wth the Federal Bank of Albuquerque!\n");
            data.add("[Bank] Your account no is: " + accountId + "\n");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(data);
            System.out.println("[Bank]: Agent Account Registered--> " + "Acc: " + accountId + "| Balance: " + deposit + "$");
            System.out.println("[Bank]: Total Accounts: " + this.bank.getAccounts().size());
            System.out.println("----------------------------------------------------------------------------");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * sendAuctionList sends the Auction list
     * @param bank - bank
     * @param socket - socket
     * @throws IOException
     */
    public void sendAuctionList(Bank bank, Socket socket) throws IOException {
        ArrayList<Object> data = new ArrayList<>();
        data.add(bank.getIds());
        data.add(bank.getAuctionBook());

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        synchronized (this) { out.writeObject(data);}
    }

    /**
     * connectToAuction established connection to the port
     * @param auctionId - id for the auction
     * @param bank - bank
     * @param socket - socket
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void connectToAuction(int auctionId, Bank bank, Socket socket) throws IOException, ClassNotFoundException {
        int auctionPort = 0;
        if(bank.getAuctionHouses().get(auctionId) != null) {
            auctionPort = bank.getAuctionHouses().get(auctionId);
        }

        synchronized (this) {
            ArrayList<Object> data = new ArrayList<>();
            data.add(auctionPort);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(data);

        }
    }

    /**
     * transferFunds is used for transferring the funds
     * @param amount - amount to be transferred
     * @param agentId - agentId
     * @param auctionId - auctionId
     * @param bank - bank
     * @param agentClient - client for the agent
     */
    private void transferFunds(int amount, int agentId, int auctionId, Bank bank, Socket agentClient) {
        System.out.println("[Update]: Agent: " + agentId + " initiated transfer of $" + amount + "to Auction: " + auctionId + "\n");
        System.out.println("----------------------------------------------------------------------------");
        BankAccount agent = bank.getAccounts().get(agentId);
        BankAccount auction = bank.getAccounts().get(auctionId);
        agent.transferFunds(amount); /* take out money */
        auction.addFunds(amount); /* transfer it to the auctions's account */

        try {
            String message1 = "[Bank]: (Transfer) request accepted!!\n";
            String message2 = "[Bank]: -->Transfer funds initiated-->AgentId: " + agentId + "--> $" + amount + " --->" + "Auction: " + auctionId + "\n\n";
            ArrayList<Object> data = new ArrayList<>();
            data.add(message1);
            data.add(message2);
            ObjectOutputStream out = new ObjectOutputStream(agentClient.getOutputStream());
            out.writeObject(data);

        } catch (IOException e) { e.printStackTrace(); }


    }

    /**
     * disconnectAgent for disconnecting from the Agent
     * @param agent - agent to be disconnected
     * @param agentId - agentId
     * @param bank - bank
     */
    private void disconnectAgent(Socket agent, int agentId, Bank bank){
        try {
            agent.close();
            System.out.println("[Update]: (remove) -->account of Agent: " + agentId);
            System.out.println("----------------------------------------------------------------------------");
            bank.getAccounts().remove(agentId);
            bank.getBankAccountIds().remove(agentId);

        } catch (IOException e) { e.printStackTrace(); }
    }

}
