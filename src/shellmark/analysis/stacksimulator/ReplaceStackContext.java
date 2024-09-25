package shellmark.analysis.stacksimulator;

class ReplaceStackContext extends Context {
	   private Context c;
	   private int index;
	   private int size;
	   private StackData[] data;

	   ReplaceStackContext(Context c, int index, StackData[] data) {
	      this.c = c;
	      this.index = index;
	      this.data = data;
	      size = c.getStackSize();
	      if (index < 0 || index >= size)
		 throw new IndexOutOfBoundsException();
	   }

	   public int getStackSize() {
	      return size;
	   }

	   public int getLocalVariableCount() {
	      return c.getLocalVariableCount();
	   }

	   public StackData[] getStackAt(int index) {
	      if (index == this.index)
		 return data;
	      else
		 return c.getStackAt(index);
	   }

	   public StackData[] getLocalVariableAt(int index) {
	      return c.getLocalVariableAt(index);
	   }

	   public int depth() { return 1 + c.depth(); }
	}
