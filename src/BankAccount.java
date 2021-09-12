/**
 * @author - Mausam Shrestha
 *
 */package auction;


/**
 * BankAccount setup
 */
public class BankAccount {
    private Bank bank; /* associated bank */
    private String accountName; /* name of the account */
    private int accountId; // not sure if needs implementation
    private int totalFunds; /* total funds in the account*/
    private int hold; /* funds on hold due to ongoing transaction */
    private int availableFunds; /* funds free and available immediately */

    public BankAccount(String accountName, int initialDeposit, int accountId, Bank associatedBank){
        this.accountName = accountName;
        this.totalFunds = initialDeposit;
        this.accountId = accountId;
        this.availableFunds = initialDeposit;
        this.bank = associatedBank;
        this.hold = 0;
    }

    /**
     * updateAvailableFunds : updating available funds
     * @param amount - amount to be updated
     * @returns true if amount is updated, false otherwise
     */
    public synchronized boolean updateAvailableFunds (int amount){
        if(amount < availableFunds){
            availableFunds = availableFunds - amount;
            hold = hold + amount;
            return true;
        }
        else return false;
    }

    /**
     * releaseFunds : releasing the funds
     * @param amount - amount to be released
     */
    public synchronized void releaseFunds(int amount){
        this.hold = hold - amount; /* release hold amount */
        this.availableFunds = availableFunds + amount;
        this.totalFunds = totalFunds - amount;
    }

    /**
     * addFunds : adding funds to the account
     * @param amount - amount to be added
     */
    public synchronized void addFunds(int amount){
        this.availableFunds = availableFunds + amount;
        this.totalFunds = totalFunds + amount;
    }

    /**
     * transferFunds: transferring funds
     * @param amount - amount to be transferred
     */
    public synchronized void transferFunds(int amount){
        this.hold = hold - amount;
        this.availableFunds = availableFunds - amount;
        this.totalFunds = totalFunds - amount;
    }

    public int getTotalFunds(){
        return this.totalFunds;
    }

}
