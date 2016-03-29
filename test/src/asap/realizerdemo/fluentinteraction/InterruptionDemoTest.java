package asap.realizerdemo.fluentinteraction;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;
import hmi.testutil.demotester.FestUtils;

/**
 * Unit tests for the interruption demo
 * @author hvanwelbergen
 *
 */
public class InterruptionDemoTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        InterruptionDemo demo = new InterruptionDemo(testFrame);        
        demo.startClocks();
        Thread.sleep(4000);
        FestUtils.clickButton("Play",window);
        Thread.sleep(10000);
        FestUtils.clickButton("Interrupt",window);
        Thread.sleep(1000);
        FestUtils.clickButton("Continue",window);
        Thread.sleep(4000);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
