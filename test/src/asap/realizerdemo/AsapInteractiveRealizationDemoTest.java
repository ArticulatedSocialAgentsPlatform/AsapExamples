package asap.realizerdemo;

import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;
import hmi.testutil.demotester.FestUtils;

public class AsapInteractiveRealizationDemoTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);
    
    @Test
    public void test() throws IOException, InterruptedException
    {
        AsapInteractiveRealizationDemo demo = new AsapInteractiveRealizationDemo(testFrame, "asaparmandia_vp_nogui.xml");        
        demo.startClocks();
        Thread.sleep(4000);
        FestUtils.clickButton("bml1",window);
        Thread.sleep(8000);
        FestUtils.clickButton("bml2",window);
        Thread.sleep(1000);
        FestUtils.clickButton("bml3",window);
        Thread.sleep(1000);
        FestUtils.clickButton("bml4",window);
        Thread.sleep(1000);
        FestUtils.clickButton("bml5",window);
        Thread.sleep(4000);
        window.close();
    }
    
    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
