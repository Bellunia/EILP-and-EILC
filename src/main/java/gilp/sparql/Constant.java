package gilp.sparql;

public class Constant {
  public static String CONF_FILE = "ruge_embedding/main/config/Configuration.xml";

  // parameter paths of the conf file
  public static String CONF_LOGGER = "logfile";

  public static String GREATER_EQUAL_REL = ">=";
  public static String LESS_EQUAL_REL = "<=";
  public static String GREATER_REL = ">";
  public static String LESS_REL = "<";
  public static String EQUAL_REL = "=";
  public static String DIFF_REL = "!=";

  public static String CONF_NUM_THREADS = "naive.runtime.num_threads";
  public static String CONF_VALIDATION_THRESHOLD = "naive.runtime.score.validation_threshold";//0.2
  public static String CONF_SCORE_ALPHA = "naive.runtime.score.alpha";//0.3
  public static String CONF_SCORE_BETA = "naive.runtime.score.beta";//0.7
  public static String CONF_SCORE_GAMMA = "naive.runtime.score.gamma";//0.1
  public static String CONF_MAX_RULE_LEN = "naive.runtime.max_rule_lenght";

  public static String CONF_SPARQL_ENGINE = "naive.sparql";
  public static String CONF_RELATION_PREFIX = "relation_prefix.prefix";
  public static final String CONF_RELATION_PREFIX_NAME = "name";
  public static final String CONF_RELATION_PREFIX_URI = "uri";
  public static String CONF_TYPE_PREFIX = "types.type_prefix";//http://www.w3.org/1999/02/22-rdf-syntax-ns#type
  public static String CONF_RELATION_TARGET_REFIX = "relation_target_prefix.prefix";//http://dbpedia.org/ontology/
  public static String CONF_GRAPH_IRI = "graph_iri";//http://dbpedia.org
  public static String CONF_RELATION_TO_AVOID = "relation_to_avoid.relation";
  public static String CONF_GENERIC_TYPES = "generic_types.type";
  public static String CONF_INCLUDE_LITERALS = "include_literals";
  public static String CONF_SPARQL_ENDPOINT = "parameters.sparql_endpoint";//http://dbpedia.org/sparql
  // limits
  public static String CONF_SUBJECT_LIMIT = "limits.edges.subject";
  public static String CONF_OBJECT_LIMIT = "limits.edges.object";
  public static String CONF_POS_EXAMPLES_LIMIT = "limits.examples.positive";
  public static String CONF_NEG_EXAMPLES_LIMIT = "limits.examples.negative";

  public static String CONF_EQUALITY_TYPES_NUMBER = "naive.disequality_relation.equality_types_number";//0

}
