/**
 * @author - Mausam Shrestha
 *
 */
package auction;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String description;
    private int basePrice;
    private int itemId;
    private int auctionId;
    private int highestBid;
    private int highestBidder;
    private static final long serialVersionUID = 123456789;

    /**
     * Item - constructor for out Item object
     * @param name - name of the item
     * @param price - base price of the item
     * @param itemId - unique id of the item
     */
    public Item(String name, int price, int itemId, String description){
        this.name = name;
        this.basePrice = price;
        this.itemId = itemId;
        this.highestBid = basePrice;
        this.highestBidder = 0;
        this.description = description;
    }

    /**
     * getters and setters
     */
    public void setAuctionId(int auctionId){
        this.auctionId = auctionId;
    }

    public String getName(){
        return this.name;
    }

    public int getBasePrice(){
        return this.basePrice;
    }

    public int getItemId(){
        return this.itemId;
    }

    public int getAuctionId(){
        return this.auctionId;
    }

    public void setHighestBid(int bid){
        if(bid > highestBid) {
            this.highestBid = bid;
        }
    }

    public void setHighestBidder(int agentId){
        this.highestBidder = agentId;
    }

    public int getHighestBid(){
        return this.highestBid;
    }

    public int getHighestBidder(){
        return this.highestBidder;
    }

    public String getDescription(){
        return this.description;
    }

}
