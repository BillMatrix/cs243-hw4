package submit;

import joeq.Class.jq_Class;
import joeq.Main.Helper;
import submit.MySolver4;
import submit.NullCheck;
import joeq.Class.*;
import joeq.Compiler.Quad.*;
import flow.Flow.*;


public class FindRedundantNullChecks {

    public static void main(String[] args) {
	String usage = "USAGE: Flow solver-class analysis-class [test-class]+";
	jq_Class[] classes = new jq_Class[args.length];
	for (int i=0; i < classes.length; i++){
	    classes[i] = (jq_Class)Helper.load(args[i]);
	}
	Solver solver;
	Object solver_obj = new MySolver4();
	solver = (Solver) solver_obj;

        // get an instance of the analysis class.
        Analysis analysis;
        try {
            Object analysis_obj = new NullCheck();
            analysis = (Analysis) analysis_obj;
        } catch (Exception ex) {
            System.out.println(usage);
            return;
        }
        // register the analysis with the solver.
        solver.registerAnalysis(analysis);
	
	// visit each of the specified classes with the solver.
	for (int i=0; i < classes.length; i++) {
	    Helper.runPass(classes[i], solver);
	}
    }    
}
