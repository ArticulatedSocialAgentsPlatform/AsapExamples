package asap.realizerdemo.fluentinteraction;

import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;
import hmi.testutil.demotester.FestUtils;

/**
 * Integration tests for the TurnKeepDemo
 * @author hvanwelbergen
 *
 */
public class TurnKeepDemoTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        TurnKeepDemo demo = new TurnKeepDemo(testFrame);        
        demo.startClocks();
        Thread.sleep(4000);
        FestUtils.clickButton("Play",window);
        Thread.sleep(10000);
        window.pressKey(KeyEvent.VK_SPACE);
        Thread.sleep(1000);
        window.releaseKey(KeyEvent.VK_SPACE);
        Thread.sleep(2000);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
