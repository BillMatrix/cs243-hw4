package submit;

import joeq.Main.Helper;
import joeq.Class.*;
import joeq.Compiler.Quad.*;
import flow.Flow.*;
import submit.Hw4Solver;
import submit.RedundantNull;

public class FindRedundantNullChecks {

    /*
     * args is an array of class names
     * method should print out a list of quad ids of redundant null checks
     * for each function as described on the course webpage
     */
    public static void main(String[] args) {
        String solver_name = "submit.Hw4Solver";
        Hw4Solver solver = new Hw4Solver();

        String analysis_name = "submit.RedundantNull";
        RedundantNull analysis = new RedundantNull();

        jq_Class[] classes = new jq_Class[args.length];
        for (int i=0; i < classes.length; i++)
            classes[i] = (jq_Class)Helper.load(args[i]);

        solver.registerAnalysis(analysis);

        for (int i=0; i < classes.length; i++) {
            Helper.runPass(classes[i], solver);
        }
    }
}
