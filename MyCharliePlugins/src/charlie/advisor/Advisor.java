package charlie.advisor;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;

/**
 *
 * @author Vishal
 */
public class Advisor implements IAdvisor{
    BasicStrategy bs = new BasicStrategy();

    @Override
    public Play advise(Hand myHand, Card upCard) {
        return bs.getPlay(myHand, upCard);
    }
}
