package charlie.bot.test;

import java.util.Random;

/**
 *
 * @author Vishal
 */
public class Shoe extends charlie.card.Shoe {
    @Override
    public void init() {
        ran = new Random(1);
        load();
        shuffle();
    }
}
