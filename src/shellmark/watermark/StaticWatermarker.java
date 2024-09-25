package shellmark.watermark;

/**
 *  A StaticWatermarker object encapsulates code for running
 *  a particular static watermark algorithm.
 */

public abstract class StaticWatermarker 
  extends shellmark.watermark.GeneralWatermarker {
    
    /**
     * Embed a watermark value into the program. The props argument
     * holds at least the following properties:
     *  <UL>
     *     <LI> Watermark: The watermark value to be embedded.
     *     <LI> Key: The secret key.
     *  </UL>
     */
    public abstract void embed(StaticEmbedParameters params) 
	throws shellmark.watermark.WatermarkingException;
    

    
    /* Return an iterator which generates the watermarks
     * found in the program. The props argument
     * holds at least the following properties:
     *  <UL>
     *     <LI> Input File: The name of the file to be examined.
     *     <LI> Key: The secret key.
     *  </UL>
     */
    public abstract java.util.Iterator recognize
        (StaticRecognizeParameters params)
    throws shellmark.watermark.WatermarkingException;

    /*
     *  Get the GENERAL properties of StaticWatermark
     */
    private static shellmark.util.ConfigProperties sConfigProps;
    public static shellmark.util.ConfigProperties getProperties(){
        if(sConfigProps == null) {
            String[][] props = {
                {"Output File",
                 "",
                 "The output jar-file.",
                 null,"J","SE",
                },
		{"Watermark",
		 "",
		 "The watermark to be embedded.",
		 null,"S","SE",
		},
                {"Key",
                 "",
                 "The key used to embed or recognize a static watermark.",
                 null,"S","SE,SR",
                },
            };
            sConfigProps = 
                new shellmark.util.ConfigProperties
                (props,GeneralWatermarker.getProperties());
        }
        return sConfigProps;
    }
    
    public static StaticEmbedParameters getEmbedParams(shellmark.program.Application app) {
        StaticEmbedParameters sep = new StaticEmbedParameters();
        sep.app = app;
        sep.key = (String)getProperties().getValue("Key");
        sep.watermark = (String)getProperties().getValue("Watermark");
        return sep;
    }
    
    public static StaticRecognizeParameters getRecognizeParams(shellmark.program.Application app) {
        StaticRecognizeParameters srp = new StaticRecognizeParameters();
        srp.app = app;
        srp.key = (String)getProperties().getValue("Key");
        return srp;
    }

    public shellmark.util.ConfigProperties getConfigProperties() {
        return null;
    }

    /*
     *  Get the HTML codes of the About page for Static Watermarking
     */
    public static java.lang.String getAboutHTML(){
	return 
	    "<HTML><BODY>" +
	    "<CENTER><B>List of Static Watermarkers</B></CENTER>" +
	    "</BODY></HTML>";
    }

    /*
     *  Describe what static watermarking is.
     */
    public static java.lang.String getOverview(){
	return "A static watermarking algorithm embeds a " +
               "watermark in the code or data of the program itself.";
    }

    /*
     *  Get the URL of the Help page for Static Watermarking
     */
    public static java.lang.String getHelpURL(){
	return "shellmark/watermark/doc/watermarking.html";
    }
}

