package asap.realizerdemo;

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Console;
import hmi.util.Resources;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import saiba.bml.builder.BehaviourBlockBuilder;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerport.RealizerPort;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Hooks up 5 buttons for interactive bml sending
 * @author hvanwelbergen
 * 
 */
public class AsapInteractiveRealizationDemo
{
    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;
    private final RealizerPort realizerPort;
    private boolean idleBehavior = true;
    protected JFrame mainJFrame = null;

    public AsapInteractiveRealizationDemo(JFrame j, String spec) throws IOException
    {
        Console.setEnabled(false);
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        mainJFrame = j;

        BMLTInfo.init();
        hre = new HmiRenderEnvironment();

        ope = new OdePhysicsEnvironment();
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");
        final JComponentEnvironment jce = new JComponentEnvironment();
        
        hre.init(); // canvas does not exist until init was called
        we.init();
        ope.init();
        aue.init();        
        mae.init(ope);

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(we);
        environments.add(ope);
        environments.add(mae);
        environments.add(ee);
        environments.add(aue);
        environments.add(jce);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(ee);

        hre.loadCheckerBoardGround("ground", 0.5f, 0f);
        hre.setBackground(0.5f, 0.5f, 0.5f);
        hre.setViewPoint(new float[] { 0.0f, 1.5060189f, 2.0078735f });

        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new VJointWorldObject(camera));

        AsapVirtualHuman vh = ee.loadVirtualHuman("humanoid", "", spec, "AsapRealizer demo");
        realizerPort = vh.getRealizerPort();

        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(WindowEvent winEvt)
            {
                // ee.requestShutdown();
                // while(!ee.isShutdown()){}
                System.exit(0);
            }
        });

        mainJFrame.setSize(1000, 600);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainJFrame.add(canvas, BorderLayout.CENTER);
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    setupButtons();
                }
            });
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        mainJFrame.setVisible(true);
    }

    public void reset()
    {
        realizerPort.performBML(BehaviourBlockBuilder.resetBlock().toBMLString());
        Resources res = new Resources("bmlscripts");
        if (idleBehavior)
        {
            try
            {
                realizerPort.performBML(res.read("blink_and_breathe.xml"));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public JButton getButton(final int i)
    {
        JButton jb = new JButton("bml" + i);
        jb.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Resources res = new Resources("bmlscripts");
                try
                {
                    String bmlContent = res.read("bml" + i + ".xml");
                    bmlContent = bmlContent.replaceAll("(?s)<!--.*?-->", "");
                    String bmls[] = Iterables.toArray(Splitter.on("</bml>").trimResults().omitEmptyStrings().split(bmlContent),
                            String.class);
                    for (String bml : bmls)
                    {
                        realizerPort.performBML(bml + "</bml>");
                    }
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
        return jb;
    }

    public void setupButtons()
    {
        JMenuBar menuBar = new JMenuBar();
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reset();
            }
        });
        menuBar.add(reset);
        for (int i = 1; i <= 5; i++)
        {
            final int nr = i;
            final JButton jb = getButton(nr);
            hre.getAWTComponent().addKeyListener(new KeyListener()
            {

                @Override
                public void keyPressed(KeyEvent e)
                {
                    if (e.getKeyChar() == '0' + nr)
                    {
                        jb.doClick();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e)
                {

                }

                @Override
                public void keyTyped(KeyEvent e)
                {

                }
            });
            menuBar.add(jb);
        }
        final JCheckBox box = new JCheckBox("blink & breath");
        box.setSelected(true);
        box.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                idleBehavior = box.isSelected();
            }
        });
        menuBar.add(box);
        mainJFrame.setJMenuBar(menuBar);
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    public static void main(String[] args) throws IOException
    {
        String spec = "asaparmandia_vp_notextengine.xml";
        if (args.length == 1)
        {
            spec = args[0];
        }
        AsapInteractiveRealizationDemo demo = new AsapInteractiveRealizationDemo(new JFrame("AsapInteractiveRealizer demo"), spec);
        demo.startClocks();
        demo.reset();
    }
}
