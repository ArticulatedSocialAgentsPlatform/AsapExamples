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

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Resources;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import saiba.bml.builder.BehaviourBlockBuilder;
import asap.bml.ext.bmla.builder.BMLABMLBehaviorAttributesBuilder;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizer.pegboard.PegBoard;
import asap.realizerport.BMLFeedbackListener;

/**
 * Demonstrates gesture + speech interruption
 * @author hvanwelbergen
 * 
 */
public class InterruptionDemo implements BMLFeedbackListener
{
    private final AsapVirtualHuman vHuman;
    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;

    public InterruptionDemo(final JFrame jf) throws IOException
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        hre = new HmiRenderEnvironment();
        ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");
        WorldObjectEnvironment we = new WorldObjectEnvironment();

        try
        {
            SwingUtilities.invokeAndWait(() -> initUI(jf));
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }

        hre.init(); // canvas does not exist until init was called
        ope.init();
        aue.init();
        mae.init(ope);
        we.init();
        BMLTInfo.init();
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(mae);
        environments.add(ee);
        environments.add(aue);
        environments.add(we);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!

        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(ee);

        hre.loadCheckerBoardGround("ground", 0.5f, 0);
        hre.setBackground(0.5f, 0.5f, 0.5f);

        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new VJointWorldObject(camera));

        vHuman = ee.loadVirtualHuman("armandia", "", "asaparmandia_vp_nogui.xml", "armandia - interruption demo");
        vHuman.getRealizerPort().addListeners(this);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        jf.add(canvas);
        jf.setVisible(true);
    }

    private void initUI(final JFrame jf)
    {

        jf.setTitle("Test new HmiEnvironment");
        jf.setSize(1000, 600);

        jf.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });

        JMenuBar mb = new JMenuBar();

        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                play();
            }
        });
        mb.add(playButton);

        JButton interruptButton = new JButton("Interrupt");
        interruptButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                interrupt();
            }
        });
        mb.add(interruptButton);

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                continueCall();
            }
        });
        mb.add(continueButton);

        jf.setJMenuBar(mb);

    }

    private String readFile(String filename) throws IOException
    {

        Resources res = new Resources("");
        BufferedReader reader = res.getReader(filename);
        StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1)
        {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    private void printPegBoard(PegBoard pb)
    {
        System.out.println("BlockPegs: " + pb.getBMLBlockPegs());
        System.out.println("TimePegs: " + pb.getTimePegs());
    }

    public void interrupt()
    {
        String str = new BehaviourBlockBuilder().id("yieldturn")
                .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder().addToInterrupt("bml1").build()).build()
                .toBMLString();
        vHuman.getRealizerPort().performBML(str);
    }

    public void continueCall()
    {
        try
        {
            vHuman.getRealizerPort().performBML(readFile("fluentdemos/interrupt/continue.xml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void play()
    {
        printPegBoard(vHuman.getPegBoard());
        vHuman.getRealizerPort().performBML(BehaviourBlockBuilder.resetBlock().toBMLString());
        try
        {
            vHuman.getRealizerPort().performBML(readFile("fluentdemos/interrupt/nhm.xml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    public static void main(String[] arg) throws IOException, InterruptedException, InvocationTargetException
    {
        InterruptionDemo demo = new InterruptionDemo(new JFrame());
        demo.startClocks();
    }

    @Override
    public void feedback(String feedback)
    {
        System.out.println(feedback);
    }
}
