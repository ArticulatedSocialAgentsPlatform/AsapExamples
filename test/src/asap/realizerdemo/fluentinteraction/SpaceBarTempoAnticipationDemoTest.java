/*******************************************************************************
 * Copyright (C) 2009-2020 Human Media Interaction, University of Twente, the Netherlands
 *
 * This file is part of the Articulated Social Agents Platform BML realizer (ASAPRealizer).
 *
 * ASAPRealizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ASAPRealizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ASAPRealizer.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
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
