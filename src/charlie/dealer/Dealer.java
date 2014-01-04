/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package charlie.dealer;

import charlie.card.Hand;
import charlie.card.Shoe;
import charlie.actor.House;
import charlie.actor.RealPlayer;
import charlie.card.Card;
import charlie.card.HoleCard;
import charlie.card.Hid;
import charlie.card.ShoeFactory;
import charlie.util.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Blackjack dealer.
 * It uses the following rules:<br>
 * <ol>
 * <li>Dealer stands on 17, hard or soft.
 * <li>Blackjack pays 3:2. For discussion of house advantage of different
 * pay outs see http://answers.yahoo.com/question/index?qid=20080617174652AAOBfaj
 * <li>Five card Charlie pays 2:1.
 * </ol>
 * @author Ron Coleman
 */
public class Dealer implements Serializable { 
    public final static Double BLACKJACK_PAYS = 3/2.;
    public final static Double CHARLIE_PAYS = 2/1.;
    protected final static Double PROFIT = 1.0;
    protected final static Double LOSS = -1.0;    
    private final Logger LOG = LoggerFactory.getLogger(Dealer.class);
    protected Shoe shoe;
    protected HashMap<Hid,Hand> hands = new HashMap<>();
    protected HashMap<Hid,IPlayer> players = new HashMap<>();
    protected List<Hid> handSequence = new ArrayList<>();
    protected List<IPlayer> playerSequence = new ArrayList<>();
    protected final House house;
    protected Integer handSeqIndex = 0;
    protected IPlayer active = null;
    protected Hand dealerHand;
    private HoleCard holeCard;
    
    /**
     * Constructor
     * @param house House actor which launched us.
     */
    public Dealer(House house) {
        this.house = house;

        // Instantiate the shoe
        Properties props = house.getProps();
        
        String scenario = props.getProperty("charlie.shoe", "charlie.card.Shoe");
        LOG.info("using scenario = '"+scenario+"'");
        
        shoe = ShoeFactory.getInstance(scenario);
        
        shoe.init();
        
        LOG.info("shoe = "+shoe);
    }
    
    /**
     * Receives a bet request from a "real" you. Don't invoke this method
     * for a bot. Bots are instantiated directly by this class.
     * @param you Player
     * @param yours Hand
     * @param bet Bet amount
     */
    public void bet(RealPlayer you,Hid yours) {
        LOG.info("got new bet = "+yours.getAmt()+" from "+you+" for hid = "+yours);
        
        // Clear out old hands, if any
        reset();

        // Insert the player -- IN THIS ORDER
        // B9 Robby
        sit("B9",Seat.RIGHT);
        
        sit(you,yours);
        
        // AAF709 / Rhoda
        sit("B9",Seat.LEFT);
      
        handSeqIndex = 0;        

        // Create the dealer hand
        dealerHand = new Hand(new Hid(Seat.DEALER));
        
        // Shuffle cards
        shuffle();
        
        // Let the game begin!
        startGame();
    }
        
    /**
     * Inserts a player at the table.
     * @param you You player
     * @param yours Your hand id
     */
    protected void sit(IPlayer you,Hid yours) {
        handSequence.add(yours);
        playerSequence.add(you); 
        players.put(yours, you);
        hands.put(yours, new Hand(yours));        
    }
    
    /**
     * Spawns a full-fledged bot. 
     * @param name Bot name
     * @param seat Bot seat at table
     * @return A bot
     */
    protected IBot sit(String name, Seat seat) {
        if(seat != Seat.LEFT && seat != Seat.RIGHT) {
            LOG.error("can't seat bot at seat = "+seat);
            return null;
        }
        
        String name_ = name.toLowerCase();
        String className = house.getProps().getProperty("charlie.bot." + name_);
        if (className == null) {
            LOG.info("no bot configured for charlie.bot."+name_);
            return null;
        }
        
        LOG.info("attempting to spawn bot "+name_+" class = "+className);

        Class<?> clazz;
        try {
            clazz = Class.forName(className);

            IBot bot = (IBot) clazz.newInstance();
            
            bot.sit(seat);
            
            bot.setDealer(this);
            
            Hand hand = bot.getHand();
            Hid hid = hand.getHid();

            handSequence.add(hid);
            
            playerSequence.add(bot);
            
            players.put(hid, bot);
            
            hands.put(hid, hand);
           
            LOG.info("successfully spawned bot = "+name_);
            return bot;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.error("caught exception: " + ex);
        }

        return null;
    }
    
    /**
     * Resets the state of the game.
     */
    protected void reset() {
        handSequence.clear();
        playerSequence.clear();
        hands.clear();        
    }
    
    /**
     * Shuffles the shoe, if necessary.
     */
    protected void shuffle() {
        if (shoe.shuffleNeeded()) {
            shoe.shuffle();

            for (IPlayer player : playerSequence) {
                player.shuffling();
            }
        }      
    }
    
    /**
     * Starts the game.
     */
    protected void startGame() {
        LOG.info("starting a game");
        try {
            // Gather up all the initial hands (ie, not including splits)
            List<Hid> hids = new ArrayList<>();
            
            for(Hid hid: handSequence) {
                hids.add(hid);
            }
            
            // Include the dealer's hand
            hids.add(dealerHand.getHid());
          
            LOG.info("hands at table + dealer = "+hids.size()+1);
            
            // Tell each player we're starting a game
            for(IPlayer player: playerSequence)              
                player.startGame(hids,shoe.size());
            
            Thread.sleep(250);
            
            // First round card to every one
            round(hids);
            
            holeCard = new HoleCard(shoe.next());
            dealerHand.hit(holeCard);  
            
            Thread.sleep(Constant.DEAL_DELAY);
            
            for(IPlayer player: playerSequence) {
                // Don't send hole card yet to bots -- they could see it
                if(!(player instanceof IBot))
                    player.deal(dealerHand.getHid(), holeCard, dealerHand.getValues());
            }

            // Second round card to everyone
            round(hids);
            
            Card upCard = shoe.next();
            dealerHand.hit(upCard);
            
            Thread.sleep(Constant.DEAL_DELAY);
            
            for(IPlayer player: playerSequence)
                player.deal(dealerHand.getHid(), upCard, dealerHand.getValues()); 
            
            // Revalue the dealer's hand since hit doesn't vale hole card
            dealerHand.revalue();
            
            // CHeck if players want to buy insurance
            if(upCard.isAce())
                insure();
            
            if(dealerHand.isBlackjack()) {
                closeGame();
            }
            else
                goNextHand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insures against dealer Blackjack
     */
    protected void insure() {
        // TODO
    }
    
    /**
     * Deals a round of cards to everyone.
     * @param hids Hand ids
     * @param dealerCard The dealer card, up or hole.
     */
    protected void round(List<Hid> hids) {
        try {
            for(Hid hid: hids) {
                IPlayer player = players.get(hid);
                
                // If there's no correspondsing player, must be dealer's hid_
                if(player == null)
                    continue;
               
                // Get a card from the shoe
                Card card = shoe.next();
                
                // Deal this card
                LOG.info("dealing to "+player+" card 1 = "+card); 
                
                // Save it to dealer's copy of hand
                Hand hand = this.hands.get(hid);
                
                hand.hit(card);
                                                
                Thread.sleep(Constant.DEAL_DELAY);
                
                // Distribute the hard to everyone, even if it's not theirs
                for (IPlayer _player : playerSequence)
                    _player.deal(hid, card, hand.getValues());
                
                // If player has blackjack -- they win automatically!
                if (hand.isBlackjack()) {
                    hid.multiplyAmt(BLACKJACK_PAYS);
                    
                    house.updateBankroll(players.get(hid), hid.getAmt(), PROFIT);

                    for (IPlayer player_ : playerSequence) {
                        player_.blackjack(hid);
                    }
                }
            }            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hits player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid_ Player's hand id
     */
    public void hit(IPlayer player,Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalid HIT player = "+player);
            return;
        }
        
        // Deal a card
        Card card = shoe.next();
        
        hand.hit(card);
        for (IPlayer _player : playerSequence)
            _player.deal(hid, card, hand.getValues());
        
        // If the hand isBroke, we're done with this hand
        if(hand.isBroke()) {
            house.updateBankroll(player,hid.getAmt(),LOSS);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
            
            goNextHand();
        }
        // If hand got a isCharlie, we're done with this hand
        else if(hand.isCharlie()) {
            hid.multiplyAmt(CHARLIE_PAYS);
            house.updateBankroll(player,hid.getAmt(),PROFIT);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.charlie(hid);
            
            goNextHand();
        }
        // Player has 21: don't force player to break!
        else if(hand.getValue() == 21) {
            goNextHand();
        }
    }    
    
    /**
     * Stands down player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid_ Player's hand id
     */
    public void stay(IPlayer player, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide STAY player = "+player);
            return;
        }
        
        // Since player stayed, we're done with hand
        goNextHand();
    }
    
    /**
     * Double down player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid_ Player's hand id
     */    
    public void doubleDown(IPlayer player, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        
        if(hand == null) {
            LOG.error("got invalide DOUBLE DOWN player = "+player);
            return;
        }
        
        LOG.info("got double down amt = "+hid.getAmt()+" hid = "+hid);
        
        // Dealer must double bet since one in hid is a copy -- not dealers
        hand.dubble();
       
        Card card = shoe.next();
        
        // Double the bet and hit the hand once
        hand.hit(card);
        
        // Send the card out to everyone
        for (IPlayer _player : playerSequence)
            _player.deal(hid, card, hand.getValues());
        
        // If hand broke, update the account and tell everyone
        if(hand.isBroke()) {
            house.updateBankroll(player,hid.getAmt(),LOSS);
            
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
        }
        
        // Go to next hand regardless on a double down
        goNextHand();
    }
    
    /**
     * Moves to the next hand at the table
     */
    protected void goNextHand() {
        // Get next hand and inform player
        if (handSeqIndex < handSequence.size()) {
            Hid hid = handSequence.get(handSeqIndex++);

            active = players.get(hid);
            LOG.info("active player = " + active);

            // Check for isBlackjack before moving on
            Hand hand = this.hands.get(hid);

            // If hand has Blackjack, it's not automatic hand wins
            // since the dealer may also have isBlackjack
            if (hand.isBlackjack()) {               
                goNextHand();
                return;
            }

            // Unless the player got a isBlackjack, tell the player they're
            // to start playing this hand
            for (IPlayer player: playerSequence) {
                LOG.info("sending turn "+hid+" to "+player);
                player.play(hid);
            }

            return;
        }

        // If there are no more hands, close out game with dealer
        // making last play.
        closeGame();
    }
    
    protected void closeGame() { 
        // Tell everyone it's dealer's turn
        signal();
        
        // "null" card means update the value of the hand
        for (IPlayer player : playerSequence)
            player.deal(dealerHand.getHid(), null, dealerHand.getValues());
     
        // Dealer only plays if there is someone standing and dealer doesn't
        // isBlackjack
        if (handsStanding() && !dealerHand.isBlackjack()) {
            // Draw until we reach (any) 17 or we break
            while (dealerHand.getValue() < 17) {
                Card card = shoe.next();

                dealerHand.hit(card);
                
                try {
                    Thread.sleep(Constant.DEAL_DELAY);
                }
                catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Dealer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                // Tell everybody what dealer drew
                for (IPlayer player : playerSequence) {
                    player.deal(dealerHand.getHid(), card, dealerHand.getValues());
                }
            }
        }
        
        // Compute outcomes and inform everyone
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            // These handled during hit cycle
            if(hand.isBroke() || hand.isCharlie() || hand.isBlackjack())
                continue;

            // If hand less than dealer and dealer not isBroke, hand lost
            if(hand.getValue() < dealerHand.getValue() && !dealerHand.isBroke()) {
                house.updateBankroll(players.get(hid),hid.getAmt(),LOSS);
                
                for (IPlayer player: playerSequence)
                    player.loose(hid);
            }
            // If hand less than dealer and dealer isBroke OR...
            //    hand greater than dealer and dealer NOT isBroke => hand won
            else if(hand.getValue() < dealerHand.getValue() && dealerHand.isBroke() ||
                    hand.getValue() > dealerHand.getValue() && !dealerHand.isBroke()) {
                
                house.updateBankroll(players.get(hid),hid.getAmt(),PROFIT);
                
                for (IPlayer player: playerSequence)
                    player.win(hid);   
            }
            // If player and dealer hands same, hand pushed
            else if(hand.getValue() == dealerHand.getValue())
                for (IPlayer player: playerSequence)
                    player.push(hid);
//            else
//                LOG.error("bad outcome");

        }
        
        // Wrap up the game
        wrapUp();
    }
    
    /**
     * Tells everyone game over.
     */
    protected void wrapUp() {
        for (IPlayer player: playerSequence)           
            player.endGame(shoe.size());      
    }
    
    /**
     * Tells everyone it's dealers turn.
     */
    protected void signal() {
        for (IPlayer player: playerSequence) {
            // Reveal hole card for bot
            if(player instanceof IBot)
                player.deal(dealerHand.getHid(), holeCard, dealerHand.getValues());
            
            // Tell player it's dealers turn
            player.play(this.dealerHand.getHid());
        }    
    }
    
    /**
     * Returns true if there are any hands that haven't isBroke
     * @return True if at least one hand hasn't broken, false otherwise
     */
    protected boolean handsStanding() {
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            if(!hand.isBroke() && !hand.isBlackjack() && !hand.isCharlie())
                return true;
        }
        
        return false;
    }
    
//    /**
//     * Double the bet amount in a hand id
//     * @param hid 
//     */
//    protected void dubble(Hid hid) {
//        int match = this.handSequence.indexOf(hid);
//        
//        Hid hid_ = handSequence.get(match);
//        
//        hid_.setAmt(hid_.getAmt());        
//    }
    
    /**
     * Validates a hand.
     * @param hid_ Hand
     * @return True if had is valid, false otherwise
     */
    protected Hand validate(Hid hid) {
        if(hid == null)
            return null;
        
        Hand hand = hands.get(hid);
        
        if(hand.isBroke())
            return null;
        
        if(players.get(hid) != active)
            return null;
        
        return hand;
    }
}