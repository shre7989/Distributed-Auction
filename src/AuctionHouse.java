/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Auction House
 */
public class AuctionHouse {
    private String name;
    private int auctionId;
    private int port;
    private HashMap<Integer,Item> itemBook;
    private ArrayList<Item> listOfItems;
    private HashMap<Item,Integer> biddingLogBook;

    public AuctionHouse(String name){
        this.name = name;
        this.itemBook = new HashMap<>();
        this.listOfItems = new ArrayList<>();
        this.biddingLogBook = new HashMap<>();
    }

    /**
     * validBidCheck function check if the entered bid amount is valid or not
     * @param bidAmount - bid Amount
     * @param itemId - item
     * @returns true if bidAmount is valid, else false
     */
    public boolean validBidCheck(int bidAmount, int itemId){
        Item item = itemBook.get(itemId);
        if(bidAmount > item.getBasePrice() && bidAmount > item.getHighestBid()) return true;
        else return false;
    }

    /**
     * fillItems function fills the item
     * @param path - path
     */
    public synchronized void fillItems(String path){
        try {
            FileInputStream is = new FileInputStream(path);
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(in);
            String line = reader.readLine();

            ArrayList<Integer> randomLineNumbers = getRandomLineNumbers();
            int count = 0;
            while (line != null) {
                if (randomLineNumbers.contains(count)) {
                    ArrayList<Object> data = parseLine(line);

                    String name = (String) data.get(0);
                    int price = (Integer) data.get(1);
                    int id = (Integer) data.get(2);
                    String description = (String) data.get(3);

                    Item item = new Item(name, price, id, description);
                    item.setAuctionId(this.auctionId);

                    this.listOfItems.add(item);
                    this.itemBook.put(id, item);
                }
                line = reader.readLine();
                count++;
            }
            is.close();
            in.close();
            reader.close();
        }
        catch (IOException e){}

    }

    /**
     * getRandomLineNumbers function generates random numbers for the Auction House
     * @returns random Auction House
     */
    private ArrayList<Integer> getRandomLineNumbers() {
        ArrayList<Integer> randomNumbers = new ArrayList<>();
        Random random = new Random();
        while(randomNumbers.size() < 5){
            int rand = random.nextInt(20);
            if(!randomNumbers.contains(rand)) randomNumbers.add(rand);
        }
        return randomNumbers;
    }

    /**
     * parseLine function parses the line into meaningful content for items
     * @param line - line to be parsed
     * @returns data of the Auction House
     */
    public synchronized ArrayList<Object> parseLine(String line){
        ArrayList<Object> data = new ArrayList<>();

        String[] content = line.split(" ");

        String name = content[0];
        int price = Integer.parseInt(content[1]);
        int id = Integer.parseInt(content[2]);
        String description = content[3];
        description.replaceAll("-"," ");

        data.add(name);
        data.add(price);
        data.add(id);
        data.add(description);

        return data;
    }

    /**
     * removeItem function removes the item
     * @param item- item to be removed
     */
    public synchronized void removeItem(Item item){
        this.listOfItems.remove(item);
        this.itemBook.remove(item.getItemId());
    }

    /**
     * hasItem checks if the Auction House has the item
     * @param itemId- item to be checked
     * @returns true if the item is contained, else false
     */
    public boolean hasItem(int itemId){
        if(this.itemBook.containsKey(itemId)) return true;
        else return false;
    }
    /**
     * getters and setters
     */

    public void setAuctionId(int accountNo){
        this.auctionId = accountNo;
    }

    public void setPort(int portNo){
        this.port = portNo;
    }

    public String getName(){
        return this.name;
    }

    public int getPort(){
        return this.port;
    }

    public ArrayList<Item> getListOfItems(){
        return this.listOfItems;
    }

    public HashMap<Integer,Item> getItemBook(){
        return this.itemBook;
    }
    public int getAuctionId(){return  this.auctionId;}


}
