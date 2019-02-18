package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import flow.Flow;

public class RedundantNull implements Flow.Analysis {

  public static class VarSet implements Flow.DataflowObject {
      private Set<String> set;
      public static Set<String> universalSet;

      public VarSet() { set = new TreeSet<String>(universalSet); }

      public void setToTop() { set = new TreeSet<String>(universalSet); }
      public void setToBottom() { set = new TreeSet<String>(); }

      public void meetWith (Flow.DataflowObject o)
      {
          VarSet a = (VarSet)o;
          set.retainAll(a.set);
      }
      public void copy (Flow.DataflowObject o)
      {
          VarSet a = (VarSet) o;
          set = new TreeSet<String>(a.set);
      }
      @Override
      public boolean equals(Object o)
      {
          if (o instanceof VarSet)
          {
              VarSet a = (VarSet) o;
              return set.equals(a.set);
          }
          return false;
      }
      @Override
      public int hashCode() {
          return set.hashCode();
      }

      @Override
      public String toString()
      {
          return set.toString();
      }

      public void setChecked(String v) {
          set.add(v);
      }

      public void setNotChecked(String v) {
          set.remove(v);
      }

      public boolean isChecked(String v) {
          return set.contains(v);
      }
  }

  private VarSet[] in, out;
  private VarSet entry, exit;

  public void preprocess(ControlFlowGraph cfg) {
      // get the amount of space we need to allocate for the in/out arrays.
      QuadIterator qit = new QuadIterator(cfg);
      int max = 0;
      while (qit.hasNext()) {
          int id = qit.next().getID();
          if (id > max)
              max = id;
      }
      max += 1;

      // Begin computing the universal set. This needs to be done before
      // any VarSet objects are created.
      Set<String> s = new TreeSet<String>();

      /* Arguments are always there. */
      int numargs = cfg.getMethod().getParamTypes().length;
      for (int i = 0; i < numargs; i++) {
          s.add("R"+i);
      }

      qit = new QuadIterator(cfg);
      while (qit.hasNext()) {
          Quad q = qit.next();
          for (RegisterOperand use : q.getUsedRegisters()) {
              if (q.getOperator() instanceof Operator.NullCheck) {
                  s.add(use.getRegister().toString());
              }
          }
      }
      VarSet.universalSet = s;
      // End computing the universal set

      // allocate the in and out arrays.
      in = new VarSet[max];
      out = new VarSet[max];

      // initialize the contents of in and out.
      qit = new QuadIterator(cfg);
      while (qit.hasNext()) {
          int id = qit.next().getID();
          in[id] = new VarSet();
          out[id] = new VarSet();
          in[id].setToBottom();
      }

      // initialize the entry and exit points.
      entry = new VarSet();
      entry.setToBottom();
      exit = new VarSet();

      transferfn.val = new VarSet();
  }

  public void postprocess (ControlFlowGraph cfg) {
    // System.out.println("entry: " + entry.toString());
    // for (int i=0; i<in.length; i++) {
    //     if (in[i] != null) {
    //         System.out.println(i + " in:  " + in[i].toString());
    //         System.out.println(i + " out: " + out[i].toString());
    //     }
    // }
    // System.out.println("exit: " + exit.toString());
      System.out.print(cfg.getMethod().getName().toString() + " ");
      QuadIterator qit = new QuadIterator(cfg);
      while (qit.hasNext()) {
        Quad q = qit.next();
        if ((q.getOperator() instanceof Operator.NullCheck) &&
            in[q.getID()].isChecked(q.getUsedRegisters().iterator().next().getRegister().toString())) {
              System.out.print(q.getID());
              System.out.print(" ");
        }
      }
      System.out.println();
  }

  public boolean isForward () { return true; }
  public Flow.DataflowObject getEntry()
  {
      Flow.DataflowObject result = newTempVar();
      result.copy(entry);
      return result;
  }
  public Flow.DataflowObject getExit()
  {
      Flow.DataflowObject result = newTempVar();
      result.copy(exit);
      return result;
  }
  public void setEntry(Flow.DataflowObject value)
  {
      entry.copy(value);
  }
  public void setExit(Flow.DataflowObject value)
  {
      exit.copy(value);
  }
  public Flow.DataflowObject getIn(Quad q)
  {
      Flow.DataflowObject result = newTempVar();
      result.copy(in[q.getID()]);
      return result;
  }
  public Flow.DataflowObject getOut(Quad q)
  {
      Flow.DataflowObject result = newTempVar();
      result.copy(out[q.getID()]);
      return result;
  }
  public void setIn(Quad q, Flow.DataflowObject value)
  {
      in[q.getID()].copy(value);
  }
  public void setOut(Quad q, Flow.DataflowObject value)
  {
      out[q.getID()].copy(value);
  }
  public Flow.DataflowObject newTempVar() { return new VarSet(); }

  private TransferFunction transferfn = new TransferFunction ();
  public void processQuad(Quad q) {
      transferfn.val.copy(in[q.getID()]);
      transferfn.visitQuad(q);
      out[q.getID()].copy(transferfn.val);
  }

  public static class TransferFunction extends QuadVisitor.EmptyVisitor {
      VarSet val;
      @Override
      public void visitQuad(Quad q) {
          if (q.getOperator() instanceof Operator.NullCheck) {
              RegisterOperand used = q.getUsedRegisters().iterator().next();
              val.setChecked(used.getRegister().toString());
          }
          RegisterOperand def = q.getDefinedRegisters().iterator().next();
          val.setNotChecked(def.getRegister().toString());
      }
  }
}
