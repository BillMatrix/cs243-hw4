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
        // try {
        //     Object solver_obj = Class.forName(solver_name).newInstance();
        //     solver = (Solver) solver_obj;
        // } catch (Exception ex) {
        //     System.out.println("ERROR: Could not load class '" + solver_name +
        //         "' as Solver: " + ex.toString());
        //     return;
        // }

        String analysis_name = "submit.RedundantNull";
        RedundantNull analysis = new RedundantNull();
        // try {
        //     Object analysis_obj = Class.forName(analysis_name).newInstance();
        //     analysis = (Analysis) analysis_obj;
        // } catch (Exception ex) {
        //     System.out.println("ERROR: Could not load class '" + analysis_name +
        //         "' as Analysis: " + ex.toString());
        //     return;
        // }

        jq_Class[] classes = new jq_Class[args.length];
        for (int i=0; i < classes.length; i++)
            classes[i] = (jq_Class)Helper.load(args[i]);

        solver.registerAnalysis(analysis);

        for (int i=0; i < classes.length; i++) {
            System.out.println("Now analyzing " + classes[i].getName());
            Helper.runPass(classes[i], solver);
        }
    }
}
