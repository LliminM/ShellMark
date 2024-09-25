package shellmark;

public class ShellMarkCLI {
	private static boolean DEBUG = false;
	private ShellMarkCLI() {}
	
	private static String optSpec[] = new String[] {
	   "-U","usage",
	   "-H algname", "algorithm description",
	   "-E","embed a watermark",
	   "-R","recognize a watermark",
	   "-A algname","the algorithm name",
	   "-i input","the input file",
	   "-o output","the output file",
	   "-w watermark","the watermark",
	   "-k key","the key",
	   "-t tracefile","the trace file",
	   "-c classpath","class path additions",
	   "-m mainclass","the main class",
	   "-a arguments","arguments to the program",
	   "-d property_values","extra algorithm parameters",
	};
	
	public static void main(String[] args) {
	   shellmark.util.Options opts =
			   new shellmark.util.Options(optSpec,"ShellMarkCLI",args);
	   
	   String options = opts.getWhich();
	   String actions = "[UHEROSDFT]";
	   java.util.regex.Pattern actionPat =
			   java.util.regex.Pattern.compile(actions);
	   java.util.regex.Matcher matcher = actionPat.matcher(options);
	   if(!matcher.find()) {
		   opts.usage(System.out, "ShellMarkCLI");
		   System.exit(1);
	   }
	   int action = matcher.group().charAt(0);
	   
	   try {
		   switch(action) {
		   case 'E':
			   doEmbed(opts);
			   break;
		   case 'R':
			   doRecognize(opts);
			   break;
		   case 'H':
	           showAlgHelp(opts.getValue('H'));
	           break;
		   case 'U':
	           opts.usage(System.out,"ShellMarkCLI");
	           break;
		   default:
	           opts.usage(System.out,"ShellMarkCLI");
	        System.exit(1);
		   }
	   } catch(Throwable t) {
		   System.err.println("ERROR: " + t);
	        t.printStackTrace();
	   }
	   
	}
	
	private static void doEmbed(shellmark.util.Options opts) throws Exception{
		shellmark.Algorithm alg = getAlg(opts);
		if(alg == null ||!(alg instanceof shellmark.watermark.GeneralWatermarker)) {
			System.out.println("Please specify a Watermarker");
	        System.exit(1);
		}
		
		setExtraProperties(alg,opts);
	     
	     shellmark.program.Application app;
	     java.io.File outputFile;
	     try {
	        String inputFileName = opts.getValue('i');
	        app = new shellmark.program.Application(inputFileName);
	        String outputFileName = opts.getValue('o');
	        outputFile = new java.io.File(outputFileName);
	     } catch(Exception e) {
	        System.out.println("Please specify an input and output file.");
	        System.exit(1);
	        return;
	     }
	     
	     String wm = opts.getValue('w');
	     if(wm == null) {
	        System.out.println("Please specify a watermark.");
	        System.exit(1);
	     }
	     
	     //if(alg instanceof shellmark.watermark.DynamicWatermarker)
	        //doDynamicEmbed((shellmark.watermark.DynamicWatermarker)alg,
	                       //app,outputFile,wm,opts);
	     //else 
	     if(alg instanceof shellmark.watermark.StaticWatermarker)
	        doStaticEmbed((shellmark.watermark.StaticWatermarker)alg,
	                      app,outputFile,wm,opts);
	     else
	        assert false : alg.getClass();
		
	}
	
	//public static void doDynamicEmbed(shellmark.watermark.DynamicWatermarker alg,shellmark.program.Application app,
		      //java.io.File output,String wm,shellmark.util.Options opts) {}
	
	private static void doStaticEmbed
    	(shellmark.watermark.StaticWatermarker alg,shellmark.program.Application app,
    	java.io.File output,String wm,shellmark.util.Options opts) 
    	throws shellmark.watermark.WatermarkingException {
        shellmark.util.ConfigProperties cp =
            shellmark.watermark.StaticWatermarker.getProperties();
    
        cp.setValue("Watermark",wm);
    
        String key = opts.getValue('k');
        cp.setValue("Key",key);
    
        shellmark.watermark.StaticEmbedParameters params = 
            shellmark.watermark.StaticWatermarker.getEmbedParams(app);
    
        alg.embed(params);
    
        try {
        	app.save(output);
        } catch(Exception e) {
        	System.out.println("Please specify a valid output file.");
        	System.exit(1);
        }
	}
	
	private static void doRecognize(shellmark.util.Options opts) throws Exception {
		shellmark.Algorithm alg = getAlg(opts);
	     if(alg == null || 
	        !(alg instanceof shellmark.watermark.GeneralWatermarker)) {
	        System.out.println("Please specify a Watermarker");
	        System.exit(1);
	     }
	     
	     setExtraProperties(alg,opts);
	     
	     shellmark.program.Application app;
	     try {
	        String inputFileName = opts.getValue('i');
	        app = new shellmark.program.Application(inputFileName);
	     } catch(Exception e) {
	        System.out.println("Please specify an input file.");
	        System.exit(1);
	        return;
	     }
	     
	     //if(alg instanceof shellmark.watermark.DynamicWatermarker)
	        //doDynamicRecognize((shellmark.watermark.DynamicWatermarker)alg,
	                           //app,opts);
	     //else 
	     if(alg instanceof shellmark.watermark.StaticWatermarker)
	        doStaticRecognize((shellmark.watermark.StaticWatermarker)alg,
	                          app,opts);
	     else
	        assert false : alg.getClass();
		
	}
	
	public static void doStaticRecognize(shellmark.watermark.StaticWatermarker alg,
		      shellmark.program.Application app,shellmark.util.Options opts) throws Exception {
		shellmark.util.ConfigProperties cp = shellmark.watermark.StaticWatermarker.getProperties();
		
		String key = opts.getValue('k'); 
		cp.setValue("Key",key);
		
		shellmark.watermark.StaticRecognizeParameters params =
		        shellmark.watermark.StaticWatermarker.getRecognizeParams(app);
		
		for(java.util.Iterator it = alg.recognize(params) ; it.hasNext() ; )
	        System.out.println(it.next());
	}
	
	private static void setExtraProperties(shellmark.Algorithm alg,
            shellmark.util.Options opts) {
		String props = opts.getValue('d');
	     if(props == null || props.equals(""))
	        return;
	     
	     String propVals[] = props.split("[,]");

	     for(int i = 0 ; i < propVals.length ; i++) {
	        String pv = propVals[i];
	        String split_pv[] = pv.split("=");
	        if(split_pv.length != 2 || split_pv[0].equals(""))
	           continue;
	        try {
	           alg.getConfigProperties().setProperty(split_pv[0],split_pv[1]);
	        } catch(Exception e) {
	           //getCP may return null, or the specified property may 
	           //not apply to this Alg
	        }
	     }
	}
	
	private static shellmark.Algorithm getAlg(shellmark.util.Options opts){
		String algname = opts.getValue('A');
	     if(algname == null || algname.equals(""))
	        return null;
	     
	     return getAlg(algname);
	}
	
	private static shellmark.Algorithm getAlg(String algname) {
		String className = 
		        shellmark.util.classloading.ClassFinder.getClassByShortname(algname);
		     if(className == null)
		        return null;
		     try {
		    	 return (shellmark.Algorithm) Class.forName(className).getDeclaredConstructor().newInstance();
		     } catch(Exception e) {
		        return null;
		     }
	}
	
	private static void showAlgHelp(String algName) {
		shellmark.Algorithm alg = getAlg(algName);
	     if(alg == null) {
	        System.out.println("Please specify an algorithm");
	        System.exit(1);
	     }
	     
	     System.out.println(alg.getDescription());
	}
}
