package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;

public class ConstantPropOptimizer implements Flow.Analysis {

    public static class CopyList implements Flow.DataflowObject {
        private ArrayList<ArrayList<String>> universalCopies;

        public CopyList() {}
    }
}
