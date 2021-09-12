/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Bank is a server
 */
public class Bank{
    public String name; /* name of the bank */
    public HashMap<Integer, BankAccount> accounts; /* accounts in the bank */
    public HashMap<Integer,Integer> auctionHouses; /* available auction houses and their addresses */
    public ArrayList<Integer> ids;
    public HashMap<Integer,String> auctionBook;
    public ArrayList<Integer> bankAccountIds;

    public Bank(String bankName){
        this.name = bankName;
        this.accounts = new HashMap<>();
        this.bankAccountIds = new ArrayList<>();
        this.auctionHouses = new HashMap<>();
        this.ids = new ArrayList<>();
        this.auctionBook = new HashMap<>();
        generateIds(bankAccountIds);
    }

    /**
     * holdFunds function holds the funds for the account
     * @param amount - amount in the account
     * @param accountId - account of a agent
     * @returns true if amount is holded, false otherwise
     */
    public boolean holdFunds(int amount, int accountId){
        BankAccount account = this.getAccounts().get(accountId);
        boolean result = account.updateAvailableFunds(amount);
        return result;
    }
    public synchronized void generateIds(ArrayList<Integer> Ids){
        for(int i = 100000; i <= 200000; i++){
            Ids.add(i);
        }
    }

    /**
     * getRandomId function generates the randomId for the Auction House
     * @param Ids - generated ID
     * @returns Id
     */
    public synchronized int getRandomId(ArrayList<Integer> Ids){
        Random random = new Random();
        int Id = random.nextInt(Ids.size());
        Ids.remove(Id);
        return Id;
    }

    /**
     * regsiterAccount function registers the account
     * @param accountId - account number
     * @param account - Bank account associated with the accountID
     */
    public void registerAccount(int accountId,BankAccount account){
        synchronized (this) {this.accounts.put(accountId,account);}
    }


    /**
     * registerAuctionHouse function registers the auction house with the bak
     * @param accountId - Id of the auction House
     * @param PortNumber - listening port of the auction House
     */
    public void registerAuctionHouse(int accountId, int PortNumber){
        this.auctionHouses.put(accountId,PortNumber);
    }

    /**
     * releaseFunds function releases the amount associated with the agentID
     * @param amount - amount to be released
     * @param agentId - agent
     */
    public void releaseFunds(int amount, int agentId) {
        BankAccount account = this.getAccounts().get(agentId);
        account.releaseFunds(amount);
    }

    /**
     * getTotalFunds function updates the funds for the agent
     * @param agentId - agent
     * @returns totalFunds
     */
    public int getTotalFunds(int agentId) {
        BankAccount account = this.getAccounts().get(agentId);
        int totalFunds = account.getTotalFunds();
        return totalFunds;
    }

    /**
     * getters and setters
     */

    public HashMap<Integer, BankAccount> getAccounts(){
        return this.accounts;
    }

    public ArrayList<Integer> getBankAccountIds(){
        return this.bankAccountIds;
    }

    public HashMap<Integer,Integer> getAuctionHouses(){
        return this.auctionHouses;
    }

    public ArrayList<Integer> getIds(){
        return this.ids;
    }

    public HashMap<Integer,String> getAuctionBook(){
        return this.auctionBook;
    }



}
