package asap.realizerdemo.newanimations;

import hmi.faceanimation.FaceController;
import hmi.tts.TTSTiming;
import saiba.bml.core.Behaviour;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.faceengine.viseme.MorphVisemeDescription;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Moves the lips in a sinus wave-like manner
 * @author hvanwelbergen
 *
 */
public class SinusMoveLipSyncProvider implements LipSynchProvider
{
    private final MorphVisemeBinding visimeBinding;
    private final FaceController faceController;
    private final PlanManager<TimedFaceUnit> facePlanManager;
    private final PegBoard pegBoard;

    public SinusMoveLipSyncProvider(MorphVisemeBinding visBinding, FaceController fc, PlanManager<TimedFaceUnit> facePlanManager, PegBoard pb)
    {
        visimeBinding = visBinding;
        faceController = fc;
        pegBoard = pb;
        this.facePlanManager = facePlanManager;
    }

    @Override
    public void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, TTSTiming timing)
    {
        double duration = timing.getDuration();               
        //viseme 11 is A
        MorphVisemeDescription desc = visimeBinding.getgetMorphTargetForViseme(11);
        
        SinusWaveFU fu = new SinusWaveFU();
        fu.setTargets(desc.getMorphNames());
        fu = fu.copy(faceController, null, null, null);
        TimedFaceUnit tfu = fu.createTFU(NullFeedbackManager.getInstance(), bbPeg, speechUnit.getBMLId(), speechUnit.getId(), pegBoard);
        TimePeg startPeg = new OffsetPeg(speechUnit.getTimePeg("start"), 0);
        TimePeg endPeg = new OffsetPeg(speechUnit.getTimePeg("end"), duration);
        tfu.resolveFaceKeyPositions();
        tfu.setTimePeg("start",startPeg);
        tfu.setTimePeg("end",endPeg);
        facePlanManager.addPlanUnit(tfu);        
    }
}
