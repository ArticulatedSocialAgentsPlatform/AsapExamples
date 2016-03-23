package asap.realizerdemo.newanimations;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.faceengine.loader.FaceEngineLoader;
import asap.faceengine.loader.VisemeBindingLoader;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.LipSynchProviderLoader;

public class SinusMoveLipSyncProviderLoader  implements LipSynchProviderLoader
{
    private String id;
    private LipSynchProvider lipSyncProvider;
    
    @Override
    public String getId()
    {
        return id;
    }
    
    public void setId(String newId)
    {
        id = newId;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);

        FaceEngineLoader fal = ArrayUtils.getFirstClassOfType(requiredLoaders, FaceEngineLoader.class);
        if (fal == null)
        {
            throw tokenizer.getXMLScanException("SinusMoveLipSyncProviderLoader requires FaceEngineLoader.");
        }
        
        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if (are == null)
        {
            throw new RuntimeException(
                    "SinusMoveLipSyncProviderLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        
        MorphVisemeBinding visBinding = VisemeBindingLoader.loadMorphVisemeBinding(tokenizer);

        if (visBinding == null)
        {
            throw tokenizer.getXMLScanException("SinusMoveLipSyncProviderLoader requires a visimebinding.");
        }

        lipSyncProvider = new SinusMoveLipSyncProvider(visBinding, fal.getFaceController(), fal.getPlanManager(), are.getPegBoard());
    }

    @Override
    public void unload()
    {
        
        
    }

    @Override
    public LipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
    }
    
}
