package asap.realizerdemo.fluentinteraction;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;
import hmi.testutil.demotester.FestUtils;

/**
 * Integration tests for AsapRealizerDemoNoise
 * @author hvanwelbergen
 */
public class NoiseDemoTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        NoiseDemo demo = new NoiseDemo(testFrame, "asaparmandia_vp_nogui.xml");        
        demo.startClocks();
        Thread.sleep(4000);
        FestUtils.clickButton("Play",window);
        Thread.sleep(2000);
        FestUtils.clickButton("AnnounceStart",window);
        Thread.sleep(4000);
        FestUtils.clickButton("AnnounceEnd",window);
        Thread.sleep(4000);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
