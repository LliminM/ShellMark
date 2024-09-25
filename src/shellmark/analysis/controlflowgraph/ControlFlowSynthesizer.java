package shellmark.analysis.controlflowgraph;

/**
 * Family of classes that generate code to match a control flow graph.
 * The parent class also contains utility functions and a random number
 * stream for use by subclasses.
 */
public abstract class ControlFlowSynthesizer {


protected java.util.Random rng;         // instance random number stream



/**
 * Constructs a new ControlFlowSynthesizer with its own private
 * random number stream initialized from the Math.random stream.
 */
protected ControlFlowSynthesizer() {
    rng = shellmark.util.Random.getRandom();
}


/**
 * Sets the random number seed to force reproducible behavior.
 */
public void setSeed(long seed) {
   rng.setSeed(seed);
}



/**
 *  Returns a random r such that m <= r <= n.
 */
protected int randomIn(int m, int n) {
   return m + rng.nextInt(n - m + 1);
}



/**
 * Generates a method that corresponds to the given graph.
 * Some concrete implementations may require specific graph forms
 * and throw java.lang.IllegalArgumentException for an unacceptable
 * graph.
 *
 * <P> The abstract
 */
public final shellmark.program.Method generate(
   shellmark.util.newgraph.Graph g,
   shellmark.program.Class clazz) {

   java.lang.Object root = g.roots().next();
   return generate(g, clazz, root, getNodeNumbers(g, root));
}

protected static java.util.Map getNodeNumbers(shellmark.util.newgraph.Graph g,
					      java.lang.Object root) {
   g = g.removeUnreachable(root);
   java.util.Map nodeNumbers = new java.util.HashMap();
   shellmark.util.newgraph.DomTree d = g.dominatorTree(root);
   if (shellmark.util.newgraph.Graphs.reducible(g,root,d)) {
      shellmark.util.newgraph.Graph h = g;
      java.util.Iterator edgeIter = h.edges();
      while (edgeIter.hasNext()) {
	 shellmark.util.newgraph.Edge e =
	    (shellmark.util.newgraph.Edge)edgeIter.next();
	 if (d.dominates(e.sinkNode(),e.sourceNode()))
	    h = h.removeEdge(e);
      }
      java.util.List hp = h.acyclicHamiltonianPath(root);
      if (hp != null) {
	 java.util.Iterator iter = hp.iterator();
	 for (int i = 0; iter.hasNext(); i++)
	    nodeNumbers.put(iter.next(), new java.lang.Integer(i));
	 return nodeNumbers;
      }
   }

   java.util.Iterator iter = g.depthFirst(root);
   for (int i = 0; iter.hasNext(); i++)
      nodeNumbers.put(iter.next(), new java.lang.Integer(i));
   return nodeNumbers;
}

protected abstract shellmark.program.Method generate(
   shellmark.util.newgraph.Graph g,
   shellmark.program.Class clazz,
   java.lang.Object root,
   java.util.Map nodeNumbers);

/**
 * Traverses a graph and creates a NOP instruction for each node,
 * returning an array of handles indexed by node numbers.
 * Because these are 1-based, there is an extra handle[0] left null.
 */
org.apache.bcel.generic.InstructionHandle[] makeNOPs(
      org.apache.bcel.generic.InstructionList inslist,
      shellmark.util.newgraph.Graph graph,
      java.lang.Object root,
      java.util.Map nodeNumbers) {

   org.apache.bcel.generic.InstructionHandle handles[] =
      new org.apache.bcel.generic.InstructionHandle[graph.nodeCount() + 1];

   java.util.Iterator i = graph.depthFirst(root);
   while (i.hasNext()) {
      java.lang.Object node = i.next();
      handles[((Integer)nodeNumbers.get(node)).intValue()] =
         inslist.append(org.apache.bcel.generic.InstructionConstants.NOP);
   }

   return handles;
}



/**
 *  Tests this synthesizer using a radix-encoded watermark.
 *  Called from static main methods in concrete subclasses.
 *  <P> Usage: java classname [-r] [watermark [classname]]
 *  <BR> -r specifies randomized (irreproducible) behavior
 *  <BR> watermark is the string to encode; the default is "31416"
 *  <BR> classname, if present, causes generation of a .class file
 */
void test(String[] args) throws java.lang.Exception {

   shellmark.util.Options o = new shellmark.util.Options(new String[] {
      "-r",		"randomize behavior",
      "watermark",	"string to encode",
      "classname",	"generate output classfile",
   });
   o.parse(args);

   int i = o.getIndex();
   String wmstring = (args.length > i) ? args[i] : "31416";
   java.math.BigInteger watermark = new java.math.BigInteger(wmstring);

   String classname = (args.length > i + 1) ? args[i + 1] : "bogus";
   shellmark.program.Application app = new shellmark.program.Application();
   shellmark.program.Class clazz = 
       new shellmark.program.LocalClass
       (app,classname, "java.lang.Object", "watermark",
        org.apache.bcel.Constants.ACC_PUBLIC
        | org.apache.bcel.Constants.ACC_SUPER,
        null);
   clazz.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);

   // make a simple graph
   shellmark.util.newgraph.codec.GraphCodec codec =
      new shellmark.util.newgraph.codec.ReduciblePermutationGraph();
   shellmark.util.newgraph.Graph g = codec.encode(watermark);

   // turn the graph into a CFG
   if (o.getValue('r') == null) {	// if -r option not specified
      this.setSeed(1);			// force reproducible behavior
   }
   shellmark.program.Method method = this.generate(g, clazz);

   // print it out
   System.out.println(method);
   org.apache.bcel.generic.InstructionList il = method.getInstructionList();
   System.out.println(il.toString(true));

   MethodCFG cfg = method.getCFG(false);
   shellmark.util.newgraph.Graph gr = 
      cfg.graph().removeNode(cfg.source()).removeNode(cfg.sink());
   if(!codec.decode(gr).equals(watermark))
      System.out.println("TEST FAILED: decode failed");

   // dump classfile if requested
   if (!classname.equals("bogus")) {
      method.setMaxStack();
      app.save(classname + ".jar");
   }
}
} // class ControlFlowSynthesizer

