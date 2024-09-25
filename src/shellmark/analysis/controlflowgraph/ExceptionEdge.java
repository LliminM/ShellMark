package shellmark.analysis.controlflowgraph;

public class ExceptionEdge extends shellmark.util.newgraph.EdgeImpl {
    org.apache.bcel.generic.CodeExceptionGen mCEGs[];
   public ExceptionEdge(Object from, Object to,
			org.apache.bcel.generic.CodeExceptionGen ceg) {
       this(from,to,new org.apache.bcel.generic.CodeExceptionGen [] {ceg});
   }
    public ExceptionEdge(Object from, Object to,
			 org.apache.bcel.generic.CodeExceptionGen cegs[]) {
      super(from, to);
      mCEGs = cegs;
   }
    public org.apache.bcel.generic.CodeExceptionGen [] exception() {
	return mCEGs;
    }
    public shellmark.util.newgraph.Edge clone(Object source,Object sink) 
	throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }
}
