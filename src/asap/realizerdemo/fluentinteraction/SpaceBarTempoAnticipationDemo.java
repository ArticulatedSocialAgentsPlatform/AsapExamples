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

import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.ClockListener;
import hmi.util.Console;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import saiba.bml.builder.BehaviourBlockBuilder;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.bml.ext.bmlt.builder.BMLTControllerBehaviourBuilder;
import asap.bml.ext.bmlt.builder.BMLTProcAnimationBehaviourBuilder;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizer.anticipator.SpaceBarTempoAnticipator;
import asap.realizer.anticipator.gui.JPanelSpaceBarTempoAnticipatorVisualization;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizerport.RealizerPort;
import asap.realizerport.util.StderrWarningListener;

/**
 * Shows how to align behavior to time pegs provided by an anticipator. Press spacebar rhythmically to change the tempo.
 * @author Herwin
 * 
 */
public class SpaceBarTempoAnticipationDemo implements ClockListener
{
    private SpaceBarTempoAnticipator sbta;
    public RealizerPort realizerPort;
    private JPanelSpaceBarTempoAnticipatorVisualization tempoViz;
    private HmiRenderEnvironment hre;
    private OdePhysicsEnvironment ope;

    public void init(JFrame j) throws IOException
    {
        Console.setEnabled(false);
        System.setProperty("sun.java2d.noddraw", "true");
        initRealizer();
        initAnticipator(0);        
        try
        {
            SwingUtilities.invokeAndWait(()->initUI(j));            
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }     
    }

    private void initRealizer() throws IOException
    {
        BMLTInfo.init();
        hre = new HmiRenderEnvironment();
        hre.loadCheckerBoardGround("ground", 0.3f, 0);
        ope = new OdePhysicsEnvironment();
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();

        hre.init(); // canvas does not exist until init was called
        ope.init();
        mae.init(ope);

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(we);
        environments.add(mae);
        environments.add(ee);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        ope.addPrePhysicsCopyListener(ee);
        ope.getPhysicsClock().addClockListener(this);
        AsapVirtualHuman vh = ee.loadVirtualHuman("humanoid", "", "asaparmandia_minimal_notts.xml", "AsapRealizer demo");
        sbta = new SpaceBarTempoAnticipator("spacebaranticipator", vh.getPegBoard());
        sbta.setPhysicsClock(ope.getPhysicsClock());        
        realizerPort = vh.getRealizerPort();
        realizerPort.addListeners(new StderrWarningListener());
    }

    private void initAnticipator(double startTime)
    {
        for (int i = 0; i < 1000; i++)
        {
            TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
            tp.setGlobalValue(i * 1);
            sbta.addSynchronisationPoint("tick" + i, tp);
        }
    }

    private void resetAnticipator(double startTime)
    {
        for (int i = 0; i < 1000; i++)
        {
            sbta.setSynchronisationPoint("tick" + i, i * 1 + startTime);
        }
    }

    private void initUI(JFrame j)
    {
        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });
        JMenuBar menuBar = new JMenuBar();
        menuBar.addKeyListener(sbta);
        j.addKeyListener(sbta);
        JPanel pPressed = new JPanel();
        JPanel pPredict = new JPanel();
        tempoViz = new JPanelSpaceBarTempoAnticipatorVisualization(pPressed, pPredict, sbta, sbta);
        sbta.addObserver(tempoViz);
        JButton playButton = new JButton("Reset");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                play();
            }
        });
        menuBar.add(playButton);
        menuBar.add(pPressed);
        menuBar.add(pPredict);

        hre.getAWTComponent().addKeyListener(sbta);
        j.setJMenuBar(menuBar);
        j.add(hre.getAWTComponent(), BorderLayout.CENTER);
        j.setSize(1000, 600);
        j.setVisible(true);
    }

    public void play()
    {
        realizerPort.performBML(BehaviourBlockBuilder.resetBlock().toBMLString());
        resetAnticipator(ope.getPhysicsClock().getMediaSeconds());
        realizerPort.performBML(getConductBML());
    }

    @Override
    public void initTime(double initTime)
    {

    }

    @Override
    public void time(double currentTime)
    {
        tempoViz.update(currentTime);
    }

    private String getConductBML()
    {
        BehaviourBlockBuilder builder = new BehaviourBlockBuilder();
        builder.id("bml1").addBehaviour(new BMLTControllerBehaviourBuilder("bml1", "balance1", "BalanceController").build());
        int j = 1;
        for (int i = 1; i < 100; i++)
        {
            builder.addBehaviour(new BMLTProcAnimationBehaviourBuilder("bml1", "conduct" + i, "3-beat").build())
                    .addAtConstraint("conduct" + i, "start", "anticipators:spacebaranticipator", "tick" + j++)
                    .addAtConstraint("conduct" + i, "beat2", "anticipators:spacebaranticipator", "tick" + j++)
                    .addAtConstraint("conduct" + i, "beat3", "anticipators:spacebaranticipator", "tick" + j++)
                    .addAtConstraint("conduct" + i, "end", "anticipators:spacebaranticipator", "tick" + j);
        }
        return builder.build().toBMLString();
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    public void startConduct()
    {
        realizerPort.performBML(getConductBML());
    }
    
    public static void main(String[] arg) throws Exception
    {
        SpaceBarTempoAnticipationDemo env = new SpaceBarTempoAnticipationDemo();
        env.init(new JFrame());
        env.startClocks();
        env.startConduct();
    }
}
