/*
 * Created on 9/27
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package shellmark.program.util;

/**
 * @author LiMin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class AllMethods implements java.util.Iterator {
    private Object next;
    private java.util.Iterator methodIt = new java.util.LinkedList().iterator();
    private java.util.Iterator classIt;
    public AllMethods(shellmark.program.Application app) {
        classIt = app.classes();
        setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return next != null;
    }

    public Object next() {
        Object n = next;
        setNext();
        return n;
    }
    private void setNext() {
        next = null;
        if(!methodIt.hasNext() && !classIt.hasNext())
            return;
        if(!methodIt.hasNext())
            methodIt = ((shellmark.program.Class)classIt.next()).methods();
        next = methodIt.next();
    }

}