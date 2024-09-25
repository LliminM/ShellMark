package shellmark.program;

/**
 * Represents a field within a class or interface found on the CLASSPATH.
 * Such fields are immutable;
 * modification attempts produce a java.lang.UnsupportedOperationException.
 *
 * @see shellmark.program.Field
 */

public class LibraryField extends shellmark.program.Field {
	
	/**
	    *  Constructs a LibraryField from a BCEL Field and adds it to a class.
	    */
	   /*package*/ LibraryField(
	         shellmark.program.Class parent, org.apache.bcel.classfile.Field f) {

	      super(parent, f);
	      setImmutable();
	   }

}
