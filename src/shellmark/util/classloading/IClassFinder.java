package shellmark.util.classloading;

/**
 * An interface implemented by all classes suitable for use by
 * shellmark.util.classloading.ClassFinder as sources of class names
 @see shellmark.util.classloading.ClassFinder
 @author LiMin
 @version 1.0
*/

public interface IClassFinder {
    int ALGORITHM = 0;
    int GEN_OBFUSCATOR = 1;
    int APP_OBFUSCATOR = 2;
    int METHOD_OBFUSCATOR = 3;
    int CLASS_OBFUSCATOR = 4;
    int DYN_WATERMARKER = 5;
    int GEN_WATERMARKER = 6;
    int STAT_WATERMARKER = 7;
    int GRAPH_CODEC = 8;
    int METHOD_ALGORITHM = 9;
    int CLASS_ALGORITHM = 10;
    int APP_ALGORITHM = 11;
    int APP_METRIC = 12;
    int METHOD_METRIC = 13;
    int CLASS_METRIC = 14;
    int PREDICATE_GENERATOR = 15;
    int WRAPPER_CODEC = 16;
    int GEN_OPTIMIZER = 17;
    int APP_OPTIMIZER = 18;
    int METHOD_OPTIMIZER = 19;
    int CLASS_OPTIMIZER = 20;
    int GEN_BIRTHMARK = 21;
    int STAT_BIRTHMARK = 22;
    int DYN_BIRTHMARK = 23;
    int QUICK_PROTECT = 24;
    int CLASS_COUNT = 25;

    String CLASS_NAMES[] = new String[] {
    "shellmark.Algorithm",
    "shellmark.obfuscate.GeneralObfuscator",
    "shellmark.obfuscate.AppObfuscator",
    "shellmark.obfuscate.MethodObfuscator",
    "shellmark.obfuscate.ClassObfuscator",
    "shellmark.watermark.DynamicWatermarker",
    "shellmark.watermark.GeneralWatermarker",
    "shellmark.watermark.StaticWatermarker",
    "shellmark.util.newgraph.codec.GraphCodec",
    "shellmark.MethodAlgorithm",
    "shellmark.ClassAlgorithm",
    "shellmark.AppAlgorithm",
    "shellmark.metric.ApplicationMetric",
    "shellmark.metric.MethodMetric",
    "shellmark.metric.ClassMetric",
    "shellmark.util.opaquepredicatelib.OpaquePredicateGenerator",
    "shellmark.util.newgraph.codec.WrapperCodec",
    "shellmark.optimise.GeneralOptimizer",
    "shellmark.optimise.AppOptimizer",
    "shellmark.optimise.MethodOptimizer",
    "shellmark.optimise.ClassOptimizer",
    "shellmark.birthmark.GeneralBirthmark",
    "shellmark.birthmark.StaticClassBirthmark",
    "shellmark.birthmark.DynamicBirthmark",
    "shellmark.wizard.quickprotect.QuickProtect"
    };
    String CLASS_IDS[] = new String[] {
    "ALGORITHM",
    "GEN_OBFUSCATOR",
    "APP_OBFUSCATOR",
    "METHOD_OBFUSCATOR",
    "CLASS_OBFUSCATOR",
    "DYN_WATERMARKER",
    "GEN_WATERMARKER",
    "STAT_WATERMARKER",
    "GRAPH_CODEC",
    "METHOD_ALGORITHM",
    "CLASS_ALGORITHM",
    "APP_ALGORITHM",
    "APP_METRIC",
    "METHOD_METRIC",
    "CLASS_METRIC",
    "PREDICATE_GENERATOR",
    "WRAPPER_CODEC",
    "GEN_OPTIMIZER",
    "APP_OPTIMIZER",
    "METHOD_OPTIMIZER",
    "CLASS_OPTIMIZER",
    "GEN_BIRTHMARK",
    "STAT_BIRTHMARK",
    "DYN_BIRTHMARK",
    "QUICK_PROTECT",
    };
    /**
     * Get a Collection of String's containing names
     * of classes that derive from the type specified by
     * ancestor.
     * @param ancestor one of the constants above
     * @return Collection of String's containing names of classes derived from class specified by ancestor
     */
    java.util.Collection getClassesWithAncestor(int ancestor);

    /**
     * Get a string suitable for display to the user that describes className
     * @param className A String returned as a member of a Collection by getClassesWithAncestor
     * @return A short String description of className
     */
    String getClassShortname(String className);
}

