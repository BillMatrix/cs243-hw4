package submit;

import joeq.Compiler.Quad.*;
import flow.Flow;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import java.util.*;

/**
 * Skeleton class for implementing a faint variable analysis
 * using the Flow.Analysis interface.
 */
public class NullCheck implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the Faintness analysis.
     * Based very closely on the class flow.Liveness.VarSet
     */
    public static class VarSet implements Flow.DataflowObject {
        private Set<String> set;
        public static Set<String> universalSet;
        /**
         *The default value has all registers unchecked
         */
        public VarSet() { set = new TreeSet<String>(universalSet); }

        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * 
         * Most are similar to the methods in flow.Liveness.VarSet
         */
        public void setToTop() { set = new TreeSet<String>(universalSet); }
        public void setToBottom() { set = new TreeSet<String>(); }

        /**
         * Meet is an intersection
         */
        public void meetWith (Flow.DataflowObject o)
        {
            VarSet a = (VarSet)o;
            set.retainAll(a.set); // strange name for intersect
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

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[REG0, REG1, REG2, ...]", where each REG is
         * the identifier of a register, and the list of REGs must be sorted.
         * See src/test/TestFaintness.out for example output of the analysis.
         * The output format of your reaching definitions analysis must
         * match this exactly.
         */
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

        public boolean isRedundant(String v) {
            return set.contains(v);
        }
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private VarSet[] in, out;
    private VarSet entry, exit;

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
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
        VarSet.universalSet = s;

        /* Arguments are always there. */
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
	    if (q.getOperator() instanceof Operator.NullCheck) {
		for (RegisterOperand use : q.getUsedRegisters()) {
		    s.add(use.getRegister().toString());
		}
	    }
        }
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
        }

        // initialize the entry and exit points.
        entry = new VarSet();
        exit = new VarSet();
	entry.setToBottom();
	
        transferfn.val = new VarSet();
    }

    /**
     * This method is called after the fixpoint is reached.
     * It must print out the dataflow objects associated with
     * the entry, exit, and all interior points of the CFG.
     * Unless you modify in, out, entry, or exit you shouldn't
     * need to change this method.
     *
     * @param cfg  Unused.
     */
    public void postprocess (ControlFlowGraph cfg) {
	System.out.print(cfg.getMethod().getName().toString());
	SortedSet<Integer> redundant = new TreeSet<Integer>();
        QuadIterator qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
	    Quad q = qit.next();
	    if (q.getOperator() instanceof Operator.NullCheck) {
		if (in[q.getID()].equals(out[q.getID()])){
		    redundant.add(q.getID());
		}
            }
	}

	for (int id : redundant){
	    System.out.print(" " + id);
	}
	System.out.println();
    }

    /**
     * Other methods from the Flow.Analysis interface.
     * See Flow.java for the meaning of these methods.
     * 
     * These implementations essentially copied from flow.Livenesss
     */
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

    /* Actually perform the transfer operation on the relevant
     * quad. Propagation is backward. */
    private TransferFunction transferfn = new TransferFunction ();
    public void processQuad(Quad q) {
        transferfn.val.copy(in[q.getID()]);
        transferfn.visitQuad(q);
        out[q.getID()].copy(transferfn.val);
    }

    /* The QuadVisitor that actually does the computation */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        VarSet val;
        @Override
        public void visitQuad(Quad q) {
	    for (RegisterOperand def : q.getDefinedRegisters()){
		val.setNotChecked(def.getRegister().toString());
	    }
            if (q.getOperator() instanceof Operator.NullCheck) {
		for (RegisterOperand use : q.getUsedRegisters()) {
		    val.setChecked(use.getRegister().toString());
		}
            }
        }
    }
}
