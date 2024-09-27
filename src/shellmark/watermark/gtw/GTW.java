package shellmark.watermark.gtw;

public class GTW extends shellmark.watermark.StaticWatermarker {
	   public static boolean DEBUG = false;
	   public String getShortName() {
	      return "Graph Theoretic Watermark";
	   }
	   public String getLongName() {
	      return "Venkatesan's Graph Theoretic Watermarking Algorithm";
	   }
	   private shellmark.util.ConfigProperties mConfigProps;
	   public shellmark.util.ConfigProperties getConfigProperties() {
	      if(mConfigProps == null) {
		      String props[][] = new String[][] {
	            /*
	              {"GTW_GRAPHCODEC","PermutationGraph",
	              "What graph codec to use to go from " +
	              "watermark number to CFG and back",
	              "true","S",},
	              {"GTW_CFS","ControlFlowSynthesizer",
	              "What CFS to use","true","S",},
	              {"GTW_BBM","ParityBlockMarker",
	              "What block marker to use","true","S",},
	              {"GTW_VALUESPLITTER","Scientific Notation",
	              "What value splitter to use","true","S",},
	            */
	            {"Key File","gtw.key",
	             "File in which to store the encryption key",null,"F","SE,SR"},
	            {"Use CRT Splitter", "true", "Use the Chinese Remainder Theorem Splitter Algorithm", 
	             null, "B", "SE,SR"},

	            {"Debug","false","Output Debugging Info",null,"B",},
	            {"Dump Dot Graphs","false","Dump DOT graphs?",null,"B",},
		      };	      
	         mConfigProps = new shellmark.util.ConfigProperties(props,null);
	      }
	      return mConfigProps;
	   }

	   public String getAlgHTML() {
	      return "<HTML><BODY>Graph Theoretic Watermark is an implementation of Venkatesan's Software Watermarking Algorithm " +
	         "described in <a href=\"http://www.cc.gatech.edu/fac/Vijay.Vazirani/water.ps\"> " +
	         "A Graph Theoretic Approach to Software Watermarking </a>. "+
	         " The watermark is embedded in the control flow within the" +
	         " program." +
	         "<TABLE>" +
	         "<TR><TD>" +
	         "Authors: <a href=\"mailto:ecarter@cs.arizona.edu\">Ed Carter</a>,"+ 
	         " <a href=\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</a>" +
	         " and <a href=\"mailto:gmt@cs.arizona.edu\">Gregg Townsend</a>" +
	         "</TR></TD>" +
	         "</TABLE>" +
	         "</BODY></HTML>";
	   }

	   public String getAlgURL() {
	      return "shellmark/watermark/gtw/doc/help.html";
	   }

	   public String getDescription() {
	      return "Venkatesan's Graph Theoretic Watermarking Algorithm embeds the " +
	         "watermark in control flow graph within the program.";
	   }
	   public String[] getReferences() {
	      return new String[] {};
	   }
	   public shellmark.config.ModificationProperty[] getMutations() {
	      return null;
	   }
	   public String getAuthor() {
	      return "Ed Carter, Andrew Huntwork, and Gregg Townsend";
	   }
	   public String getAuthorEmail() {
	      return "{ecarter,ash,gmt}@cs.arizona.edu";
	   }
	//这是主要部分！
	//从ShellMarkCLI的embed中来
	   public void embed(shellmark.watermark.StaticEmbedParameters params)
	      throws shellmark.watermark.WatermarkingException {

	      boolean useCRT = getConfigProperties().getProperty("Use CRT Splitter").equals("true");//是否使用CRT

	      boolean dumpDots = getConfigProperties().getProperty("Dump Dot Graphs").equals  //是否输出CFG的DOT
	         ("true");
	      {
	         if(dumpDots) {
	            System.out.println("dumping cfgs");
	            int i = 0;
	            for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
	               shellmark.program.Class clazz = (shellmark.program.Class)classIt.next();

	               for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; i++) {
	                  shellmark.program.Method method = (shellmark.program.Method)methodIt.next();
	                  if(method.getInstructionList() == null)
	                     continue;
	                  shellmark.util.newgraph.Graphs.dotInFile(method.getCFG(),
	                                                          "graphs/cfg." + i + "." + method.getName() + ".dot");
	                  System.out.println("   cfg " + i);
	               }
	            }
	         }
	         int i = 0;
	         //生成CFG的主要代码
	         //遍历APP的类和方法，并生成每个方法的CFG，将生成的CFG添加到GraphList中
	         //params.app表示需要嵌入水印的应用程序
	         for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
	            shellmark.program.Class clazz = (shellmark.program.Class)classIt.next();
			
	            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; i++) {
	               shellmark.program.Method method = (shellmark.program.Method)methodIt.next();
	               if(method.getInstructionList() == null)//检查当前方法的指令列表
	                  continue;
	               shellmark.util.graph.graphview.GraphList.instance().add(method.getCFG(),
	                                                                      "cfg." + i);
	            }
	         }
	      }

	      {
	         boolean debugProp = getConfigProperties().getProperty("Debug").equals("true");
	         DEBUG = debugProp;
	         GTWRecognizer.DEBUG = debugProp;
	         ClusterGraph.DEBUG = debugProp;
	         FunctionClusterGraph.DEBUG = debugProp;
	         shellmark.analysis.controlflowgraph.NullNENullCallGenerator.DEBUG = debugProp;
	         shellmark.analysis.controlflowgraph.CallingCallGenerator.DEBUG = debugProp;
	      }

	      //从params.key中生成密钥
	      java.math.BigInteger key = new java.math.BigInteger
	         (params.key == null || 
	          params.key.equals("") ? "0" : params.key);
	      //methodMarkValue：用于存储每个方法与其对应的水印标记值的映射
	      java.util.Hashtable methodMarkValue = 
	         new java.util.Hashtable();
	      //programMethodCFGs：用于存储程序中每个方法的控制流图（CFG）
	      java.util.ArrayList programMethodCFGs = 
	         new java.util.ArrayList();
	      //用于存储与水印方法相关的控制流图（CFG）
	      java.util.ArrayList wmMethodCFGs =
	         new java.util.ArrayList();
	      //生成一个随机数生成器 rnd，并将其种子设置为上面生成的 key 的长整数值
	      java.util.Random rnd = shellmark.util.Random.getRandom();
	      rnd.setSeed(key.longValue());
	      //遍历程序中的类和方法  重点检查的部分！！！
	      for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
	         shellmark.program.Class clazz = (shellmark.program.Class)classIt.next();
	            
	         for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
	            shellmark.program.Method method = (shellmark.program.Method)methodIt.next();
	            if(method.getInstructionList() == null || 
	               method.getInstructionList().getLength() == 0)
	               continue;
	            programMethodCFGs.add(method.getCFG());//出错！！！

	            if (!useCRT){
	               methodMarkValue.put(method,new Integer(0));
	            }
	         }
	      }
	      java.math.BigInteger wmark = 
	         new java.math.BigInteger(params.watermark);
	      if(DEBUG)
	         System.out.println("wmark: " + wmark);
	      int splitParts = 5 + (((rnd.nextInt() % 10) + 10) % 10);
	      
	      java.math.BigInteger splitWM[] = null;


	      if (useCRT){
	         try {
	            javax.crypto.KeyGenerator kg = 
	               javax.crypto.KeyGenerator.getInstance
	               (shellmark.util.splitint.CRTSplitter.getAlgorithm());
	            javax.crypto.SecretKey w = kg.generateKey();
	            java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream
	               (new java.io.FileOutputStream(getConfigProperties().getProperty("Key File")));
	            oo.writeObject(w);
	            oo.close();
	            splitWM = 
	               new shellmark.util.splitint.SlowCRTSplitter(128,50,w).split
	               (wmark);
	         } catch(java.security.NoSuchAlgorithmException e) {
	            throw new RuntimeException("crt splitter uses a bad alg");
	         } catch(java.io.IOException e) {
	            throw new RuntimeException();
	         }
	      }else{
	         splitWM = (new shellmark.util.splitint.PartialSumSplitter()).split
	            (wmark, splitParts);
	      }


	      if(DEBUG) {
	         System.out.print("wm parts: ");
	         for(int i = 0 ; i < splitWM.length ; i++)
	            System.out.print(splitWM[i] + " ");
	         System.out.println();
	      }

	      shellmark.program.Class wmClazz = 
	         new shellmark.program.LocalClass
	         (params.app,"watermark", "java.lang.Object", "watermark", 
	          org.apache.bcel.Constants.ACC_PUBLIC
	          | org.apache.bcel.Constants.ACC_SUPER,
	          null);
	      wmClazz.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);
	      {
	         shellmark.analysis.controlflowgraph.ControlFlowSynthesizer cfs =
	            new shellmark.analysis.controlflowgraph.PositiveIntSynthesizer();
	         shellmark.util.newgraph.codec.GraphCodec codec =
	            new shellmark.util.newgraph.codec.ReduciblePermutationGraph();
	         for(int i = 0 ; i < splitWM.length ; i++) {
	            shellmark.util.newgraph.Graph g = codec.encode(splitWM[i]);
	            if(dumpDots) {
	               String filename = "graphs/spg." + splitWM[i] + ".dot";
	               shellmark.util.newgraph.Graphs.dotInFile(g, filename);
	            }
	            shellmark.util.graph.graphview.GraphList.instance().add(g, "spg." + splitWM[i]);
	            shellmark.program.Method wmMethod =
	               cfs.generate(g, wmClazz);
	            wmMethod.setName("m" + i);
	            if(DEBUG)
	               System.out.println(wmMethod.getName());
	            if(dumpDots) {
	               String filename = "graphs/cfg.spg." + splitWM[i] + "." + wmMethod.getName() + ".dot";
	               shellmark.util.newgraph.Graphs.dotInFile(wmMethod.getCFG(),
	                                                       filename);
	            }
	            shellmark.util.graph.graphview.GraphList.instance().add(wmMethod.getCFG(),
	                                                                   "cfg.spg." + splitWM[i]);
	            wmMethodCFGs.add(wmMethod.getCFG());

	            if (!useCRT){
	               methodMarkValue.put(wmMethod,new Integer(1));
	            }
	         }
	      }
	      {
	         if(DEBUG)
	            System.out.println("building pcfg");
	         java.util.ArrayList allCFGs = new java.util.ArrayList(programMethodCFGs);
	         allCFGs.addAll(wmMethodCFGs);
	         shellmark.analysis.controlflowgraph.ProgramCFG pCFG =
	            new shellmark.analysis.controlflowgraph.ProgramCFG(allCFGs);
	         if(dumpDots) {
	            if(DEBUG)
	               System.out.println("dotting");
	            shellmark.util.newgraph.Graphs.dotInFile(pCFG,
	                                                    "graphs/pcfg.dot");
	         }
	         shellmark.util.graph.graphview.GraphList.instance().add(pCFG, "pcfg");
	         if(DEBUG)
	            System.out.println("done building pcfg");
	         ClusterGraph cg = 
	            new FunctionClusterGraph(pCFG);
	         if(dumpDots)
	            shellmark.util.newgraph.Graphs.dotInFile(cg,
	                                                    "graphs/fcg.orig.dot");
	         shellmark.util.graph.graphview.GraphList.instance().add(cg, "fcg.orig");
	         if(DEBUG)
	            System.out.println("done building cg");
	         double randomEdgeCount = 
	            getNumberOfEdgesToAdd(cg,programMethodCFGs,wmMethodCFGs,rnd);
	         if(randomEdgeCount < 1.0)
	            randomEdgeCount = 1.0;
	         if(DEBUG)
	            System.out.println("adding " + randomEdgeCount + " edges");
	         cg.randomlyWalkAddingEdges(programMethodCFGs, wmMethodCFGs,
	                                    (new Double(randomEdgeCount)).intValue());
	         if(dumpDots)
	            shellmark.util.newgraph.Graphs.dotInFile(cg,
	                                                    "graphs/fcg.munged.dot");
	         shellmark.util.graph.graphview.GraphList.instance().add(cg, "fcg.munged");
	      }
	      for(java.util.Iterator it = programMethodCFGs.iterator() ; 
	          it.hasNext() ; ) {
	         shellmark.analysis.controlflowgraph.MethodCFG cfg = 
	            (shellmark.analysis.controlflowgraph.MethodCFG)it.next();
	         cfg.rewriteInstructionList();
	         cfg.method().removeNOPs();
	         cfg.method().setMaxStack();
	         cfg.method().mark();
	         cfg.method().removeLocalVariables();
	         cfg.method().removeLineNumbers();
	      }
	      for(java.util.Iterator it = wmMethodCFGs.iterator() ; 
	          it.hasNext() ; ) {
	         shellmark.analysis.controlflowgraph.MethodCFG cfg = 
	            (shellmark.analysis.controlflowgraph.MethodCFG)it.next();
	         cfg.rewriteInstructionList();
	         cfg.method().removeNOPs();
	         cfg.method().setMaxStack();
	         cfg.method().mark();
	         cfg.method().removeLocalVariables();
	         cfg.method().removeLineNumbers();
	      }
	      programMethodCFGs = wmMethodCFGs = null;


	      if (!useCRT){
	         shellmark.watermark.util.BasicBlockMarker bm =
	            new shellmark.watermark.util.MD5Marker(wmClazz,2,key);
	         shellmark.watermark.util.MethodMarker mm =
	            new shellmark.watermark.util.EveryBlockMarker(bm);
	         for(java.util.Iterator methodIt = methodMarkValue.keySet().iterator() ;
	             methodIt.hasNext() ; ) {
	            shellmark.program.Method method = (shellmark.program.Method)methodIt.next();
	            
	            if(method.getInstructionList() == null) {
	               if(DEBUG)
	                  System.out.println("not embedding in abstract method " +
	                                     method.getName());
	               continue;
	            }
	            
	            int value = ((Integer)methodMarkValue.get(method)).intValue();
	            if(DEBUG)
	               System.out.println("value for " + method.getName() + " is " + value);
	            mm.embed(method, value);
	         }
	      }

	      if(dumpDots) {
	         int i = 0;
	         for(java.util.Iterator classes = params.app.classes() ; classes.hasNext() ; ) {
	            shellmark.program.Class clazz = (shellmark.program.Class)classes.next();
	            for(java.util.Iterator methods = clazz.methods() ; methods.hasNext() ; i++) {
	               shellmark.program.Method m = (shellmark.program.Method)methods.next();
	               shellmark.util.newgraph.Graphs.dotInFile
	                  (m.getCFG(),"graphs/cfg.final." + i + "." + m.getName() + ".dot");
	            }
	         }
	      }
	   }

	       
	   public java.util.Iterator recognize(shellmark.watermark.StaticRecognizeParameters params) {
	      {
	         boolean debugProp = getConfigProperties().getProperty("Debug").equals("true");
	         DEBUG = debugProp;
	         GTWRecognizer.DEBUG = debugProp;
	         ClusterGraph.DEBUG = debugProp;
	         FunctionClusterGraph.DEBUG = debugProp;
	         shellmark.analysis.controlflowgraph.NullNENullCallGenerator.DEBUG = debugProp;
	         shellmark.analysis.controlflowgraph.CallingCallGenerator.DEBUG = debugProp;
	      }
	      try {
	         shellmark.program.Application app = params.app;

	         java.math.BigInteger key = new java.math.BigInteger
	            (params.key == null || 
	             params.key.equals("") ? "0" : params.key);
	         GTWRecognizer gr = new GTWRecognizer
	            (app,
	             getConfigProperties(),   //.getProperty("Dump Dot Graphs").equals("true"),
	             key);
	         return gr;
	      } catch(Exception e) {
	         e.printStackTrace();
	         throw new RuntimeException(e);
	      }
	   }

	   public static void main(String argv[]) throws Exception {
	      shellmark.program.Application app = null;
	      app = new shellmark.program.Application(argv[0]);
	      int wmark = argv.length >=3 ? 
	         Integer.decode(argv[2]).intValue() : 17;
	      GTW me = new GTW();
	      shellmark.watermark.StaticWatermarker.getProperties().setProperty
	         ("Watermark",wmark + "");
	      me.embed(shellmark.watermark.StaticWatermarker.getEmbedParams(app));
	      java.util.Iterator it = 
	         me.recognize
	         (shellmark.watermark.StaticWatermarker.getRecognizeParams(app));
	      while(it.hasNext()) {
	         String wmarkStr =  (String)it.next();
	         System.out.println("possible watermark: " + wmarkStr);
	      }
	   }

	   private double getNumberOfEdgesToAdd
	      (ClusterGraph cg,java.util.ArrayList programCFGs,
	       java.util.ArrayList wmCFGs,java.util.Random rnd) {
	      double p = programCFGs.size();
	      double w = wmCFGs.size();
	      double e = cg.edgeCount();
	      double qdenom = p + w - 1.0;
	      double qp = (p - 1.0) / qdenom;
	      double qw = (w - 1.0) / qdenom;
	      double m = 4.0 * e * w * (1 - qw) * (1 - qp) /
	         (p * (2 - qw) * (1 - qp) - w * (2 - qp) * (1 - qw));
	      double randomFactor = w * rnd.nextGaussian() / 4;
	      return m + randomFactor;
	   }
	}

