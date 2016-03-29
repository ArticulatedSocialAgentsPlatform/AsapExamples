package asap.realizerdemo.fluentinteraction;

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Console;
import hmi.util.Resources;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import saiba.bml.builder.BehaviourBlockBuilder;
import saiba.bml.feedback.BMLBlockProgressFeedback;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLFeedbackParser;
import asap.bml.ext.bmla.builder.BMLABMLBehaviorAttributesBuilder;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.bml.ext.bmlt.builder.BMLTAudioFileBehaviourBuilder;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.collect.ImmutableSet;


/**
 * Demo in which speech is temporarily interrupted for an announcement and then resumed. 
 * @author Herwin
 */
public class AsapRealizerDemoNoise implements BMLFeedbackListener
{
    private HmiRenderEnvironment hre;
    private OdePhysicsEnvironment ope;
    protected JFrame mainJFrame = null;
    private RealizerPort realizerPort;
        
    private final String CHUNK_CONTENT[] = { "Tomorrow at 10", "is the meeting with your brother", "and at two o'clock", "you'll go shopping",
            "and at eight", "is the gettogether", "in the bar" };

    private final String CHUNK_CONTINUER_CONTENT[] = { "So, tomorrow at 10", "so, then is the meeting with your brother",
            "and at two", "so, then you'll go shopping", "and at eight", "so, then is the gettogether",
            "that's in the bar" };
    private Set<String> finishedChunks = Collections.synchronizedSet(new HashSet<String>());
    private HashMap<Integer, String> chunkId = new HashMap<>();
    private int continueFrom = 0;
    private String pauseId;
    
    public AsapRealizerDemoNoise(JFrame j, String spec) throws IOException
    {
        System.setProperty("sun.java2d.noddraw", "true"); 
        Console.setEnabled(false);
        initAsapVH(spec);        
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    setupUI(j);
                }
            });
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void initAsapVH(String spec) throws IOException
    {
        BMLTInfo.init();        
        hre = new HmiRenderEnvironment();
        ope = new OdePhysicsEnvironment();        
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");
        
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

        hre.setViewPoint(new float[]{0.14291215f, 1.6600605f, 0.7f});
        hre.setViewRollPitchYawDegrees(0,0,-9.869118f);
 
        
        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(ee);
        hre.loadCheckerBoardGround("ground", 0.5f, 0f);
        hre.setBackground(0.5f, 0.5f, 0.5f);
        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new VJointWorldObject(camera));
        
        AsapVirtualHuman vh = ee.loadVirtualHuman("humanoid", "", spec, "AsapRealizer demo");
        realizerPort = vh.getRealizerPort();
        realizerPort.addListeners(this);
    }

    private void setupUI(JFrame j)
    {
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
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendBML();
            }
        });
        JButton aStartButton = new JButton("AnnounceStart");        
        aStartButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                coughStart();
            }
        });
        JButton aEndButton = new JButton("AnnounceEnd");
        aEndButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                coughEnd();
            }
        });
        JMenuBar menubar = new JMenuBar();
        menubar.add(playButton);
        menubar.add(aStartButton);
        menubar.add(aEndButton);
        mainJFrame.setJMenuBar(menubar);
        
        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainJFrame.add(canvas);
        canvas.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e)
            {
                                
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyChar()=='1')
                {
                    sendBML();
                }
                if(e.getKeyChar()=='2')
                {
                    coughStart();
                }
                if(e.getKeyChar()=='3')
                {
                    coughEnd();
                }                
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                                
            }            
        });
        mainJFrame.setVisible(true);
    }

    private void insertPause(int i)
    {
        pauseId = "pause" + UUID.randomUUID().toString().replaceAll("-", "");
        String bml = new BehaviourBlockBuilder()
            .id(pauseId)
            .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                    .addToChunkBefore(chunkId.get(i + 1))
                    .setPreplanned(true)
                    .build())
            .build()
            .toBMLString();
        realizerPort.performBML(bml);
    }

    private void continueFromPause(int i)
    {
        String continuerId = "continuer" + UUID.randomUUID().toString().replaceAll("-", "");
        finishedChunks.remove("bml" + (i + 1));
        String bml = new BehaviourBlockBuilder()
                        .id(continuerId)
                        .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                            .addToOnStart(pauseId)
                            .build())
                        .build()
                        .toBMLString();
        realizerPort.performBML(bml);
    }
    
    private void stopRequest(int i)
    {
        String bmlId = "stopReq" + UUID.randomUUID().toString().replaceAll("-", "");
        String bml = new BehaviourBlockBuilder()
            .id(bmlId)
            .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                .addToInterrupt(chunkId.get(i))
                .build())
            .addBehaviour(new BMLTAudioFileBehaviourBuilder(bmlId,"a1","announcement.wav").build())
            .build()
            .toBMLString();
        
        realizerPort.performBML(bml);
        try
        {
            realizerPort.performBML(new Resources("").read("fluentdemos/noise/gazeright.xml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void prependContinuerChunk(int i)
    {
        finishedChunks.remove("bml" + (i + 1));

        String bmlId = "bml" + (i + 1) + UUID.randomUUID().toString().replaceAll("-", "");
        chunkId.put(i, bmlId);

        if (i < CHUNK_CONTENT.length - 1)
        {
            realizerPort.performBML(constructPrependedBMLSpeechChunk(CHUNK_CONTINUER_CONTENT[i], bmlId, chunkId.get(i + 1)));
        }
        else
        {
            realizerPort.performBML(constructPreplannedBMLSpeechChunk(CHUNK_CONTINUER_CONTENT[i], bmlId));
        }
    }
    
    private void coughPlanInsertion()
    {
        ImmutableSet<String> allChunks = ImmutableSet.of("bml1", "bml2", "bml3", "bml4", "bml5", "bml6", "bml7");
        Set<String> remainingChunks = new HashSet<>(allChunks);
        remainingChunks.removeAll(finishedChunks);
        int start = CHUNK_CONTENT.length - remainingChunks.size();
        continueFrom = start;

        if (start < CHUNK_CONTENT.length - 1)
        {
            insertPause(start);
            stopRequest(start);
            prependContinuerChunk(start);
        }
        else if (start < CHUNK_CONTENT.length)
        {
            stopRequest(start);
            sendChunk(start);
        }
    }
    
    private void coughEnd()
    {
        if (continueFrom < CHUNK_CONTENT.length - 1)
        {
            continueFromPause(continueFrom);
        }
    }

    private void coughStart()
    {
        coughPlanInsertion();
    }
    
    private String constructPreplannedBMLSpeechChunk(String speechText, String bmlId)
    {
        return new BehaviourBlockBuilder()
            .id(bmlId)
            .addSpeechBehaviour("speech1", speechText)
            .build()
            .toBMLString();
    }
    
    private String constructPrependedBMLSpeechChunk(String speechText, String bmlId, String sucBMLId)
    {
        return new BehaviourBlockBuilder()
        .id(bmlId)
        .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                .addToPrependBefore(sucBMLId)
                .addToChunkAfter(pauseId)
                .build())
        .addSpeechBehaviour("speech1", speechText)
        .build()
        .toBMLString();
    }

    private String constructBMLSpeechChunk(String speechText, String bmlId, String predBMLId)
    {
        return new BehaviourBlockBuilder()
            .id(bmlId)
            .addBMLBehaviorAttributeExtension(new BMLABMLBehaviorAttributesBuilder()
                .addToChunkAfter(predBMLId)
                .build())
            .addSpeechBehaviour("speech1", speechText)
            .build()
            .toBMLString();
    }
    
    private void sendChunk(int i, String prevChunk)
    {
        finishedChunks.remove("bml" + (i + 1));
        chunkId.put(i, "bml" + (i + 1));
        realizerPort.performBML(constructBMLSpeechChunk(CHUNK_CONTENT[i], "bml" + (i + 1), prevChunk));
    }

    private void sendChunk(int i)
    {
        String prevChunk = "cleanup";
        if (i > 0) prevChunk = "bml" + i;
        sendChunk(i, prevChunk);
    }
    
    private void sendBML()
    {
        realizerPort.performBML(BehaviourBlockBuilder.resetBlock().toBMLString());
        finishedChunks.clear();
        
        try
        {
            realizerPort.performBML(new Resources("").read("fluentdemos/noise/blink_and_breathe.xml"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < CHUNK_CONTENT.length; i++)
        {
            sendChunk(i);
        }
    }
    
    public void startClocks()
    {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }
    
    @Override
    public void feedback(String feedback)
    {
        try
        {
            BMLFeedback fb = BMLFeedbackParser.parseFeedback(feedback);
            if (fb instanceof BMLBlockProgressFeedback)
            {
                BMLBlockProgressFeedback bpf = (BMLBlockProgressFeedback) fb;
                if (bpf.getSyncId().equals("end"))
                {
                    finishedChunks.add(bpf.getBmlId().substring(0, 4));
                    System.out.println("Finished now: " + bpf.getBmlId() + " finished: " + finishedChunks);                    
                }
                if (bpf.getSyncId().equals("start"))
                {
                    finishedChunks.remove(bpf.getBmlId().substring(0, 4));
                    System.out.println("Started: " + bpf.getBmlId() + " finished: " + finishedChunks);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws IOException
    {
        AsapRealizerDemoNoise demo = new AsapRealizerDemoNoise(new JFrame("AsapRealizer noise demo"), "asaparmandia_vp_nogui.xml");
        demo.startClocks();
    }
}
