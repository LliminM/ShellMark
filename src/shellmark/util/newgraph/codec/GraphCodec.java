package shellmark.util.newgraph.codec;

public interface GraphCodec {
	   abstract public shellmark.util.newgraph.Graph encode(java.math.BigInteger value,
							       shellmark.util.newgraph.NodeFactory factory);
	   abstract public java.math.BigInteger decode(shellmark.util.newgraph.Graph graph)
	      throws DecodeFailure;

	   abstract public shellmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value,
									     shellmark.util.newgraph.NodeFactory f);
	   abstract public java.math.BigInteger decode(shellmark.util.newgraph.MutableGraph graph)
	      throws DecodeFailure;

	   abstract public shellmark.util.newgraph.Graph encode(java.math.BigInteger value);
	   abstract public shellmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value);
	   
	   public abstract int maxOutDegree();
	}
