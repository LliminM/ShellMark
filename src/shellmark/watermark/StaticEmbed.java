package shellmark.watermark;

public class StaticEmbed {
    public static void runEmbed(shellmark.Algorithm alg,
                                StaticEmbedParameters params) throws Exception {
       try {
	    ((StaticWatermarker)alg).embed(params);
          shellmark.util.Log.message(0,"Done embedding the watermark!");
	} catch (WatermarkingException e) {
         shellmark.util.Log.message(0,"Embedding failed: " + e);
       }
    }
} // class Embed
