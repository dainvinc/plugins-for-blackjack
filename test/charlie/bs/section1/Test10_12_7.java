/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.bs.section1;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests my 12 vs dealer 7 which should be HIT.
 */
public class Test10_12_7 {
    @Test
    public void test() {
        // Generate an initially empty hand
        Hand myHand = new Hand(new Hid(Seat.YOU));
        
        // Put two cards in hand: 5 + 7
        Card card1 = new Card(5,Card.Suit.CLUBS);
        Card card2 = new Card(7,Card.Suit.SPADES);
        
        myHand.hit(card1);
        myHand.hit(card2);
        
        // Create dealer up card: 7
        Card upCard = new Card(7,Card.Suit.HEARTS);
        
        // Construct advisor and test it
        IAdvisor advisor = new BasicStrategy();
  
        Play advice = advisor.advise(myHand, upCard);
        
        // Validate the advise
        assertEquals(advice, Play.HIT);
    }
}