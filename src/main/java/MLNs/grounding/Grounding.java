package MLNs.grounding;

import MLNs.util.Bundle;
import MLNs.util.Shell;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/** create datebase probkb in pgAdmin4 */

public class Grounding {

    private final static Logger logger = LogManager.getLogger(Grounding.class);

    public static void ground(String base) throws FileNotFoundException {
        prepare(base);
        generate(base);
    }


    private static void  generate(String base) {
        String prefix="psql postgres://postgres:123456@localhost:5432/probkb -f " +
           //     System.getProperty("user.dir") +
                base;

        String Drop_schema = prefix  + "/pgsql/sql/drop.sql";

        // Create the probkb schema and tables.
        String create_schema_table = prefix  + "/pgsql/sql/create.sql";

        // Create quality control procedures.
        String quality_control = prefix  + "/pgsql/sql/qc.sql";

        String Load_csv = prefix  + "/load.sql";

        String Create_grounding = prefix  + "/pgsql/sql/ground.sql";

        String[] cmd = { // generate tables and procedures
                Drop_schema, create_schema_table, quality_control, Load_csv, Create_grounding};

        for (String c : cmd) {
            logger.debug("> " + c);
            Shell.execute(c, true);
        }

        logger.info("Grounding..."); // run scripts for grounding
        // Drop RUN.SQL
        String run_sql = prefix  + "/pgsql/sql/run.sql";

        logger.debug("> " + run_sql);
        Shell.execute(run_sql, true);


    }

    private static void prepare(String base) throws FileNotFoundException { // prepare SQL files

        PrintWriter load = new PrintWriter(
              //  System.getProperty("user.dir") +
                        base + "/load.sql");

        // write head
        write(//System.getProperty("user.dir")+
                 base + "/pgsql/sql/load-head.sql", load);

        // write graph tables
        String[] tables = {"classes", "entities", "relations", "entClasses",
                "relClasses", "functionals", "extractions",};
        // due to a stylistic choice from ProbKB, table `extractions`
        // corresponds to file `relationships.csv`
        String[] csv = {"classes", "entities", "relations", "entClasses",
                "relClasses", "functionals", "relationships",};
        for (int i = 0; i < tables.length; i++)
            load.write("COPY probkb." + tables[i] + " FROM '"
                    	//+ System.getProperty("user.dir")
                    + base + "/"
                    + csv[i] + ".csv' DELIMITERS ',' CSV;\n");

        // write body
        write(//System.getProperty("user.dir")+
                 base + "/pgsql/sql/load-body.sql", load);

        // write MLN tables
        for (int i = 1; i <= 6; i++)
            load.write("COPY probkb.mln" + i + " FROM '"
                  //  + System.getProperty("user.dir") //+ "/"
                    + base + "/mln" + i
                    + ".csv' DELIMITERS ',' CSV;\n");

        // write tail
        write(//System.getProperty("user.dir")+
                 base + "/pgsql/sql/load-tail.sql", load);
        load.close();

    }

    private static void write(String filename, PrintWriter pw) throws FileNotFoundException {

        Scanner in = new Scanner(new File(filename));
        while (in.hasNextLine())
            pw.write(in.nextLine() + "\n");
        in.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        BasicConfigurator.configure();
        String base = System.getProperty("user.dir")+"/knowledgeClean-data/MLN";
        ground(base);

    }

}
