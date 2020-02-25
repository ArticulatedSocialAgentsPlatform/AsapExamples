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
package asap.realizerdemo.newanimations;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACS2MorphConverter;
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
        // JK: Added phase shift to start with 0 and changed amplitude to between 0 and 1 instead of -1 and 1
        float newMorphedWeight = 0.5f+(0.5f*intensity)*(float)Math.sin(t*repeats*2*Math.PI+3*Math.PI/2.0);
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
    public SinusWaveFU copy(FaceController fc, FACSConverter fconv, EmotionConverter econv, FACS2MorphConverter f2mconv)
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
