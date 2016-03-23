package asap.realizerdemo;

import hmi.testutil.demotester.DefaultFestDemoTester;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

/**
 * Integration test to check if the AsapRealizerDemo runs. 
 * @author hvanwelbergen
 *
 */
public class AsapRealizerDemoIntegrationTest extends DefaultFestDemoTester
{
    private static final int DELAY = 4000;
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        AsapRealizerDemo demo = new AsapRealizerDemo(testFrame,"asaparmandia.xml");
        demo.startClocks();
        Thread.sleep(DELAY);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
