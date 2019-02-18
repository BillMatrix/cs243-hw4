package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.*;
import java.lang.Object;

/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);

        if (this.analysis.isForward()) {
          forwardPass(cfg);
        } else {
          backwardPass(cfg);
        }

        // this needs to come last.
        analysis.postprocess(cfg);
    }

    private void forwardPass(ControlFlowGraph cfg) {
      boolean changed = true;
      Flow.DataflowObject top = this.analysis.newTempVar();
      top.setToTop();
      Flow.DataflowObject bottom = this.analysis.newTempVar();
      bottom.setToBottom();

      // this.analysis.setEntry(top);

      while (changed) {
        QuadIterator qit = new QuadIterator(cfg);
        changed = false;
        while (qit.hasNext()) {
          Quad q = qit.next();
          Flow.DataflowObject oldIn = this.analysis.newTempVar();
          oldIn.copy(this.analysis.getIn(q));

          Flow.DataflowObject oldOut = this.analysis.newTempVar();
          oldOut.copy(this.analysis.getOut(q));

          Iterator predecessor_it = qit.predecessors();
          Flow.DataflowObject tempIn = this.analysis.newTempVar();
          tempIn.setToTop();
          // tempIn.copy(this.analysis.getIn(q));
          while (predecessor_it.hasNext()) {
            Quad next_predecessor = (Quad) predecessor_it.next();
            if (next_predecessor != null) {
              tempIn.meetWith(this.analysis.getOut(next_predecessor));
            } else {
              tempIn.meetWith(this.analysis.getEntry());
            }
          }
          this.analysis.setIn(q, tempIn);
          this.analysis.processQuad(q);
          if (!(this.analysis.getOut(q).equals(oldOut))) {
            changed = true;
          }
        }
      }

      Flow.DataflowObject finalObjects = this.analysis.newTempVar();
      finalObjects.setToTop();
      QuadIterator fqit = new QuadIterator(cfg);
      while (fqit.hasNext()) {
        Quad q = fqit.next();
        Iterator successors = fqit.successors();
        while (successors.hasNext()) {
          Quad next_successor = (Quad) successors.next();
          if (next_successor == null) {
            finalObjects.meetWith(this.analysis.getOut(q));
          }
        }
      }
      this.analysis.setExit(finalObjects);
    }

    private void backwardPass(ControlFlowGraph cfg) {
      boolean changed = true;

      Flow.DataflowObject top = this.analysis.newTempVar();
      top.setToTop();
      Flow.DataflowObject bottom = this.analysis.newTempVar();
      bottom.setToBottom();

      // this.analysis.setExit(top);

      // QuadIterator iqit = new QuadIterator(cfg);
      // while (iqit.hasNext()) {
      //   Quad q = iqit.next();
      //   this.analysis.setOut(q, top);
      // }

      while (changed) {
        QuadIterator qit = new QuadIterator(cfg);
        changed = false;
        while (qit.hasNext()) {
          qit.next();
        }
        while (qit.hasPrevious()) {
          Quad q = qit.previous();
          Flow.DataflowObject oldIn = analysis.newTempVar();
          oldIn.copy(this.analysis.getIn(q));

          Flow.DataflowObject oldOut = analysis.newTempVar();
          oldOut.copy(this.analysis.getOut(q));

          Iterator successor_it = qit.successors();
          Flow.DataflowObject tempOut = analysis.newTempVar();
          tempOut.setToTop();
          // tempOut.copy(this.analysis.getOut(q));
          while (successor_it.hasNext()) {
            Quad next_successor = (Quad) successor_it.next();
            if (next_successor != null) {
              tempOut.meetWith(this.analysis.getIn(next_successor));
            } else {
              tempOut.meetWith(this.analysis.getExit());
            }
          }
          this.analysis.setOut(q, tempOut);
          this.analysis.processQuad(q);
          if (!(this.analysis.getIn(q).equals(oldIn))) {
            changed = true;
          }
        }
      }

      Flow.DataflowObject finalObjects = this.analysis.newTempVar();
      finalObjects.setToTop();
      QuadIterator fqit = new QuadIterator(cfg);
      while (fqit.hasNext()) {
        Quad q = fqit.next();
        Iterator predecessors = fqit.predecessors();
        while (predecessors.hasNext()) {
          Quad next_predecessor = (Quad) predecessors.next();
          if (next_predecessor == null) {
            finalObjects.meetWith(this.analysis.getIn(q));
          }
        }
      }
      this.analysis.setEntry(finalObjects);
    }
}
