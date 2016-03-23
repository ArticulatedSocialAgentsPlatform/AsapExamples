package asap.realizerdemo.fluentinteraction;

import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Resources;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import saiba.bml.builder.BehaviourBlockBuilder;
import asap.bml.ext.bmla.builder.BMLABMLBehaviorAttributesBuilder;
import asap.bml.ext.bmla.builder.BMLAParameterValueChangeBehaviourBuilder;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerport.RealizerPort;
import asap.realizerport.util.StderrWarningListener;
import asap.realizerport.util.StdoutFeedbackListener;

/**
 * Illustrates the use of acoustic features for interruption through BML. Try to interrupt Armandia by pressing the spacebar.
 * @author herwinvw
 *
 */
public class TurnKeepDemo implements KeyListener
{
    public RealizerPort realizerPort;

    int paramnumber = 0;

    enum VolumeChange
    {
        NONE, UP, DOWN;
    }

    VolumeChange vc = VolumeChange.NONE;
    
    private HmiRenderEnvironment hre;
    private OdePhysicsEnvironment ope;
    
    public TurnKeepDemo(JFrame j) throws IOException
    {
        initRealizer();
        setupUI(j);
    }
    
    private void setupUI(JFrame j)
    {
        j.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent winEvt)
            {
                System.exit(0);
            }
        });
        
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                play();
            }
        });
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(playButton);
        j.setJMenuBar(menuBar);
        
        j.add(hre.getAWTComponent());
        hre.getAWTComponent().addKeyListener(this);
        j.setSize(new Dimension(1000,600));
        j.setVisible(true);
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
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");


        hre.init(); // canvas does not exist until init was called
        ope.init();
        mae.init(ope);
        aue.init();
        we.init();
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(we);
        environments.add(mae);
        environments.add(ee);
        environments.add(aue);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        ope.addPrePhysicsCopyListener(ee);
        
        
        AsapVirtualHuman vh = ee.loadVirtualHuman("humanoid", "", "asaparmandia_vp_nogui.xml", "AsapRealizer demo");
        realizerPort = vh.getRealizerPort();
        realizerPort.addListeners(new StderrWarningListener());
        realizerPort.addListeners(new StdoutFeedbackListener());
    }

    public void play()
    {
        realizerPort.performBML(BehaviourBlockBuilder.resetBlock().toBMLString());
        Resources res = new Resources("");
        try
        {
            realizerPort.performBML(res.read("fluentdemos/interrupt/nhm.xml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE)
        {
            if (vc != VolumeChange.DOWN)
            {
                paramnumber++;
                realizerPort.performBML(getVolumeSetBML(15));
                vc = VolumeChange.DOWN;
            }

        }
    }

    private String getVolumeSetBML(int volume)
    {
        String bmlString = new BehaviourBlockBuilder()
                            .id("bmlparam"+paramnumber)
                            .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                                .addToInterrupt("bmlparam"+(paramnumber - 1))
                                .build())
                            .addBehaviour(new BMLAParameterValueChangeBehaviourBuilder("bmlparam"+paramnumber, "pvc1", "bml1:welkom", "volume")
                                    .trajectory("instant", ""+volume)
                                    .build())    
                            .build()
                            .toBMLString();
        System.out.println(bmlString);
        return bmlString;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE)
        {
            if (vc != VolumeChange.UP)
            {
                paramnumber++;
                realizerPort.performBML(getVolumeSetBML(100));
                vc = VolumeChange.UP;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent arg)
    {
        //unused
    }

    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }
    
    public static void main(String args[]) throws HeadlessException, IOException
    {
        TurnKeepDemo tkd = new TurnKeepDemo(new JFrame());
        tkd.startClocks();
    }
}
