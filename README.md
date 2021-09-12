**Walkthrough**

**Introduction:**
This Project simulates a system of multiple Auction houses selling multiple items, multiple Agents who bid on these items and a Bank to keep track of the involved transactions. It consists of three main parts which are:

    - **Bank:** The bank is static and at a known address. You’ll start this program before either
            agents or auction houses. (The bank is a server and the agents and auction houses  
            are its clients.)Both agents and auction houses will have bank accounts. When an 
            agent bids on or or is outbid in an auction, the bank will block or unblock the  
            appropriate amount of funds, at the request of the auction house. When an agent wins 
            an auction, the bank will transfer these blocked funds from the agent to the auction 
            house account, at the request of the agent. Auction houses provide the bank with 
            their host and port information. The bank provides the agents with the list of the 
            auction houses and their addresses so the agents will be able to connect directly to 
            the auction houses. 

    - **Auction House:** Each auction house is dynamically created. Upon creation, it registers 
                     with the bank, opening an account with zero balance. It also provides the 
                     bank with its host and port address1, so the bank can inform the agents of 
                     the existence of this auction house. (An auction house is a client of the 
                     bank, but also is a server with agents as its clients.)It hosts a list of 
                     items being auctioned and tracks the current bidding status of each item. 
                     Initially, the auction house will offer at least 3 items for sale.2 As the 
                     items are sold, new items will be listed to replace them. (The items for 
                     sale may be scripted, read in from a configuration file, programmatically 
                     generated, etc.)Upon request, it shares the list of items being auctioned
                     and the bidding status with agents, including for each item house id, item 
                     id, description, minimum bid and current bid. The user may terminate the 
                     program when no bidding activity is in progress. The program should not 
                     allow exit when there are still bids to be resolved. At termination, it 
                     de-registers with the bank. 
    - **Agent:** Each agent is dynamically created. Upon creation, it opens a bank account by
                 providing a name and an initial balance, and receives a unique account number.
                 (The agent is a client of both the bank and the auction houses.)The agent gets 
                 a list of active auction houses from the bank. In connects to an auction house 
                 using the host and port information sent from the bank. The agent receives a 
                 list of items being auctioned from the auction house. When an agent makes a bid 
                 on an item, it receives back one or more status messages as the auction 
                 proceeds:
                    • acceptance – The bid is the current high bid
                    • rejection – The bid was invalid, too low, insufficient funds in the bank,
                    etc.
                    • outbid – Some other agent has placed a higher bid
                    • winner – The auction is over and this agent has won.
                 The agent notifies the bank to transfer the blocked funds to the auction house 
                 after it wins a bid. The program may terminate when no bidding activity is in 
                 progress. The program does not allow exit when there are still bids to be
                 resolved. It deregisters with the bank upon termination.

**Tutorial:**
         - If you are using the .jar files to simulate Auction Houses, place the resource folder 
         where the jar files are. The items.txt file for the Auction Houses are in the resource 
         folder. Should you choose to edit the items file, please do so in the following manner ]
         to maintain the integrity of the file.
              1) Each line represents property description for an item, and is in the following 
                 format: (Item name) (Base price) (item id) (description)
              2) Item ids for the items should be unique and should not allow any duplicates.
        - Agent.jar is the only jar file which uses javafx GUI. Bank and AuctionHouses live 
          updates for updates in the simulation in the command line.
        - Every Agent must register an account with the bank to utilize its services. This can 
          be done by pressing the Register account button in the top right corner.
        - There are two tabs in the Agent GUI 

          1) **log tab:** This tab contains a log that prints out all the updates and transactions 
          involved with the agent. It also contains a Combobox for items owned by the Agent and 
          another Combobox for Ids of Auctionhouses the Agent is connected to. Along with this, 
          just below the log area, there is a setup of text fields and a bid button to help the 
          agent to make a bid for a particular item in a particular auction with the specified 
          amount. 
          **To make a bid : **
             - Enter the amount you want to bid in the amount Field. 
             - Enter the Item id for the item you want to bid for. Enter the Auction Id for the 
               Auction hosting this item. 
             - Click the Bid button to make a bid. 
          Your bid will be held for 10 seconds if it is not valid, After the 10 seconds, if no 
          other Agent outbids you for that item, you will pay for the item through the bank to t
          he hosting Auction house and the item will be added to your inventory. 
          **Invalid Bid Cases:**
             - If the amount entered is less than the current highest bid.
             - If the amount entered is greater than available balance in the account.
             - If the item id does not belong to any item in the Auction house.
             - If the Auction Id entered is Invalid.
          **Exit case:** If you want to exit from the GUI, exit by pressing the close account button 
          at the top right of the window. Agent cannot exit if it has any bidding activity in 
          progress.

          2) **AuctionHouses tab:** This tab contains additional functionalities for the Agents. The 
          agents can request the available Auction houses registered with the bank. To utilize 
          this, the Agent needs to be registered with the bank. Agents can also connect to an 
          Auction house given its auction Id by entering the AuctionId in the field provided in 
          the top of the tab, and then clicking the connect button. A short info about the port 
          where the Agent connects with the Auction house is also displayed.Agents can also get 
          the list of items along with its information in the Auction house given its auction id 
          by entering the Auction id in the Auction id field which is in the left bottom and 
          then clicking the get items button. 
