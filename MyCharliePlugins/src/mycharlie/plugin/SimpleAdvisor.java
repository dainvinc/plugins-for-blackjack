/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mycharlie.plugin;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 *
 * @author maristuser
 */
public class SimpleAdvisor implements IAdvisor {
    public HashMap<String, Play> moves = new HashMap<>();
    public BufferedReader br;
    
    public SimpleAdvisor() {
        br = new BufferedReader(new InputStreamReader(System.in));
        
        this.moves.put("H", Play.HIT);
        this.moves.put("S", Play.STAY);
        this.moves.put("P", Play.SPLIT);
        this.moves.put("D", Play.DOUBLE_DOWN);
    }

    @Override
    public Play advise(Hand myHand, Card upCard) {
        Card card1 = myHand.getCard(0);
        Card card2 = myHand.getCard(1);
        
        if(card1.isAce() && card2.isAce()) {
            return Play.SPLIT;
        }
        
        if(myHand.getValue() >= 17) {
            return Play.STAY;
        }
        
        if(myHand.getValue() <= 10) {
            return Play.HIT;
        }
        
        if(myHand.getValue() == 11) {
            return Play.DOUBLE_DOWN;
        }
        
        if(myHand.getValue() >= 12 && myHand.getValue() <=16) {
            if(upCard.value() >= 10 || upCard.value() <= 16){
                System.out.println("**********************************"+upCard.value());
                return Play.STAY;
            }
                
            else if(upCard.value() >= 10 || upCard.value() > 16)
                return Play.HIT;
        }

        return Play.NONE;
    }
    
}
