package shellmark.watermark.gtw;

//用到的自定义类：shellmark.analysis.controlflowgraph.ProgramCFG

public class FunctionClusterGraph extends ClusterGraph {
  public static boolean DEBUG = false;
  private static final double WM_METHOD_REAL_CALL_THRESHHOLD = .2;
  public FunctionClusterGraph
	(shellmark.analysis.controlflowgraph.ProgramCFG pCFG) {
      buildClusters(pCFG);
      buildClusterEdges(pCFG);
  }
  private void buildClusters
      (shellmark.analysis.controlflowgraph.ProgramCFG pcfg) {
      for(java.util.Iterator cfgs = pcfg.nodes() ; cfgs.hasNext() ; )
          addNode(cfgs.next());
  }
  private void buildClusterEdges
      (shellmark.analysis.controlflowgraph.ProgramCFG pcfg) {
      for(java.util.Iterator edges = pcfg.edges() ; edges.hasNext() ; ) {
          shellmark.util.newgraph.Edge edge =
              (shellmark.util.newgraph.Edge)edges.next();
          addEdge(new shellmark.util.newgraph.EdgeImpl
                  (edge.sourceNode(),edge.sinkNode()));
      }
  }
  protected void synthesizeEdge
      (Object node1,int node1Type,Object node2,int node2Type) {
      Object from,to;
      if(!(node1 instanceof shellmark.analysis.controlflowgraph.MethodCFG) ||
         !(node2 instanceof shellmark.analysis.controlflowgraph.MethodCFG))
          throw new RuntimeException("node1: " + node1 + " ; node2: " + node2);

      int fromType,toType;
      if(node1Type == APP) {
         from = node1;
         fromType = node1Type;
          to = node2;
          toType = node2Type;
      } else if(node2Type == APP) {
          from = node2;
          fromType = node2Type;
          to = node1;
          toType = node1Type;
      } else {
         boolean compare = node1.toString().compareTo(node2.toString()) < 0;
         from = compare ? node1 : node2;
         fromType = compare ? node1Type : node2Type;
         to = compare ? node2 : node1;
         toType = compare ? node2Type : node1Type;
      }

	shellmark.analysis.controlflowgraph.MethodCFG fromCFG = 
	    (shellmark.analysis.controlflowgraph.MethodCFG)from;
	shellmark.analysis.controlflowgraph.MethodCFG toCFG = 
	    (shellmark.analysis.controlflowgraph.MethodCFG)to;


	/* Choose a random edge contained in the origin cluster */
	shellmark.analysis.controlflowgraph.Edge e = 
	    getRandomEdge(fromCFG,fromType);

      shellmark.analysis.controlflowgraph.CallGenerator cg;
      if(node1Type == APP && node2Type == APP) {
          if(DEBUG)
              System.out.println
                  ("adding NullNENull method call from " +
                   shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(fromCFG) + 
                   " to " +
                   shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(toCFG));
          cg = new shellmark.analysis.controlflowgraph.NullNENullCallGenerator();
      } else if(node1Type == WMARK && node2Type == WMARK) {
         cg = new shellmark.analysis.controlflowgraph.TopoMaintainingCallGenerator();
      } else {
	    double rnd = shellmark.util.Random.getRandom().nextDouble();
	    if(rnd < WM_METHOD_REAL_CALL_THRESHHOLD) {
		if(DEBUG)
		    System.out.println
			("adding real method call from " +
			 shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(fromCFG) + 
			 " to " +
			 shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(toCFG));
		cg = new shellmark.analysis.controlflowgraph.CallingCallGenerator();
	    } else {
		if(DEBUG)
		    System.out.println
			("adding real method call from " +
			 shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(fromCFG) + 
			 " to " +
			 shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(toCFG));
		cg = new shellmark.analysis.controlflowgraph.NullNENullCallGenerator();
	    }
      }
      cg.addPhantomCall(fromCFG,e,toCFG);
	if(!hasEdge(node1,node2))
	    addEdge(node1,node2);
	if(!hasEdge(node2,node1))
	    addEdge(node2,node1);
  }
  private shellmark.analysis.controlflowgraph.Edge getRandomEdge
	(shellmark.analysis.controlflowgraph.MethodCFG methodCFG,int type) {
     java.util.Iterator nodeIter = methodCFG.nodes();
     java.util.ArrayList nodes = new java.util.ArrayList();
     while (nodeIter.hasNext())
	  nodes.add(nodeIter.next());
     java.util.Random rnd = shellmark.util.Random.getRandom();
     shellmark.analysis.controlflowgraph.BasicBlock from,to;
     from = to = null;
     while(from == null) {
	  int fromIndex = ((rnd.nextInt() % nodes.size()) +
			   nodes.size()) % nodes.size();
	  int toIndex = ((rnd.nextInt() % nodes.size()) +
			 nodes.size()) % nodes.size();
	  from = 
	     (shellmark.analysis.controlflowgraph.BasicBlock)
	     nodes.get(fromIndex);
	  to = 
	     (shellmark.analysis.controlflowgraph.BasicBlock)
	     nodes.get(toIndex);
	  if(!methodCFG.hasEdge(from,to) || 
	     !isSplitableEdge(methodCFG,from,to,type)) {
	     from = null;
	  }
     }
     if(DEBUG)
	  System.out.println("chose " + from + " -> " + to);
     return new shellmark.analysis.controlflowgraph.Edge(from,to);
  }
  protected boolean isLegalEdge(Object node1,int node1Type,
                                Object node2,int node2Type) {
      Object from,to;
      int fromType,toType;
      if(node1Type == APP) {
          from = node1;
          fromType = node1Type;
          to = node2;
          toType = node2Type;
      } else if(node2Type == APP) {
          from = node2;
          fromType = node2Type;
          to = node1;
          toType = node1Type;
      } else {
         boolean compare = node1.toString().compareTo(node2.toString()) < 0;
         from =  compare ? node1 : node2;
         fromType = compare ? node1Type : node2Type;
         to = compare ? node2 : node1;
         toType = compare ? node2Type : node1Type;
      }
      shellmark.analysis.controlflowgraph.MethodCFG fromCFG =
         (shellmark.analysis.controlflowgraph.MethodCFG)from;
      shellmark.analysis.controlflowgraph.MethodCFG toCFG =
         (shellmark.analysis.controlflowgraph.MethodCFG)to;
	if(fromCFG.method().isNative() || 
	   fromCFG.method().isAbstract() || 
	   fromCFG.method().isInterface())
	    return false;
	if(toCFG.method().isPrivate() &&
	   !fromCFG.method().getClassName().equals
	   (toCFG.method().getClassName()))
	    return false;
      String toPackageName = 
          (toCFG.method().getClassName().lastIndexOf(".") == -1) ?
          "" : toCFG.method().getClassName().substring
          (0,toCFG.method().getClassName().lastIndexOf("."));
      String fromPackageName = 
          (fromCFG.method().getClassName().lastIndexOf(".") == -1) ?
          "" : fromCFG.method().getClassName().substring
          (0,fromCFG.method().getClassName().lastIndexOf("."));
	if(toCFG.method().isProtected() && 
         !toPackageName.equals(fromPackageName))
	    return false;
      if(toCFG.method().getName().indexOf("<init>") != -1 ||
         toCFG.method().getName().indexOf("<clinit>") != -1) {
          return false;
      }
	if(!hasSplitableEdge(fromCFG,fromType))
	    return false;
	return true;
  }
  protected boolean hasSplitableEdge
	(shellmark.analysis.controlflowgraph.MethodCFG cfg,int type) {
	java.util.Iterator nodeIt = cfg.nodes();
	while(nodeIt.hasNext()) {
	    shellmark.analysis.controlflowgraph.BasicBlock bb =
		(shellmark.analysis.controlflowgraph.BasicBlock)nodeIt.next();
	    java.util.Iterator succIt = 
	       cfg.succs(bb);
	    while(succIt.hasNext()) {
		shellmark.analysis.controlflowgraph.BasicBlock succ =
		    (shellmark.analysis.controlflowgraph.BasicBlock)
		    succIt.next();
		if(isSplitableEdge(cfg,bb,succ,type))
		    return true;
	    }
	}
	return false;
  }
  protected boolean isSplitableEdge
	(shellmark.analysis.controlflowgraph.MethodCFG cfg,
	 shellmark.analysis.controlflowgraph.BasicBlock src,
	 shellmark.analysis.controlflowgraph.BasicBlock dest,
       int type) {
     if(type == WMARK && src.fallthrough() != dest)
        return false;
	if(dest == cfg.sink())
	    return false;
	if(src == cfg.source())
	    return false;
	if(src.getInstList().size() == 0)
	    return false;
	if(dest.getInstList().size() == 0)
	    return false;
	org.apache.bcel.generic.Instruction srcLastInstr = 
	    src.getLastInstruction().getInstruction();
	if(srcLastInstr instanceof org.apache.bcel.generic.RET)
	    return false;
	if(srcLastInstr instanceof 
	   org.apache.bcel.generic.JsrInstruction)
	    return false;
	org.apache.bcel.generic.CodeExceptionGen exceptions[] =
	    cfg.method().getExceptionHandlers();
	for(int i = 0 ; exceptions != null && i < exceptions.length ; i++)
	    if(exceptions[i].getHandlerPC() == dest.getIH())
		return false;
	return true;
  }
  public String nodeName(Object o) {
      return shellmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
          ((shellmark.analysis.controlflowgraph.MethodCFG)o);
  }
}

