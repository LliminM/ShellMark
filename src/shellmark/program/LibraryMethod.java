package shellmark.program;

/**
 * Represents a method within a class or interface found on the CLASSPATH.
 * Such methods are immutable;
 * modification attempts produce a java.lang.UnsupportedOperationException.
 *
 * @see shellmark.program.Method
 */

public class LibraryMethod extends shellmark.program.Method {
	/**
	 * Constructs a LibraryMethod from a BCEL Method and adds it to a class.
	 */
	/*package*/ LibraryMethod(shellmark.program.Class parent,
	   org.apache.bcel.classfile.Method method) {
	   super(parent, method);
	   setImmutable();
	}


}
