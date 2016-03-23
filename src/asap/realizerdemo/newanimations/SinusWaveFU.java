package asap.realizerdemo.newanimations;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.util.StringUtil;

import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

public class SinusWaveFU implements FaceUnit
{
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private FaceController faceController;
    private String targetName = "";
    private String[] morphTargets = new String[] { "" };
    private int repeats = 1;
    private float intensity = 1;
    
    private void updateMorphTargets()
    {
        morphTargets = targetName.split(",");
    }

    public void setTargets(Set<String> targets)
    {
        targetName = Joiner.on(",").join(targets);
        updateMorphTargets();
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        float newMorphedWeight = intensity*(float)Math.sin(t*repeats*2*Math.PI);
        float[] newWeights = new float[morphTargets.length];
        for (int i = 0; i < newWeights.length; i++)
            newWeights[i] = newMorphedWeight;
        faceController.addMorphTargets(morphTargets, newWeights);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    @Override
    public double getPreferedDuration()
    {
        return 1;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if(name.equals("intensity"))
        {
            intensity = value;            
        }
        else if(name.equals("repeats"))
        {
            repeats = (int)value;
        }
        else
        {
            throw new ParameterNotFoundException(name);
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("targetname"))
        {
            targetName = value;
            updateMorphTargets();
        }
        else
        {
            if (StringUtil.isNumeric(value))
            {
                setFloatParameterValue(name, Float.parseFloat(value));
            }
            else
            {
                throw new InvalidParameterException(name, value);
            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("targetname")) return "" + targetName;
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if(name.equals("intensity"))
        {
            return intensity;
        }
        else if(name.equals("repeats"))
        {
            return repeats;
        }
        throw new ParameterNotFoundException(name);        
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this, pb);
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public SinusWaveFU copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        SinusWaveFU fu = new SinusWaveFU();
        fu.setFaceController(fc);
        fu.targetName = targetName;
        fu.intensity = intensity;
        fu.repeats = repeats;
        for (KeyPosition keypos : getKeyPositions())
        {
            fu.addKeyPosition(keypos.deepCopy());
        }
        fu.updateMorphTargets();
        return fu;
    }

    @Override
    public void interruptFromHere()
    {
        // TODO Implement this
    }

}
