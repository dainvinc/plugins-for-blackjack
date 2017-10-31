package mycharlie.plugin;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prototype advisor which uses human input as advice.
 * @author Ron Coleman
 */
public class HumanAdvisor implements IAdvisor { 
    /** Play table to map from human input to advice */
    protected HashMap<String, Play> plays = new HashMap<>();
    
    /** Reader to get player input */
    protected BufferedReader br;
    
    /**
     * Constructor
     */
    public HumanAdvisor() {
        // This used to get input from the player
        this.br = new BufferedReader(new InputStreamReader(System.in));
        
        // Mapping from player input to play
        this.plays.put("S", Play.STAY);
        this.plays.put("H", Play.HIT);
        this.plays.put("D", Play.DOUBLE_DOWN);
        this.plays.put("P", Play.SPLIT);
    }
    
    /**
     * Gives advice.
     * @param myHand Player's hand
     * @param upCard Dealer's up-card
     * @return A play type
     */
    @Override
    public Play advise(Hand myHand, Card upCard) {
        while (true) {
            try {
                // Prompt the player and get their advice
                System.out.print("Advice [S, H, D, or P]? ");
                
                String input = br.readLine();
                
                // Validate the input
                if(input.length() == 0)
                    continue;
                
                String s = input.substring(0,1).toUpperCase();
                
                Play play = plays.get(s);
                
                if(play == null)
                    continue;
                
                // If we get here we have a valid play
                return play;
            } catch (IOException ex) {
                Logger.getLogger(HumanAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
