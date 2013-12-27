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
package charlie.controller;

import charlie.card.Hand;
import charlie.card.Shoe;
import charlie.card.DealerHand;
import charlie.actor.House;
import charlie.actor.RealPlayer;
import charlie.card.Card;
import charlie.card.HoleCard;
import charlie.card.Hid;
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
 * @author Ron Coleman
 */
public class Dealer implements Serializable { 
    private final Logger LOG = LoggerFactory.getLogger(Dealer.class);
    private final static Double PROFIT = 1.0;
    private final static Double LOSS = -1.0;
    protected Shoe shoe;
    protected HashMap<Hid,Hand> hands = new HashMap<>();
    protected HashMap<Hid,Integer> bets = new HashMap<>();
    protected HashMap<Hid,IPlayer> players = new HashMap<>();
    protected HashMap<IPlayer,Double> accounts = new HashMap<>();
    protected List<Hid> handSequence = new ArrayList<>();
    protected List<IPlayer> playerSequence = new ArrayList<>();
    protected final House house;
    protected Integer handSeqIndex = 0;
    protected IPlayer active = null;
    protected DealerHand dealerHand;
    
    /**
     * Constructor
     * @param house House actor which launched us.
     */
    public Dealer(House house) {
        this.house = house;

        // Instantiate the shoe
        Properties props = house.getProps();
        
        int shoeno = Integer.parseInt(props.getProperty("charlie.shoe", "-1"));
        
        if(shoeno == -1)
            shoe = new Shoe();
        else
            shoe = new Shoe(shoeno);
    }
    
    /**
     * Receives a bet request from a "real" player. Don't invoke this method
     * for a bot. Bots are instantiated directly by this class.
     * @param player
     * @param hid
     * @param bet 
     */
    public void bet(RealPlayer player,Hid hid,Integer bet) {
        LOG.info("got new bet = "+bet+" from "+player+" for hid = "+hid);
        
        // Clear out the old stuff
        handSequence.clear();
        playerSequence.clear();
        hands.clear();
        bets.clear();

        // Add hid in sequence of hands to play
        handSequence.add(hid);
        
        handSeqIndex = 0;
        
        // Add player in sequence of players
        playerSequence.add(player); 
        
        // Add a bet for this hand
        bets.put(hid, bet);
        
        // Connect this hand to a player
        players.put(hid, player);
       
        // Add new hand for this player
        hands.put(hid, new Hand(hid));
        
        // Create the bots
        spawnBots();

        // Create the dealer hand
        dealerHand = new DealerHand(new Hid(Seat.DEALER));
        
        // Shuffle cards, if needed
        if(shoe.shuffleNeeded()) {
            shoe.shuffle();
            
            for(IPlayer _player: playerSequence)
                _player.shuffling();
        }
        
        // Let the game begin!
        startGame();
    }
        
    protected void spawnBots() {
        // TODO:
    }
    
    /** Starts a game */
    protected void startGame() {
        LOG.info("starting a game");
        try {
            // Gather up all the initial hands
            List<Hid> hids = new ArrayList<>();
            
            for(Hid hid: hands.keySet()) {
                hids.add(hid);
            }
            
            // Include the dealer's hand
            hids.add(dealerHand.getHid());
          
            LOG.info("hands at table + dealer = "+hids.size());
            
            // Tell each player we're starting a game
            for(IPlayer player: playerSequence)              
                player.startGame(hids);
            
            Thread.sleep(250);
            
            // First round hole card sent to everyone
            HoleCard holeCard = new HoleCard(shoe.next());
            dealerHand.hit(holeCard);
            round(hids,holeCard);
            Thread.sleep(Constant.DEAL_DELAY);
            
            // Second round up-card sent to everyone
            Card upCard = shoe.next();
            dealerHand.hit(upCard);
            round(hids,upCard);

            // Revalue the dealer's hand since the normal hit doesn't count
            // the hole card
            dealerHand.revalue();
            
            // CHeck if players want to buy insurance
            if(upCard.isAce())
                insure();
            
            if(dealerHand.blackjack()) {
                closeGame();
            }
            else
                goNextHand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insures a dealer blackjack
     */
    protected void insure() {
        // TODO
    }
    
    /**
     * Deals a round of cards to everyone.
     * @param hids Hand ids
     * @param dealerCard The dealer card, up or hole.
     */
    protected void round(List<Hid> hids,Card dealerCard) {
        try {
            for(Hid hid: hids) {
                IPlayer player = players.get(hid);
                
                // If there's no correspondsing player, must be dealer's hid
                if(player == null)
                    continue;
                
                // Get a card from the shoe
                Card card = shoe.next();
                
                // Deal this card
                LOG.info("dealing to "+player+" card 1 = "+card); 
                
                Hand hand = this.hands.get(hid);
                
                hand.hit(card);
                
                player.deal(hid, card, hand.getValues());

                Thread.sleep(Constant.DEAL_DELAY);

                // Deal the corresponding dealer card
                LOG.info("sending dealer card = "+dealerCard);

                player.deal(dealerHand.getHid(), dealerCard, dealerHand.getValues());
            }            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hits player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid Player's hand id
     */
    public void hit(IPlayer player,Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide HIT player = "+player);
            return;
        }
        
        // Deal a card
        Card card = shoe.next();
        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        // If the hand broke, we're done with this hand
        if(hand.broke()) {
            house.updateBankroll(player,(double)bets.get(hid),LOSS);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
            
            goNextHand();
        }
        // If hand got a charlie, we're done with this hand
        else if(hand.charlie()) {
            house.updateBankroll(player,(double)bets.get(hid),PROFIT);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.charlie(hid);
            
            goNextHand();
        }
    }    
    
    /**
     * Stands down player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid Player's hand id
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
     * @param hid Player's hand id
     */    
    public void doubleDown(IPlayer player, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        
        if(hand == null) {
            LOG.error("got invalide DOUBLE DOWN player = "+player);
            return;
        }
        
        Card card = shoe.next();

        hand.hit(card);
        
        // Doubble the bet and hit the hand once
        Integer bet = bets.get(hid) * 2;

        bets.put(hid, bet);
        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        // If hand broke, tell everone
        if(hand.broke())
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
        
        // Go to next hand regardless
        goNextHand();
    }
    
    /**
     * Moves to the next hand at the table
     */
    protected void goNextHand() {
        // Get next hand and inform player
        if(handSeqIndex < handSequence.size()) {
            Hid hid = handSequence.get(handSeqIndex++);
            
            active = players.get(hid);
            LOG.info("active player = "+active);

            // Check for blackjack before moving on
            Hand hand = this.hands.get(hid);
            
            // If hand has blackjack, it's not automatic hand wins
            // since the dealer may also have blackjack
            if(hand.blackjack()) {
                for (IPlayer player : playerSequence)
                    player.blackjack(hid);
             
                goNextHand();
                
                return;
            }
            
            // Unless the player got a blackjack, tell the player they're
            // to start playing this hand
            active.play(hid);
            
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
        // blackjack
        if (handsStanding() && !dealerHand.blackjack()) {
            // Draw until we reach (any) 17 or we break
            while (dealerHand.getValue() < 17) {
                Card card = shoe.next();

                dealerHand.hit(card);

                // Tell everybody what dealer drew
                for (IPlayer player : playerSequence) {
                    player.deal(dealerHand.getHid(), card, dealerHand.getValues());
                }
                
                try {
                    if(dealerHand.getValue() < 17)
                        Thread.sleep(Constant.DEAL_DELAY);
                }
                catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Dealer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // Compute outcomes and inform everyone
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            // These handled during hit cycle
            if(hand.broke() || hand.charlie())
                continue;

            // If hand less than dealer and dealer not broke, hand lost
            if(hand.getValue() < dealerHand.getValue() && !dealerHand.broke()) {
                house.updateBankroll(players.get(hid),(double)bets.get(hid),LOSS);
                
                for (IPlayer player: playerSequence)
                    player.loose(hid);
            }
            // If hand less than dealer and dealer broke OR...
            //    hand greater than dealer and dealer NOT broke => hand won
            else if(hand.getValue() < dealerHand.getValue() && dealerHand.broke() ||
                    hand.getValue() > dealerHand.getValue() && !dealerHand.broke()) {
                house.updateBankroll(players.get(hid),(double)bets.get(hid),PROFIT);
                
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
     * Tells everyone the game is over.
     */
    protected void wrapUp() {
        for (IPlayer player: playerSequence) {
            if(accounts.containsKey(player)) {
                Double bankroll = accounts.get(player);
                player.endGame(bankroll);
            }
        }         
    }
    
    /**
     * Tells everyone it's dealer's turn.
     */
    protected void signal() {
        for (IPlayer player: playerSequence) {
            player.play(this.dealerHand.getHid());
        }    
    }
    
    /**
     * Returns true if there are any hands that haven't broke
     * @return True if at least one hand hasn't broken, false otherwise
     */
    protected boolean handsStanding() {
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            if(!hand.broke())
                return true;
        }
        
        return false;
    }
    
    /**
     * Validates a hand.
     * @param hid Hand
     * @return True if had is valid, false otherwise
     */
    protected Hand validate(Hid hid) {
        if(hid == null)
            return null;
        
        Hand hand = hands.get(hid);
        
        if(hand.broke())
            return null;
        
        if(players.get(hid) != active)
            return null;
        
        return hand;
    }
}
