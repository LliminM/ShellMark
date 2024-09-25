package shellmark.watermark;

public class StaticRecognize {
    public static java.util.Iterator runRecognition
	(shellmark.Algorithm alg,StaticRecognizeParameters params) throws java.io.IOException {
       try {
	    return ((StaticWatermarker)alg).recognize(params);
	} catch (WatermarkingException e) {
         shellmark.util.Log.message(0,"Recognition failed: " + e);
         e.printStackTrace();
       }
	return null;
    }
}
