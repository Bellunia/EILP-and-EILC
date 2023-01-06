package MLNs;

import MLNs.Controller.*;
import MLNs.grounding.Grounding;
import MLNs.inference.ProbKBToRockitGibbsSampling;
import MLNs.model.PredictionSet;
import MLNs.model.RDFToTSV;
import MLNs.reasoner.RDFSReasoner;
import MLNs.rulemining.RuleMiner;
import MLNs.util.SetUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Scanner;

import static MLNs.grounding.Grounding.ground;

public class test_mln {

    private final static Logger logger = LogManager.getLogger(test_mln.class);
    private static final int THETA_MIN = 0;
    private static final int THETA_MAX = 5;

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();

      new test_mln().run();
    }

    public void run() throws Exception {


        String workspace=System.getProperty("user.dir")+"/knowledgeClean-data/MLN";
        boolean enableOnt= true;
        boolean enableFwc=true;

        String path="./knowledgeClean-data/data_MLN/benchmark/fb15k/freebase_mtr100_mte100-train.nt,"
                +"./knowledgeClean-data/data_MLN/benchmark/fb15k/freebase_mtr100_mte100-valid.nt";
        String path1="./knowledgeClean-data/data_MLN/benchmark/wn18/wordnet-mlj12-train.nt," +
                "./knowledgeClean-data/data_MLN/benchmark/wn18/wordnet-mlj12-valid.nt";

        String[] inputPaths =path.split(",");

        Double mining = 0.8;

   //     String[] simVal = {"-1", "-1", "-1"};

   //     boolean enableSim=true;

        // create working directory
     //   new File(workspace).mkdirs();

    //    if(enableOnt) {
            // inputs -> model-tmp.nt
            OntoImporter.run(workspace, inputPaths);
  //      }

        // inputs (or model-tmp.nt) -> model.nt (or model-fwc.nt)
        Validator.run(workspace, inputPaths, enableFwc, enableOnt);
        if(enableFwc) {
            // model.nt -> model-fwc.nt
            RDFSReasoner.run(workspace);
        }
         String aim="*";
        NameMapper map = new NameMapper(aim);

        // model-fwc.nt -> map (classes)
        Classes.build(map, workspace);
        // model-fwc.nt -> map (other)

            Evidence.build(map, workspace);

            map.pretty();

        logger.info("# entClasses: " + map.getEntClasses().size());
        logger.info("# relClasses: " + map.getRelClasses().size());
        logger.info("# relationships: " + map.getRelationships().size());

        // map -> KB description csv
        ProbKBData.buildCSV(map, workspace);

        // model-fwc.nt -> model.tsv
        RDFToTSV.run(workspace);
        // model.tsv -> MLN csv
        int maxRules= 1500;
        RuleMiner.run(map, workspace, mining, maxRules);

//		// csv -> Postgre factors
		Grounding.ground(workspace);//----grounding need postgre
        Integer sampling= 1000000;

        // Postgre factors -> predictions
        PredictionSet pset = new ProbKBToRockitGibbsSampling(map).infer(sampling);

        pset.saveTo(workspace + "/predictions.dat");

        for(int th=THETA_MIN; th<=THETA_MAX; th+=1) {
            double theta = th / 10.0;
            logger.info("theta = "+theta);

            // get set of predicted (just outputted) links
            String knowledge = workspace + "/model-fwc.nt";
            String predicted = workspace + "/output_" + theta + ".nt";
            pset.saveLinkset(map, theta, predicted);

            // compute set of discovered (emergent) links
            String discovered = workspace + "/discovered_" + theta + ".nt";
            SetUtils.minus(predicted, knowledge, discovered);
            logger.debug("+++ DISCOVERED +++");
            Scanner in = new Scanner(new File(discovered));
            int size = 0;
            while(in.hasNextLine()) {
                logger.debug(in.nextLine());
                size++;
            }
            in.close();
            logger.info("Discovered triples size: "+size);
        }


        logger.info("Mandolin done.");

    }
}
