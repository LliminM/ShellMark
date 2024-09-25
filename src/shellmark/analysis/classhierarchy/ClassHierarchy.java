package shellmark.analysis.classhierarchy;

/*
 *  An instance method m1 declared in a class C overrides another method with the
 *  same signature, m2, declared in class A if both
 *     1. C is a subclass of A.
 *     2. Either
 *        m2 is non-private and accessible from C, or
 *        m1 overrides a method m3, m3 distinct from m1,
 *        m3 distinct from m2, such that m3 overrides m2.
 *
 *  If you are trying to override a method with a different argument list,
 *  that isn't overriding, but overloading. In order for the method to be
 *  properly overridden, and
 *  thus called appropriately, the subclass method signature must explicitly
 *  match that of the superclass. In the case of equals() that means the
 *  return type must be boolean and the argument must be Object.
 *
 * Adding argument to B:m means adding arguments to
 * all m:s _that override B:m_ in any super- and sub-classes of B.
 * I.e. any methods m with the same signature as B:m
 * has to get the extra argument. And the calls, too.
 *
 * Two methods A:m and B:m (A and B may be the same class)
 * with different signatures (they're overloaded) must
 * not get the _same_ signature after the argument was
 * added.
 */

/**
 * ClassHierarchy is a tree s.t. there exists an edge from
 * class A to B iff B extends (or implements) A. For example, to
 * retrieve all the subclasses of method with name S, you could define an: 
 * iterator: <code> it = this.breadthFirst((this.lookup(S))</code>. This
 * iterator contains {@link shellmark.program.Class shellmark.program.Class} objects.
 **/

public class ClassHierarchy extends shellmark.util.newgraph.MutableGraph {     

   private static final java.lang.String DYNAMIC_INIT = "<init>";
   private static final java.lang.String STATIC_INIT = "<clinit>";    
   private java.util.HashMap name2class;
    
   /**
    * Null constructor, used for testing purposes.
    */
   public ClassHierarchy() {
      name2class = new java.util.HashMap();
   }

   /**
    * Build a class hierarchy tree from the classes in app.
    * Any standard java classes (java.*, etc) are also
    * included if they can be loaded.
    * Each node in the tree is a a class. Incoming edges represent
    * parent classes, i.e. the superclass and any interface classes.     
    */
   public ClassHierarchy(shellmark.program.Application app) {
      this();
      addApplication(app);
   }

   public String toDot() {
      return shellmark.util.newgraph.Graphs.toDot(this);
   }   
  
   /**
    * Adds the class files in the application app to this ClassHierarchy
    */
   public void addApplication(shellmark.program.Application app) {
      java.util.Iterator classes = app.classes();
      while(classes.hasNext()) {
    	  shellmark.program.Class cls = (shellmark.program.Class)classes.next();            
         addClass(cls);
      }      
   }

   /*
    * Build the tree rooted in the class 'className'.
    * Assume that this is a 'user-defined' class, i.e. one
    * contained in the app object which can be loaded using Application Class.
    */
   public void addClass(shellmark.program.Class cls)  
   {
      if(hasNode(cls)) 
         return;

      name2class.put(cls.getName(), cls);
      addNode(cls);
        
      shellmark.program.Class superClass = cls.getSuperClass();
      if (superClass != null && superClass != cls) {
         addClass(superClass);
         //System.out.println("Adding edge " + superClass + "->" + cls);
         addEdge(new shellmark.util.newgraph.EdgeImpl(superClass, cls));
      }
      shellmark.program.Class interfaceClasses[] = cls.getInterfaces();
      for(int i=0; i<interfaceClasses.length; i++) {
         addClass(interfaceClasses[i]);
         addEdge(new shellmark.util.newgraph.EdgeImpl(interfaceClasses[i], cls));
      }
   }  

   public String toString() {
      return toDot();
   }

   /**
    * Return the class associated with this class name. 
    * @return The class object, if it is this hierarchy
    * @throws ClassHierarchyException if the class is not 
    * in this hierarchy
    **/
   public shellmark.program.Class lookup(String className) 
      throws ClassHierarchyException {
      return lookup(className,null);
   }

   public shellmark.program.Class lookup
      (String className,shellmark.program.Application referent) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      if (!name2class.containsKey(className))
         className = className.replace('/','.');
      if (!name2class.containsKey(className))
         className = className.replace('.','/');
      if (!name2class.containsKey(className)){
    	  shellmark.program.Class clazz = 
	    referent == null ? 
	    		shellmark.program.LibraryClass.find(className.replace('/','.')) :
	    referent.findClass(className.replace('/','.'));
         if (clazz==null){
            throw new shellmark.analysis.classhierarchy.ClassHierarchyException
               ("No such class '"+ className + "' in the class hierarchy.");
         }else{
            addClass(clazz);
            return clazz;
         }
      }
      return (shellmark.program.Class)name2class.get(className);
   }

   /**
    * @return true if cls is an instance of 
    * {@link shellmark.program.LibraryClass shellmark.program.LibraryClass},
    */
   public boolean isLibraryClass(shellmark.program.Class cls) {
      return cls instanceof shellmark.program.LibraryClass;            
   }

   /*
    * Return true if this is an interface class.
    */
   public boolean isInterface(shellmark.program.Class cls) {
      return cls.isInterface();
   }   
  
   /*
    * Not sure why this is here but it creates MethodIDs from Methods...
    */
    
   public shellmark.util.MethodID[] getMethods(shellmark.program.Class cls) {
	   shellmark.util.MethodID[] mids = 
         new shellmark.util.MethodID[cls.getMethods().length];
      for(int i = 0; i < cls.getMethods().length; i++)
         mids[i] = new shellmark.util.MethodID(cls.getMethods()[i]);
      return mids;       
   }

   /************************************************************************/
   /*                  Querying the inheritance tree                       */
   /************************************************************************/

   /**
    * Return true of subClass extends (directly or indirectly) superClass.
    * classExtends(A,A) returns true.
    **/
   public boolean classExtends (shellmark.program.Class subClass,
		   shellmark.program.Class superClass) {
      return graph().removeUnreachable(superClass).hasNode(subClass);
   }

   private java.util.ArrayList itAsList(java.util.Iterator it) {
      java.util.ArrayList list = new java.util.ArrayList();
      while(it.hasNext())
         list.add(it.next());
      return list;
   }

   /**
    * Return all the classes that subClass extends or implements, directly or indirectly.
    * subClass is part of this list. 
    */
   public shellmark.program.Class[] superClasses(shellmark.program.Class subClass) {
      return (shellmark.program.Class[])
         itAsList(graph().reverse().depthFirst(subClass)).toArray
         (new shellmark.program.Class[0]);
   }

   /**
    * Return all the classes that inherit superClass, directly or indirectly.
    * superClass is part of this list.
    */
   public shellmark.program.Class[] subClasses
      (shellmark.program.Class superClass) {
      return (shellmark.program.Class[])
         itAsList(graph().depthFirst(superClass)).toArray
         (new shellmark.program.Class[0]);
   }

   /**
    * Return all the classes that extend Class, directly or indirectly,
    * or which Class extends, directly or indirectly. Class is part of
    * this list.
    **/
   public shellmark.program.Class[] inheritanceChain(shellmark.program.Class Class) {
	   shellmark.program.Class sub[] = subClasses(Class);
	   shellmark.program.Class sup[] = superClasses(Class);
	   shellmark.program.Class all[] = 
         new shellmark.program.Class[sub.length + sup.length - 1];
      all[0] = Class;
      int allNdx = 1;
      for(int i = 0 ; i < sub.length ; i++)
         if(sub[i] != Class)
            all[allNdx++] = sub[i];
      for(int i = 0 ; i < sup.length ; i++)
         if(sup[i] != Class)
            all[allNdx++] = sup[i];

      return all;
   }

   /**
    * Return true if subMethod overrides superMethod. They must have
    * the same signature and name and subMethod must be in a class
    * that extends superMethod, directly or indirectly.
    * overrides(A,A) will return true.
    */
   public boolean overrides(shellmark.util.MethodID subMethod,
		   shellmark.util.MethodID superMethod) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException {
	   shellmark.program.Class subClass = lookup(subMethod.getClassName());
	   shellmark.program.Class superClass = lookup(superMethod.getClassName());
        
      if (!subMethod.getName().equals(superMethod.getName()))
         return false;
      if (!subMethod.getSignature().equals(superMethod.getSignature()))
         return false;
      return classExtends(subClass, superClass);
   }

   /**
    * Return true if subMethod overloads superMethod. They must have
    * the same name, different signatures, and subMethod must be in
    * a class that extends superMethod, directly or indirectly.
    * overloads(A,A) will return false.
    **/
   public boolean overloads(shellmark.util.MethodID subMethod,
		   shellmark.util.MethodID superMethod) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{

	   shellmark.program.Class subClass = lookup(subMethod.getClassName());
	   shellmark.program.Class superClass = lookup(superMethod.getClassName());

      if (!subMethod.getName().equals(superMethod.getName()))
         return false;
      if (subMethod.getSignature().equals(superMethod.getSignature()))
         return false;
      return classExtends(subClass, superClass);
   }

   /**
    * Return all methods overridden by subMethod. They must have
    * the same signature and name and subMethod must be in a class
    * that extends superMethod, directly or indirectly.
    * overrides(A,A) will return true.
    */
   public shellmark.util.MethodID[] overrides(shellmark.util.MethodID subMethod)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{

	   shellmark.program.Class subClass = lookup(subMethod.getClassName());

      java.util.Vector res = new java.util.Vector();
      shellmark.program.Class[] superClasses = superClasses(subClass);

      for(int i=0; i<superClasses.length; i++) {
    	  shellmark.program.Class curr = superClasses[i];
            
    	  shellmark.program.Method[] methods = curr.getMethods();                
         for(int m=0; m<methods.length; m++) {
        	 shellmark.util.MethodID mid =
               new shellmark.util.MethodID(methods[m]);
            if (overrides(subMethod, mid)){
               res.add(mid);
            }
         }
      }
      shellmark.util.MethodID[] S = new shellmark.util.MethodID[res.size()];
      res.toArray(S);
      return S;
   }
   /**
    * Return all methods overridden by 'method' or which it overrides.
    * All methods must have the same signature and name and 'method'
    * must be in a class that extends the other methods, directly or
    * indirectly, or vice versa.
    * In other words, this method returns all the methods in the class
    * hierarchy related to 'method' through overriding.
    **/
   public shellmark.util.MethodID[] allOverrides(shellmark.util.MethodID method)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      java.util.Vector res = new java.util.Vector();
      shellmark.program.Class cls = lookup(method.getClassName());
      shellmark.program.Class[] classes = inheritanceChain(cls);
      for(int i=0; i<classes.length; i++) {          
         shellmark.program.Method[] methods = classes[i].getMethods();
         for(int m=0; m<methods.length; m++) {
            shellmark.util.MethodID mid =
               new shellmark.util.MethodID(methods[m]);
            if (method.getName().equals(mid.getName()) &&
                method.getSignature().equals
                (mid.getSignature()))
               res.add(mid);
         }
      }
      shellmark.util.MethodID[] S = new shellmark.util.MethodID[res.size()];
      res.toArray(S);
      return S;
   }

   /**
    * Return all methods overloaded by 'method' or which it overloads.
    * All methods must have the same name but a different signature from
    * 'method' must be in a class that extends the other methods, directly or
    * indirectly, or vice versa.
    * In other words, this method returns all the methods in the class
    * hierarchy related to 'method' through overloading.
    **/
   public shellmark.util.MethodID[] allOverloads(shellmark.util.MethodID method) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      java.util.Vector res = new java.util.Vector();
      shellmark.program.Class[] classes = 
         inheritanceChain(lookup(method.getClassName()));
      for(int i=0; i<classes.length; i++) {
         shellmark.program.Method[] methods =
            classes[i].getMethods();                
         for(int m=0; m<methods.length; m++) {
            shellmark.util.MethodID mid =
               new shellmark.util.MethodID(methods[m]);
            if (method.getName().equals(mid.getName()) &&
                !method.getSignature().equals
                (mid.getSignature()))
               res.add(mid);
         }
      }
      shellmark.util.MethodID[] S = new shellmark.util.MethodID[res.size()];
      res.toArray(S);
      return S;
   }

   /**
      A convenience version of methodRenameOK(MethodID, MethodID)
      that is used to test to see if a new method name is ok,
      given that nothing else about the method changes.
      @param origMethod the method to rename
      @param newName the proposed new name
      @return true if new name does not cause any new conflicts.
   */
   public boolean methodRenameOK(shellmark.util.MethodID origMethod,
                                 String newName) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException
   {
      shellmark.util.MethodID newMethod =
         new shellmark.util.MethodID(newName,
                                    origMethod.getSignature(),
                                    origMethod.getClassName());

      return methodRenameOK(origMethod, newMethod);
   }

   /**
    * Return false if 'origMethod' is special in some way, i.e. we
    * cannot change its name or signature into 'newMethod'. A method
    * name/signature change is illegal
    *    * if the original method overrides some method (directly or
    *      indirectly) in the java.* hierarchy;
    *    * if the new signature overrides a method declared in
    *      a superclass.
    *    * if the original method is a static or dynamic initializer.
    * There may be other conditions as well, in particular related to
    * whether a method is static or dynamic.
    */
   public boolean methodRenameOK (shellmark.util.MethodID origMethod,
                                  shellmark.util.MethodID newMethod) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{

      if(isSpecialMethod(origMethod))
         return false;

      if(overridesLibraryMethod(origMethod))
         return false;        

      shellmark.util.MethodID[] overriddenNewMethods = overrides(newMethod);
      if (overriddenNewMethods.length>0)
         return false;

      return true;
   }

   /**
      Checks if the method overrides some library method (directly or indirectly)       
      @param suspect the method in question
      @return true if the method overrides a method in a library
   */    
   public boolean overridesLibraryMethod( shellmark.util.MethodID suspect) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException
   {
      shellmark.util.MethodID[] overriddenMethods = overrides(suspect);

      for(int i=0; i < overriddenMethods.length; i++) {
         if (isLibraryClass(lookup(overriddenMethods[i].getClassName())))
            return true;
      }
      return false;
   }

   /**
      Checks if the method is a special java method, such as the main
      method, or an initializer.
      @param origMethod the method in question
      @return true if the method is special and should not be changed
   */
   public boolean isSpecialMethod(shellmark.util.MethodID origMethod)
   {
      /*
       * If a method is a static or dynamic initializer, it should not
       * be valid to rename it to anything.  Check added 2/21/2002
       * -Kelly Heffner
       */
      if(origMethod.getName().equals
         (shellmark.analysis.classhierarchy.ClassHierarchy.DYNAMIC_INIT) ||
         origMethod.getName().equals
         (shellmark.analysis.classhierarchy.ClassHierarchy.STATIC_INIT)){
         return true;
      }

      // do not allow renaming of the main method
      if(origMethod.getName().equals("main") && 
         origMethod.getSignature().equals("([Ljava/lang/String;)V"))
         return true;

      return false;
   }

   /**
    * Returns an array of methods that need to be changed if
    * 'origMethod' is renamed 'newMethod'.
    */
   public shellmark.util.MethodID[] getMethodsToRename 
      (shellmark.util.MethodID origMethod )  
      throws shellmark.analysis.classhierarchy.ClassHierarchyException {
      return allOverrides(origMethod);
   }

   /**
    * Resolves an unresolved symbolic reference from class c to a method
    * specified in MethodID m.
    * @param m method id specifying the method
    * @param referent class that references the method
    * @return the resolved method if found, null otherwise
    */
   public shellmark.program.Method resolveMethodReference
      (shellmark.util.MethodID m,shellmark.program.Class referent) 
      throws ClassHierarchyException {

      shellmark.program.Class container = lookup(m.getClassName());

      if (container == null || isInterface(container))
         return null;

      shellmark.program.Method method = null;
      {
         shellmark.program.Class superClass = container;
         while(method == null) {
            method = superClass.getMethod(m.getName(),m.getSignature());
            if(superClass.getSuperClass() == superClass)
               break;
            superClass = superClass.getSuperClass();
         }
      }

      // looks for the method in the interfaces if it hasn't found it yet
      if (method == null) {
         java.util.HashSet visited = new java.util.HashSet();
         java.util.LinkedList queue = new java.util.LinkedList();
         queue.addAll(java.util.Arrays.asList(container.getInterfaces()));
         while(method == null && !queue.isEmpty()) {
            shellmark.program.Class iface = 
               (shellmark.program.Class)queue.removeFirst();
            if(visited.contains(iface))
               continue;
            visited.add(iface);
            method = iface.getMethod(m.getName(),m.getSignature());
            queue.addAll(java.util.Arrays.asList(iface.getInterfaces()));
         }
      }
	
      if (method == null || (method.isAbstract() && !(container.isAbstract()))
          || !isAccessible(method, referent)) {
         return null;
      } else
         return method;
   }

   public shellmark.program.Method resolveInterfaceMethodReference
      (shellmark.util.MethodID m,shellmark.program.Class referent)
      throws ClassHierarchyException {
      shellmark.program.Class container = lookup(m.getClassName());

      if(container == null || !container.isInterface())
         return null;

      shellmark.program.Method method = 
         container.getMethod(m.getName(),m.getSignature());

      if (method == null) {
         java.util.HashSet visited = new java.util.HashSet();
         java.util.LinkedList queue = new java.util.LinkedList();
         queue.addAll(java.util.Arrays.asList(container.getInterfaces()));
         while(method == null && !queue.isEmpty()) {
            shellmark.program.Class iface = 
               (shellmark.program.Class)queue.removeFirst();
            if(visited.contains(iface))
               continue;
            visited.add(iface);
            method = iface.getMethod(m.getName(),m.getSignature());
            queue.addAll(java.util.Arrays.asList(iface.getInterfaces()));
         }
      }

      if(method == null) {
         shellmark.program.Class objClass = container.getSuperClass();
         if(!objClass.getName().equals("java.lang.Object"))
            throw new Error
               ("interfaces should have object as superclass " + objClass);
         method = objClass.getMethod(m.getName(),m.getSignature());
      }

      return method;
   }

   public shellmark.program.Field resolveFieldReference
      (shellmark.util.FieldID f,shellmark.program.Class referent) 
      throws ClassHierarchyException {
      return _resolveFieldReference(f,lookup(f.getClassName()),referent);
   }
   private shellmark.program.Field _resolveFieldReference
      (shellmark.util.FieldID f,shellmark.program.Class container,
       shellmark.program.Class referent) 
      throws ClassHierarchyException {

      if(container == null)
         return null;

      shellmark.program.Field field = container.getField(f.getName(),f.getSignature());

      if(field == null) {
         shellmark.program.Class interfaces[] = container.getInterfaces();
         for(int i = 0 ; field == null && i < interfaces.length ; i++)
            field = _resolveFieldReference(f,interfaces[i],referent);
      }
	
      if(field == null && container.getSuperClass() != container)
         field = _resolveFieldReference(f,container.getSuperClass(),referent);

      if(field == null || !isAccessible(field,referent))
         return null;

      return field;
   }

   // checks if method m is accessible from class cls
   private boolean isAccessible(shellmark.program.Method m,
                                shellmark.program.Class cls) {
      if (m.isPublic())
         return true;

      if (m.isProtected() && classExtends(cls, m.getEnclosingClass()))
         return true;

      if ((m.isProtected() || 
           (!m.isPublic() && !m.isProtected() && !m.isPrivate())) && 
          cls.getPackageName().equals(m.getEnclosingClass().getPackageName()))
         return true;

      if(m.isPrivate() && m.getEnclosingClass() == cls)
         return true;

      return false;
   }

   // checks if field f is accessible from class cls
   private boolean isAccessible(shellmark.program.Field f,
                                shellmark.program.Class cls) {
      if (f.isPublic())
         return true;

      if (f.isProtected() && classExtends(cls, f.getEnclosingClass()))
         return true;

      if ((f.isProtected() || (!f.isPublic() && !f.isProtected() && 
                               !f.isPrivate())) && 
          cls.getPackageName().equals(f.getEnclosingClass().getPackageName()))
         return true;

      if(f.isPrivate() && f.getEnclosingClass() == cls)
         return true;

      if (f.isPrivate()
          && cls.getMethod(f.getName(), f.getSignature()) != null)
         return true;

      return false;
   }

   /**
    * Finds the target method of the invokespecial instruction.
    * @param mid methodID
    * @param cls class where invokespecial is called from
    * @return the target method
    */
   public shellmark.program.Method findInvokeSpecialTarget(shellmark.util.MethodID mid, shellmark.program.Class cls)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException
   {
      shellmark.program.Method method = resolveMethodReference(mid, cls);

      if (method != null) {
         if (method.isStatic())
            return null;

         if (cls.isSuper() &&
             classExtends(cls, lookup(method.getClassName())) &&
             !cls.getName().equals(method.getClassName()) &&
             !isInstanceInitMethod(method)) {

            shellmark.program.Class superClass = cls.getSuperClass();
            java.lang.String name = method.getName();
            java.lang.String signature = method.getSignature();
            for (;;) {
               method = superClass.getMethod(name, signature);
               if (superClass.getName().equals("java.lang.Object"))
                  break;

               if (method == null)
                  superClass = superClass.getSuperClass();
               else
                  break;
            }
         }

         if (method!=null && method.isAbstract())
            return null;
      }
	
      return method;
   }

   // checks if the method is an instance initialization method
   private boolean isInstanceInitMethod(shellmark.program.Method method) {
      return (method.getName().equals(DYNAMIC_INIT) ||
              method.getName().equals(STATIC_INIT));
   }

   /************************************************************************/
   /*                              Testing                                 */
   /************************************************************************/
    
   static void testExtends  (
                             shellmark.analysis.classhierarchy.ClassHierarchy ch,
                             shellmark.program.Class subClass,
                             shellmark.program.Class superClass) {
      System.out.println("------------------------- testExtends -------------------");
      boolean res = ch.classExtends(subClass, superClass);
      if (res)
         System.out.println("Class '" + subClass + "' extends '" + superClass + "'");
      else
         System.out.println("Class '" + subClass + "' does not extend '" + superClass + "'");
   }

   static void testSuperClasses  (
                                  shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                  shellmark.program.Class subClass)  {
      System.out.println("------------------------- testSuperClasses -------------------");
      shellmark.program.Class[] S = ch.superClasses(subClass);
      for(int i=0; i<S.length; i++)
         System.out.println(S[i] + " is a superclass of " + subClass);
   }

   static void testSubClasses  (
                                shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                shellmark.program.Class superClass) {
      System.out.println("------------------------- testSubClasses -------------------");
      shellmark.program.Class[] S = ch.subClasses(superClass);
      for(int i=0; i<S.length; i++)
         System.out.println(S[i] + " is a subclass of " + superClass);
   }

   static void testInheritanceChain  (
                                      shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                      shellmark.program.Class Class)  {
      System.out.println("-------------------------inheritanceChain  -------------------");
      shellmark.program.Class[] S = ch.inheritanceChain(Class);
      for(int i=0; i<S.length; i++)
         System.out.println(S[i] + " is in chain of " + Class);
   }

   static void testOverrides  (
                               shellmark.analysis.classhierarchy.ClassHierarchy ch,
                               shellmark.util.MethodID subMethod, 
                               shellmark.util.MethodID superMethod)  
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      System.out.println("------------------------- overrides  -------------------");        
      boolean res = ch.overrides(subMethod, superMethod);
      if (res)
         System.out.println(subMethod + " overrides " + superMethod);
      else
         System.out.println(subMethod + " doesn't override " + superMethod);
   }

   static void testOverrides  (
                               shellmark.analysis.classhierarchy.ClassHierarchy ch,
                               shellmark.util.MethodID subMethod)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      System.out.println("------------------------- overrides  -------------------");       
      shellmark.util.MethodID[] res = ch.overrides(subMethod);
      for(int i=0; i<res.length; i++)
         System.out.println(subMethod + " is overridden by " + res[i]);
   }

   static void testAllOverrides  (
                                  shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                  shellmark.util.MethodID subMethod)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      System.out.println("------------------------- allOverrides  -------------------");       
      shellmark.util.MethodID[] res = ch.allOverrides(subMethod);
      for(int i=0; i<res.length; i++)
         System.out.println(subMethod + " overrides or is overridden by " + res[i]);
   }

   static void testMethodRenameOK  (
                                    shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                    shellmark.util.MethodID origMethod,
                                    shellmark.util.MethodID newMethod)
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{
      System.out.println("------------------------- allOverrides  -------------------");       
      boolean  res = ch.methodRenameOK(origMethod, newMethod);
      if (res)
         System.out.println("It's OK to rename " + origMethod + " to " + newMethod);
      else
         System.out.println("It is NOT OK to rename " + origMethod + " to " + newMethod);
   }

   static void testGetMethodsToRename(shellmark.analysis.classhierarchy.ClassHierarchy ch,
                                      shellmark.util.MethodID origMethod) 
      throws shellmark.analysis.classhierarchy.ClassHierarchyException{

      System.out.println("------------------------- methodsToRename  -------------------");        
      shellmark.util.MethodID rename[] = ch.getMethodsToRename(origMethod);
      for (int i=0; i < rename.length; i++ ) {
         System.out.println("Rename:" + rename[i]);
      }
   }
    
   /*
    * big slow test
    */
    
   public static void test1 () throws Exception {
      String jarFile = "shellmark.jar";
      shellmark.program.Application app = new shellmark.program.Application(jarFile);
      shellmark.analysis.classhierarchy.ClassHierarchy ch = 
         new shellmark.analysis.classhierarchy.ClassHierarchy(app);       
           
      testExtends(ch,app.getClass("shellmark.util.newgraph.codec.ReduciblePermutationGraph"), 
                  app.getClass("shellmark.util.newgraph.codec.AbstractCodec"));
        
      testExtends(ch,app.getClass("shellmark.util.newgraph.Graph"), 
                  app.getClass("shellmark.util.newgraph.GraphImpl"));
        
      testSuperClasses(ch,app.getClass("shellmark.util.newgraph.codec.ReduciblePermutationGraph"));

      testInheritanceChain(ch,app.getClass("shellmark.program.Class"));
        
      shellmark.program.Class c = 
         shellmark.program.LibraryClass.find("java.lang.Object");
      /*for(int i = 0; i < c.getMethods().length; i++)
        System.out.println(c.getMethods()[i].getName() + ": " + 
        c.getMethods()[i].getSignature());*/
      shellmark.util.MethodID m1 = new shellmark.util.MethodID
         (app.getClass("shellmark.program.Object").
          getMethod("getName", "()Ljava/lang/String;"));
        
      shellmark.util.MethodID m2 = new shellmark.util.MethodID
         (app.getClass("shellmark.program.Object").
          getMethod("toString", "()Ljava/lang/String;"));
        
      shellmark.util.MethodID m3 = new shellmark.util.MethodID
         (app.getClass("shellmark.program.Method").
          getMethod("getName", "()Ljava/lang/String;"));

      shellmark.util.MethodID m4 = new shellmark.util.MethodID
         (c.getMethod("equals", "(Ljava/lang/Object;)Z"));   

      shellmark.util.MethodID m5 = new shellmark.util.MethodID
         (app.getClass("shellmark.program.Object").
          getMethod("delete", "()V"));
              
        
      testOverrides(ch, m1);        
      testAllOverrides(ch, m1);
      testAllOverrides(ch, m3);
      testAllOverrides(ch, m4);
      testOverrides(ch, m1, m1); 
      testOverrides(ch, m3, m1);         
           
      testMethodRenameOK(ch, m1, m2);   

      System.out.println("OK to rename " + m5 + " to 'newmeth'? " + 
                         ch.methodRenameOK(m5, "newmeth"));
      System.out.println("OK to rename " + m2 + " to 'newmeth'? " + 
                         ch.methodRenameOK(m2, "newmeth"));
      System.out.println("OK to rename " + m3 + " to 'newmeth'? " + 
                         ch.methodRenameOK(m3, "newmeth"));

      testGetMethodsToRename(ch, m3);       
   }
    

   /*
     Call like this:
     > /home/collberg/lib/j2sdk1.4.0/bin/java \
     -classpath .:../smextern3/BCEL.jar:../smextern3/bloat-1.0.jar \
     shellmark.analysis.classhierarchy.ClassHierarchy 

   */

   public static void main (String[] args)  {       
      if (args.length < 1){
         System.out.println("No arguments passed !!");
         System.out.println("You can pass args: <app1> <app2> ...");
         System.out.println("where each <app?> is a jar/class file");
         System.out.println("A class hierarchy containing classes " +
                            "from all <app?> files will be constructed");
         System.out.println("\nRunning tests on 'shellmark.jar'...");
         try{
            test1();
         }catch(Exception e){
            e.printStackTrace();
         }
         System.exit(1);
      }
      else {	 
         try {
            shellmark.program.Application app = new shellmark.program.Application(args[0]);
            shellmark.analysis.classhierarchy.ClassHierarchy ch = 
               new shellmark.analysis.classhierarchy.ClassHierarchy(app);
		
            for (int i = 1; i < args.length; i++) {
               app = new shellmark.program.Application(args[i]);
               ch.addApplication(app);
            }                               
            //System.out.println(ch.toDot());                
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      }        
   }    
}
