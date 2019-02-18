package submit;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));

            Hw4Solver solver = new Hw4Solver();

            RedundantNull analysis = new RedundantNull();

            analysis.disablePrint();

            solver.registerAnalysis(analysis);

            Helper.runPass(Helper.load(classes), solver);
        }
    }
}
