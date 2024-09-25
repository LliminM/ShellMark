package shellmark.analysis.interference;

public class InterferenceGraph extends shellmark.util.newgraph.MutableGraph {
    public InterferenceGraph(shellmark.program.Method method) {
	shellmark.analysis.defuse.ReachingDefs rds = 
	    new shellmark.analysis.defuse.ReachingDefs(method);
	shellmark.analysis.liveness.Liveness liveness = 
	    new shellmark.analysis.liveness.Liveness(method);
	shellmark.analysis.defuse.DUWeb webs[] = rds.defUseWebs();
	for(int i = 0 ; i < webs.length ; i++)
	    addNode(webs[i]);
	for(int i = 0 ; i < webs.length ; i++)
	    for(int j = 0 ; j < webs.length ; j++) {
            if(j == i)
               continue;
            
		for(java.util.Iterator defs = webs[i].defs().iterator() ; 
		    defs.hasNext() ; ) {
		    shellmark.analysis.defuse.DefWrapper def =
			(shellmark.analysis.defuse.DefWrapper)defs.next();
		    org.apache.bcel.generic.InstructionHandle ih = null;
		    if(def instanceof
		       shellmark.analysis.defuse.InstructionDefWrapper)
			ih = ((shellmark.analysis.defuse.InstructionDefWrapper)
			      def).getIH();
		    else
			ih = method.getInstructionList().getStart();
		    if(liveness.liveAt(webs[j],ih)) {
			if(!hasEdge(webs[i],webs[j]))
			    addEdge(webs[i],webs[j]);
			if(!hasEdge(webs[j],webs[i]))
			    addEdge(webs[j],webs[i]);
		    }
		}
	    }
    }
    public static void main(String argv[]) throws Exception {
	shellmark.program.Application app =
	    new shellmark.program.Application(argv[0]);
	for(java.util.Iterator it = app.classes() ; it.hasNext() ; ) {
	    for(java.util.Iterator it2 = 
		    ((shellmark.program.Class)it.next()).methods() ; 
		it2.hasNext() ; ) {
		shellmark.program.Method method = 
		    (shellmark.program.Method)it2.next();
		method.getIFG();
	    }
	}
	    
    }
}
