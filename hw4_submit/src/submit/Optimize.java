package submit;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;
import submit.*;
import flow.Flow.*;
import flow.*;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));

      	    Solver solver;
      	    Object solver_obj = new MySolver4();
      	    solver = (Solver) solver_obj;
    	      if (nullCheckOnly){
            		Analysis analysis = new NullCheckOptimizer();
            		solver.registerAnalysis(analysis);
            		Helper.runPass(classes, solver);
    	      } else {
          		  Analysis analysis_op = new NullCheckOptimizedOptimizer();
          		  solver.registerAnalysis(analysis_op);
          		  Helper.runPass(classes, solver);
          		  Analysis analysis_cp = new ConstantPropOptimizer();
          		  solver.registerAnalysis(analysis_cp);
          		  Helper.runPass(classes, solver);
          		  //for (int j = 0; j < 2; j++){
          		  Analysis analysis_dc = new DeadCodeEliminator();
          		  solver.registerAnalysis(analysis_dc);
          		  Helper.runPass(classes, solver);
          		  //}
    	      }
	}
   }
}
