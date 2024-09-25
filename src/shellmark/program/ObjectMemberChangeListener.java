package shellmark.program;

/*
 * Created on 9/23
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */


/**
 * @author LiMin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ObjectMemberChangeListener {
	void addedObject(shellmark.program.Object parent, shellmark.program.Object added);
	void deletingObject(shellmark.program.Object parent, shellmark.program.Object deleted);
	void copiedObject(shellmark.program.Object parent, shellmark.program.Object orig,shellmark.program.Object copy);
}
