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
