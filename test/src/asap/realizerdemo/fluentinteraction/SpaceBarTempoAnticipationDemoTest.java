package asap.realizerdemo.fluentinteraction;

import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JFrame;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;

import hmi.testutil.demotester.DefaultFestDemoTester;

public class SpaceBarTempoAnticipationDemoTest extends DefaultFestDemoTester
{
    private JFrame testFrame = createJFrame();
    private FrameFixture window = createFrameFixture(testFrame);

    @Test
    public void test() throws IOException, InterruptedException
    {
        SpaceBarTempoAnticipationDemo demo = new SpaceBarTempoAnticipationDemo();
        demo.init(testFrame);
        demo.startClocks();
        demo.startConduct();
        Thread.sleep(4000);
        for(int i=0;i<20;i++)
        {
            window.pressAndReleaseKeys(KeyEvent.VK_SPACE);
            Thread.sleep(1000);
        }        
        window.close();
    }

    @After
    public void cleanup()
    {
        window.cleanUp();
    }
}
