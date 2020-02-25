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

import hmi.animation.Hanim;
import hmi.math.Quat4f;
import hmi.util.StringUtil;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
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

public class HeadSpinMU implements AnimationUnit
{
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private AnimationPlayer aniPlayer;
    private int repeats = 1;

    enum SpinDirection
    {
        LEFT, RIGHT
    };

    private SpinDirection spinDirection = SpinDirection.LEFT;

    @Override
    public void play(double t) throws MUPlayException
    {
        float q[] = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(q, 0, 1, 0, (spinDirection == SpinDirection.LEFT ? -1 : 1) * (float) (t * repeats * 2 * Math.PI));
        aniPlayer.getVNext().getPart(Hanim.skullbase).setRotation(q);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        // TODO connect smoothly to previous motion
    }

    @Override
    public double getPreferedDuration()
    {
        return 1;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("r"))
        {
            repeats = (int) value;
        }
        else
        {
            throw new InvalidParameterException(name, "" + value);
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("spindirection"))
        {
            spinDirection = SpinDirection.valueOf(value);
        }
        else if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }

        else
        {
            throw new InvalidParameterException(name, value);
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("spindirection"))
        {
            return spinDirection.toString();
        }
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        if (name.equals("r"))
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
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.of(Hanim.skullbase);
    }

    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationMotionUnit(bbm, bmlBlockPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p) throws MUSetupException
    {
        HeadSpinMU mu = new HeadSpinMU();
        mu.repeats = repeats;
        mu.aniPlayer = p;
        return mu;
    }

}
