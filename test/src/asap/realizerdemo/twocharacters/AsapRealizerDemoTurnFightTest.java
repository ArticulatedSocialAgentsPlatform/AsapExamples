package asap.realizerdemo.twocharacters;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;
import hmi.testutil.demotester.FestUtils;

public class AsapRealizerDemoTurnFightTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        AsapRealizerDemoTurnFight demo = new AsapRealizerDemoTurnFight(testFrame);
        demo.startClocks();
        FestUtils.clickButton("Play",window);
        Thread.sleep(10000);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
