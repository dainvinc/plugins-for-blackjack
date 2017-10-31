package charlie.bot.server;

import charlie.advisor.BasicStrategy;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.plugin.IBot;
import charlie.util.Play;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *
 *
 * @author Vishal
 */
//PLay play = bs.getPLay(.hand, .card)
// play = correct(play);
// 
//protected Play correct(Play play)
// if(play != play.SPLIT)
// return play;
public class Huey extends BasicStrategy implements IBot, Runnable {
    final int MAX_THINKING = 5;
    Seat mine;
    Hand myHand, dealerHand;
    Dealer dealer;
    Card upCard;
    Random ran = new Random();
//    BasicStrategy bs = new BasicStrategy();
//    Play play = bs.getPlay(myHand, upCard);

    @Override
    public Hand getHand() {
        return myHand;
    }

    @Override
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    @Override
    public void sit(Seat seat) {
        mine = seat;
        Hid hid = new Hid(seat);
        myHand = new Hand(hid);
    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize) {
    }

    @Override
    public void endGame(int shoeSize) {
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
    }

    @Override
    public void insure() {
    }

    @Override
    public void bust(Hid hid) {
    }

    @Override
    public void win(Hid hid) {
    }

    @Override
    public void blackjack(Hid hid) {
    }

    @Override
    public void charlie(Hid hid) {
    }

    @Override
    public void lose(Hid hid) {
    }

    @Override
    public void push(Hid hid) {
    }

    @Override
    public void shuffling() {
    }

    @Override
    public void play(Hid hid) {
        if(hid.getSeat() != mine)
            return;
        
        new Thread(this).start();
    }

    @Override
    public void run() {
        int thinking = ran.nextInt(MAX_THINKING * 1000);
        try {
            Thread.sleep(thinking);
        } catch (InterruptedException ex) {
            Logger.getLogger(Huey.class.getName()).log(Level.SEVERE, null, ex);
        }
        dealer.stay(this, myHand.getHid());
        System.out.println(">>>>>>>>>" +myHand.getCard(0) + " + " +myHand.getValue());
    }
}