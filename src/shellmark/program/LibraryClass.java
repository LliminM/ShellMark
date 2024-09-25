package shellmark.program;

/**
 * Represents a class found on the CLASSPATH.
 * Such classes are created lazily and automatically as needed,
 * and are immutable.
 * Modification attempts produce a java.lang.UnsupportedOperationException.
 *
 * @see shellmark.program.Class
 */

public class LibraryClass extends shellmark.program.Class {
//  Caches library classes for reuse, since they're immutable.

   private static java.util.Hashtable classtab = new java.util.Hashtable();


   /**
    * Finds a class on $CLASSPATH and returns a LibraryClass object.
    * Such classes are not considered part of the application,
    * and are immutable.
    * Returns null if the class cannot be found or loaded.
    */
   
   
   //CHANGE IS HERE!!!
   public static shellmark.program.LibraryClass find(String classname) {
      shellmark.program.LibraryClass c =
         (shellmark.program.LibraryClass) classtab.get(classname);
      if (c != null) {
         return c;
      }

      org.apache.bcel.classfile.JavaClass jclass = null;
	  try {
		  jclass = org.apache.bcel.Repository.lookupClass(classname);
	   } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	   }
      if(jclass == null)
         return null;

      c = new shellmark.program.LibraryClass(jclass);

      for (java.util.Iterator miter=c.methods();miter.hasNext();){
         ((Method)miter.next()).fixLDC_WBug();
      }

      classtab.put(classname, c);
      return c;
   }


   /**
    * Constructs a LibraryClass from a BCEL JavaClass.
    * The new class is not part of any application.
    */
   LibraryClass(org.apache.bcel.classfile.JavaClass jclass) {
      super(null, jclass, null);
      setImmutable();
   }



   /**
    *  Constructs a LibraryCPG for this class.
    */
   /*package*/ shellmark.program.ConstantPoolGen makeCPG
      (shellmark.program.Class c, org.apache.bcel.classfile.ConstantPool g) {
      return new LibraryCPG(c, g);
   }


   /**
    *  Constructs a LibraryField for this class.
    */
   /*package*/ shellmark.program.Field makeField
      (shellmark.program.Class c, org.apache.bcel.classfile.Field f) {
      return new LibraryField(c, f);
   }

   /**
    *  Constructs a LibraryMethod for this class.
    */
   /*package*/ shellmark.program.Method makeMethod
      (shellmark.program.Class c, org.apache.bcel.classfile.Method m) {
      return new LibraryMethod(c, m);
   }

}
