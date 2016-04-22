package asap.realizerdemo.twocharacters;

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Console;
import hmi.util.Resources;
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

import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

public class AsapRealizerDemoTurnFight
{
    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;
    protected JFrame mainJFrame = null;

    private final RealizerPort realizerPort1;
    private final RealizerPort realizerPort2;

    public AsapRealizerDemoTurnFight(JFrame j) throws IOException
    {
        this(j, "twocharacters/asaparmandia_prudence.xml", "twocharacters/asaparmandia_poppy.xml");
    }

    public AsapRealizerDemoTurnFight(JFrame j, String spec1, String spec2) throws IOException
    {
        System.setProperty("sun.java2d.noddraw", "true"); // avoid potential
                                                          // interference with
                                                          // (non-Jogl) Java
                                                          // using direct draw
        Console.setEnabled(false);
        BMLTInfo.init();
        hre = new HmiRenderEnvironment();
        ope = new OdePhysicsEnvironment();
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");

        mainJFrame = j;
        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });

        mainJFrame.setSize(1000, 600);

        BMLTInfo.init();
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

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        ope.addPrePhysicsCopyListener(ee);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window

        hre.loadCheckerBoardGround("ground", 0.5f, 0f);
        hre.setBackground(0.5f, 0.5f, 0.5f);

        hre.setViewPoint(new float[] { -0.788f, 1.7f, 0.048f });
        hre.setViewRollPitchYawDegrees(0, 0, 83.5f);
        hre.setLightPosition(0, 1.90f, 2.57f, 1.53f);
        hre.setLightPosition(1, -3.29f, 2.07f, 2.48f);
        hre.setLightPosition(2, -2.38f, 2.63f, -0.34f);

        AsapVirtualHuman vh1 = ee.loadVirtualHuman("humanoid1", "", spec1, "AsapRealizer demo");
        realizerPort1 = vh1.getRealizerPort();
        realizerPort1.addListeners(new BMLFeedbackListener()
        {
            @Override
            public void feedback(String feedback)
            {
                try
                {
                    BMLFeedback fb = BMLFeedbackParser.parseFeedback(feedback);
                    if (fb instanceof BMLBlockProgressFeedback)
                    {
                        BMLBlockProgressFeedback bpf = (BMLBlockProgressFeedback) fb;
                        if (bpf.getBmlId().equals("bmlwhats") && bpf.getSyncId().equals("end"))
                        {
                            realizerPort2.performBML(readBMLFile("nothing.xml"));
                        }
                        if (bpf.getBmlId().equals("bmlhappy") && bpf.getSyncId().equals("end"))
                        {
                            realizerPort2.performBML(readBMLFile("soundhappyq.xml"));
                        }
                        if (bpf.getBmlId().equals("yeah") && bpf.getSyncId().equals("end"))
                        {
                            realizerPort2.performBML(readBMLFile("no1.xml"));
                        }
                        if (bpf.getBmlId().equals("noq") && bpf.getSyncId().equals("end"))
                        {
                            realizerPort2.performBML(readBMLFile("no2.xml"));
                        }
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
        realizerPort1.performBML(readBMLFile("blink_and_breathe.xml"));

        AsapVirtualHuman vh2 = ee.loadVirtualHuman("humanoid2", "", spec2, "AsapRealizer demo");
        realizerPort2 = vh2.getRealizerPort();
        realizerPort2.addListeners(new BMLFeedbackListener()
        {
            @Override
            public void feedback(String feedback)
            {
                try
                {
                    BMLFeedback fb = BMLFeedbackParser.parseFeedback(feedback);
                    if (fb instanceof BMLSyncPointProgressFeedback)
                    {
                        BMLSyncPointProgressFeedback spp = (BMLSyncPointProgressFeedback) fb;
                        if (spp.getBMLId().equals("soundhappyq") && spp.getBehaviourId().equals("speech1")
                                && spp.getSyncId().equals("happy"))
                        {
                            realizerPort1.performBML(readBMLFile("yeah.xml"));
                        }
                    }
                    if (fb instanceof BMLBlockProgressFeedback)
                    {
                        BMLBlockProgressFeedback bpf = (BMLBlockProgressFeedback) fb;
                        if (bpf.getBmlId().equals("no1") && bpf.getSyncId().equals("end"))
                        {
                            realizerPort1.performBML(readBMLFile("noq.xml"));
                        }
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
        realizerPort2.performBML(readBMLFile("blink_and_breathe.xml"));

        try
        {
            SwingUtilities.invokeAndWait(() -> initUI(j, canvas));
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        
        mainJFrame.setVisible(true);
    }

    private void initUI(JFrame j, java.awt.Component canvas)
    {
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                realizerPort1.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"clear\" composition=\"REPLACE\"/>");
                realizerPort2.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"clear\" composition=\"REPLACE\"/>");
                realizerPort1.performBML(readBMLFile("whatsthematter.xml"));
                realizerPort1.performBML(readBMLFile("soundhappy.xml"));
            }
        });
        JMenuBar menubar = new JMenuBar();
        menubar.add(playButton);
        j.setJMenuBar(menubar);
        mainJFrame.add(canvas);
    }

    public String readBMLFile(String filename)
    {
        Resources res = new Resources("twocharacters");
        BufferedReader reader = res.getReader(filename);
        StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        try
        {
            while ((numRead = reader.read(buf)) != -1)
            {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        return fileData.toString();
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    /**
     * Start the ElckerlycEnvironment prog
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        AsapRealizerDemoTurnFight demo = new AsapRealizerDemoTurnFight(new JFrame("AsapRealizer turnfight"),
                "twocharacters/asaparmandia_prudence.xml", "twocharacters/asaparmandia_poppy.xml");
        demo.startClocks();
    }
}
