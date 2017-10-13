/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.advisor;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;

/**
 *
 * @author Vishal
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Prototype advisor which uses human input as advice.
 * @author Ron Coleman
 */
public class BasicStrategy implements IAdvisor { 
    public final static Play H = Play.HIT;
    public final static Play S = Play.STAY;
    public final static Play P = Play.SPLIT;
    public final static Play D = Play.DOUBLE_DOWN;
    private Hand myHand;
    private Card upCard;
    public final static HashMap<String,ArrayList<Play>> SECTION1 = new HashMap<>();
    public final static HashMap<String,ArrayList<Play>> SECTION2 = new HashMap<>();
    public final static HashMap<String,ArrayList<Play>> SECTION3 = new HashMap<>();
    public final static HashMap<String,ArrayList<Play>> SECTION4 = new HashMap<>();
    static {
                                                    // 0 1 2 3 4 5 6 7 8 9: index
                                                    // 2 3 4 5 6 7 9 9 0 A: rank
//        SECTION2.put("4",new ArrayList<>(Arrays.asList(H,H,H,P,P,H,H,H,H,H)));                                                        
//        SECTION2.put("3",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,H,H,H,H)));        
        SECTION4.put("12",new ArrayList<>(Arrays.asList(H,H,S,S,S,H,H,H,H,H)));
        SECTION4.put("13",new ArrayList<>(Arrays.asList(S,S,S,S,S,H,H,H,H,H)));
        SECTION4.put("14",new ArrayList<>(Arrays.asList(S,S,S,S,S,H,H,H,H,H)));
        SECTION4.put("15",new ArrayList<>(Arrays.asList(S,S,S,S,S,H,H,H,H,H)));
        SECTION4.put("16",new ArrayList<>(Arrays.asList(S,S,S,S,S,H,H,H,H,H)));
        SECTION4.put("17",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        SECTION4.put("18",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        SECTION4.put("19",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        SECTION4.put("20",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
//        SECTION4.put("21",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        
        SECTION3.put("5",new ArrayList<>(Arrays.asList(H,H,H,H,H,H,H,H,H,H)));
        SECTION3.put("6",new ArrayList<>(Arrays.asList(H,H,H,H,H,H,H,H,H,H)));
        SECTION3.put("7",new ArrayList<>(Arrays.asList(H,H,H,H,H,H,H,H,H,H)));
        SECTION3.put("8",new ArrayList<>(Arrays.asList(H,H,H,H,H,H,H,H,H,H)));
        SECTION3.put("9",new ArrayList<>(Arrays.asList(H,D,D,D,D,H,H,H,H,H)));
        SECTION3.put("10",new ArrayList<>(Arrays.asList(D,D,D,D,D,D,D,D,H,H)));
        SECTION3.put("11",new ArrayList<>(Arrays.asList(D,D,D,D,D,D,D,D,D,H)));
        
        SECTION2.put("2",new ArrayList<>(Arrays.asList(H,H,H,D,D,H,H,H,H,H)));
        SECTION2.put("3",new ArrayList<>(Arrays.asList(H,H,H,D,D,H,H,H,H,H)));
        SECTION2.put("4",new ArrayList<>(Arrays.asList(H,H,D,D,D,H,H,H,H,H)));
        SECTION2.put("5",new ArrayList<>(Arrays.asList(H,H,D,D,D,H,H,H,H,H)));
        SECTION2.put("6",new ArrayList<>(Arrays.asList(H,D,D,D,D,H,H,H,H,H)));
        SECTION2.put("7",new ArrayList<>(Arrays.asList(S,D,D,D,D,S,S,H,H,H)));
        SECTION2.put("8",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        SECTION2.put("9",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        SECTION2.put("10",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));
        
        
        SECTION1.put("A",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,P,P,P,P)));                                                        
        SECTION1.put("10",new ArrayList<>(Arrays.asList(S,S,S,S,S,S,S,S,S,S)));        
        SECTION1.put("9",new ArrayList<>(Arrays.asList(P,P,P,P,P,S,P,P,S,S)));
        SECTION1.put("8",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,P,P,P,P)));                                                      
        SECTION1.put("7",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,H,H,H,H)));        
        SECTION1.put("6",new ArrayList<>(Arrays.asList(P,P,P,P,P,H,H,H,H,H)));
        SECTION1.put("5",new ArrayList<>(Arrays.asList(D,D,D,D,D,D,D,D,H,H)));
        SECTION1.put("4",new ArrayList<>(Arrays.asList(H,H,H,P,P,H,H,H,H,H)));                                                        
        SECTION1.put("3",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,H,H,H,H)));        
        SECTION1.put("2",new ArrayList<>(Arrays.asList(P,P,P,P,P,P,H,H,H,H)));
    }
    
    /** Play table to map from human input to advice */
    protected HashMap<String, Play> plays = new HashMap<>();
    
    /** Reader to get player input */
    protected BufferedReader br;
    
    /**
     * Constructor
     */
    public BasicStrategy() {
        //NOTES
        //if(!isValid(myHand) || !isValid(upCard))
        //Create class BasicStrategy
        //return bs.getPlay(myHand, upCard) -- contains in *Advisor.java
        //all the code goes into BasicStrategy
     
        // This used to get input from the player
        this.br = new BufferedReader(new InputStreamReader(System.in));
        
        // Mapping from player input to play
        this.plays.put("S", Play.STAY);
        this.plays.put("H", Play.HIT);
        this.plays.put("D", Play.DOUBLE_DOWN);
        this.plays.put("P", Play.SPLIT);
    }
    
    public BasicStrategy(Hand myHand, Card upCard) {
        this.myHand = myHand;
        this.upCard = upCard;
    }
    /**
     * Gives advice.
     * @param myHand Player's hand
     * @param upCard Dealer's up-card
     * @return A play type
     */
    @Override
    public Play advise(Hand myHand, Card upCard) {
        Card card1 = myHand.getCard(0);
        Card card2 = myHand.getCard(1);
        
        if(myHand.isPair())
            return doSection1(myHand,upCard);
        
        if(myHand.size() == 2 && (card1.isAce() || card2.isAce()))
            return doSection2(myHand,upCard);
            
        if(myHand.getValue() >= 5 && myHand.getValue() <= 11)
            return doSection3(myHand,upCard);
        
        return doSection4(myHand,upCard);
    }
    
    protected Play doSection1(Hand myHand, Card upCard) {
        Card card = myHand.getCard(0);
        String rank = card.getRank() + "";
        ArrayList<Play> plays = SECTION1.get(rank);
        int index = upCard.isAce() ? 9 : upCard.getRank() - 2;
        Play play = plays.get(index);    
        return play;
    }
    
    protected Play doSection2(Hand myHand, Card upCard) {
        Card card = myHand.getCard(0).isAce() ? myHand.getCard(1) : myHand.getCard(0);
        String rank = card.getRank() + "";
        ArrayList<Play> plays = SECTION2.get(rank);
        int index = upCard.isAce() ? 9 : upCard.getRank() - 2;
        Play play = plays.get(index);    
        return play;
    } 
    
    protected Play doSection3(Hand myHand, Card upCard) {
        System.out.println("Section 3 - *************** ");
        String rank = myHand.getValue() + "";
        System.out.println("Rank - *************** " +rank);
        ArrayList<Play> plays = SECTION3.get(rank);
        System.out.println("Card - *************** " +plays);
        int index;
        if(upCard.isFace() || (upCard.getRank() == 10)) {
            index = 8;
        } else if(upCard.isAce()) {
            index = 9;
        } else {
            index = upCard.getRank() - 2;
        }
        System.out.println("Index - *************** " +index +"** : "+upCard.getRank());
        Play play = plays.get(index);
        System.out.println("play - ***********v**** " +play);
        return play;
    }
    
    protected Play doSection4(Hand myHand, Card upCard) {
        System.out.println("Section 4 *************** ");
        String rank = myHand.getValue() + "";
        System.out.println("Plays - *************** " +rank);
        ArrayList<Play> plays = SECTION4.get(rank);
        System.out.println("Plays - *************** " +plays);
//        int index = upCard.isFace() ? 8 : upCard.getRank() - 2;
        int index;
        if(upCard.isFace() || (upCard.getRank() == 10)) {
            index = 8;
        } else if(upCard.isAce()) {
            index = 9;
        } else {
            index = upCard.getRank() - 2;
        }
        System.out.println("Plays - *************** " +index);
        Play play = plays.get(index);
        System.out.println("Plays - *************** " +play);
        return play;
    }    

    protected Play getPlay() {
        return advise(myHand, upCard);
    }
}

