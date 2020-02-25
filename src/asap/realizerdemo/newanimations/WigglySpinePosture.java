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
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.util.Resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.PostureShiftTMU;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.animationengine.transitions.TransitionMU;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;

public class WigglySpinePosture implements RestPose
{
    private AnimationPlayer aniPlayer;
    private double startTime = 0;
    private static final String wigglyJointId = Hanim.vt1;
    private VJoint restPoseTree; // Holds the pose on a VJoint structure. Joints not in the pose are set to have identity rotation.
    private float amount = 10;
    
    @Override
    public RestPose copy(AnimationPlayer player)
    {
        WigglySpinePosture copy = new WigglySpinePosture();
        copy.setAnimationPlayer(player);
        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.aniPlayer = player;
        restPoseTree = player.getVCurr().copyTree("rest-");
        for (VJoint vj : restPoseTree.getParts())
        {
            vj.setRotation(Quat4f.getIdentity());
        }
    }

    @Override
    public void setResource(Resources res)
    {

    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (!kinematicJoints.contains(wigglyJointId))
        {
            double t = time - startTime;
            VJoint vj = aniPlayer.getVNext().getPart(wigglyJointId);
            float q[] = Quat4f.getQuat4f();
            Quat4f.setFromAxisAngleDegrees(q, 0, 0, 1, (float) Math.sin(t * 10) * amount);
            vj.setRotation(q);
        }
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        return createTransitionToRest(fbm, joints, startTime, 1, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        return createTransitionToRest(fbm, joints, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbm, bmlBlockPeg, bmlId, id, mu, pb, aniPlayer);
        tmu.setTimePeg("start", startPeg);
        tmu.setTimePeg("end", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        return 1;
    }

    @Override
    public TransitionMU createTransitionToRest(Set<String> joints)
    {
        float rotations[] = new float[joints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : joints)
        {
            VJoint vj = restPoseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            targetJoints.add(aniPlayer.getVNextPartBySid(joint));
            startJoints.add(aniPlayer.getVCurrPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }
    
    @Override
    public AnimationUnit createTransitionToRestFromVJoints(Collection<VJoint> joints)
    {
        float rotations[] = new float[joints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (VJoint joint : joints)
        {
            VJoint vj = restPoseTree.getPartBySid(joint.getSid());
            vj.getRotation(rotations, i);
            targetJoints.add(joint);
            startJoints.add(aniPlayer.getVCurrPartBySid(joint.getSid()));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }
    
    @Override
    public void initialRestPose(double time)
    {
        startTime = time;
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if(name.equals("amount"))
        {
            this.amount = Float.parseFloat(value);
        }
    }

    @Override
    public PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        targetJoints.add(aniPlayer.getVNextPartBySid(wigglyJointId));
        startJoints.add(aniPlayer.getVCurrPartBySid(wigglyJointId));
        AnimationUnit mu = new SlerpTransitionToPoseMU(startJoints, targetJoints, Quat4f.getIdentity());
        return new PostureShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(aniPlayer), pb, this, aniPlayer);        
    }

    @Override
    public void start(double time)
    {
                
    }

    
}
