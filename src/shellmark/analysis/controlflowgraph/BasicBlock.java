package shellmark.analysis.controlflowgraph;

/**
 * CFG中的基本块
 * BasicBlock represents a basic block in a control flow graph. Some of 
 * the ideas in this class are based upon the BLOAT project from Purdue.
 *
 * Written by LiMin 9/24
 */
public class BasicBlock implements shellmark.util.newgraph.LabeledNode {
   java.util.ArrayList instructions; //This block's list of instructions

   BasicBlock fallthroughTo;
   BasicBlock fallthroughFrom;

   MethodCFG graph; //类Method

   public int mBlockNum;
   
   //初始化基本块
   public BasicBlock(MethodCFG graph) {
      this.graph = graph;
      instructions = new BBArrayList(this);
      fallthroughTo = fallthroughFrom = null;
      mBlockNum = graph.mBlockCounter++;
   }

   //返回该基本块所属的控制流图对象
   public MethodCFG graph() {
      return graph;
   }

   //设置该基本块的顺序执行流向的下一个基本块（fallthroughto）
   public void setFallthrough(BasicBlock fallthrough) {
      if(fallthroughTo != null)
         fallthroughTo.fallthroughFrom = null;
      fallthroughTo = fallthrough;
      if(fallthrough != null)
         fallthrough.fallthroughFrom = this;
   }
    
    //返回当前基本块的顺序执行流向的下一个基本块fallthroughti
   public BasicBlock fallthrough() {
      return fallthroughTo;
   }

   //返回当前基本块的顺序流动的卡一个基本块fallthrougghfrom
   public BasicBlock fallthroughFrom() {
      return fallthroughFrom;
   }

   /**
    * Returns the instruction handler associated with this block.
    * 返回当前基本块中第一条指令的句柄
    * @return The instruction handle.
    */
   public org.apache.bcel.generic.InstructionHandle getIH(){
      return instructions.size() > 0 ? 
         (org.apache.bcel.generic.InstructionHandle)instructions.get(0) :
         null;
   }

   /**
    * Returns the instruction handler associated with the last 
    * instruction in the block.
    *返回当前基本块中的最后一条指令的句柄
    * @return The instruction handle.
    */
   public org.apache.bcel.generic.InstructionHandle getLastInstruction(){
      int listSize = instructions.size();
      return listSize > 0 ? (org.apache.bcel.generic.InstructionHandle)instructions.get(listSize - 1) : null;
   }

   //检查该基本块的指令列表中是否包含特定的指令句柄
   public boolean containsIH(
                             org.apache.bcel.generic.InstructionHandle searchHandle){
      return instructions.contains(searchHandle);
   }

   /**
    * 将一条指令添加到当前基本块的指令列表中
    * Adds an instruction to this basic block's instruction list.
    */
   public void addInst(org.apache.bcel.generic.InstructionHandle inst){
      instructions.add(inst);
   }

   /**
    * Returns this blocks instruction list.
    * 返回该基本块中的指令列表
    * @return The instruction list.
    */
   public java.util.ArrayList getInstList(){
      return instructions;
   }

   /**
    * 返回当前基本块的字符串表示形式
    * Returns a string representation of the block.
    */
   public String toString(int limit){
      String s = "<block " + mBlockNum;
      MethodCFG cfg = graph();      
    
      if(this == cfg.source()){ 
         return s += " source>";
      }else if(this == cfg.sink()){
         return s += " sink>";
      }else{
         s += "\n";
         if(limit != -1 && instructions.size() > limit) {
            s += instructions.get(0);
            s += "\n ... \n";
            s += instructions.get(instructions.size() - 1);
            s += "\n";
         } else {
            for(int i=0; i<instructions.size(); i++){
               s += instructions.get(i);
               s += "\n";
            }
         }
         return s + ">";
      }
   }
   //重载的 toString 方法，默认调用 toString(5)，即只显示前5条指令
   public String toString() { return toString(5); }
   //返回该基本块的哈希码（即基本块的编号 mBlockNum）
   public int hashCode() { return mBlockNum; }
   //返回当前基本块的完整标签，调用 toString(-1)，即不限制指令数量
   public String getLongLabel() {
      return toString(-1);//.replaceAll("\n","\\l");
   }
   //返回当前基本块的简短标签，调用 toString()，默认显示前5条指令
   public String getShortLabel() { 
      return toString();//.replaceAll("\n","\\l"); 
   }

   /////////////////////////////////////////////////////

   private static class BBArrayList extends java.util.ArrayList{
      private BasicBlock BB;

      public BBArrayList(BasicBlock bb){
         BB = bb;
      }

      public void add(int index, Object o){
         if (index>=0 && index<=size() && o!=null){
            BB.graph.instr2bb.put(o, BB);
         }
         super.add(index,o);
      }

      public boolean add(Object o){
         if (o!=null){
            BB.graph.instr2bb.put(o,BB);
         }
         return super.add(o);
      }

      public boolean addAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               BB.graph.instr2bb.put(iter.next(), BB);
            }
         }
         return super.addAll(c);
      }

      public boolean addAll(int index, java.util.Collection c){
         if (c!=null && index>=0 && index<=size()){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               BB.graph.instr2bb.put(iter.next(), BB);
            }
         }
         return super.addAll(index, c);
      }

      public void clear(){
         for (java.util.Iterator iter=iterator();iter.hasNext();){
            BB.graph.instr2bb.remove(iter.next());
         }
         super.clear();
      }

      public Object remove(int index){
         if (index>=0 && index<size()){
            BB.graph.instr2bb.remove(get(index));
         }
         return super.remove(index);
      }

      protected void removeRange(int fromIndex, int toIndex){
         if (fromIndex>=0 && fromIndex<size() && 
             toIndex>=fromIndex && toIndex<=size()){
            for (int i=fromIndex;i<toIndex;i++){
               BB.graph.instr2bb.remove(get(i));
            }
         }
         
         super.removeRange(fromIndex, toIndex);
      }

      public Object set(int index, Object element){
         if (index>=0 && index<size()){
            BB.graph.instr2bb.remove(get(index));
            BB.graph.instr2bb.put(element, BB);
         }
         return super.set(index, element);
      }

      public boolean remove(Object o){
         boolean result = super.remove(o);
         if (result){
            BB.graph.instr2bb.remove(o);
         }
         return result;
      }

      public boolean removeAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=c.iterator();iter.hasNext();){
               Object obj = iter.next();
               if (contains(obj)){
                  BB.graph.instr2bb.remove(obj);
               }
            }
         }
         return super.removeAll(c);
      }

      public boolean retainAll(java.util.Collection c){
         if (c!=null){
            for (java.util.Iterator iter=iterator();iter.hasNext();){
               Object obj= iter.next();
               if (!c.contains(obj)){
                  BB.graph.instr2bb.remove(obj);
               }
            }
         }
         return super.retainAll(c);
      }
   }
}

