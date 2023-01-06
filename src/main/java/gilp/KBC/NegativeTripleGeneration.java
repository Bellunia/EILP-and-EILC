package gilp.KBC;

import java.io.RandomAccessFile;

import java.util.HashMap;




public class NegativeTripleGeneration {


    public static HashMap<String, Integer> MapRelationID = null;

    public static HashMap<String, Integer> MapEntityID = null;




    public static HashMap<String, Integer> getMapRelationID() {
        // in this file, we have some name that we need to do the second feedback with
        // more information.

        if (MapRelationID == null) {
            try {
                RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/familyNameInAsian", "r");
                MapRelationID = new HashMap<String, Integer>();
                String line = null;
                while ((line = file.readLine()) != null) {

                    String[] columns = line.split("\t");

                    MapRelationID.put(columns[0], Integer.parseInt(columns[1]));
                }
                file.close();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        return MapRelationID;

    }



    public static HashMap<String, Integer> getMapEntityID() {
        // in this file, we have some name that we need to do the second feedback with
        // more information.

        if (MapEntityID == null) {
            try {
                RandomAccessFile file = new RandomAccessFile(getRootPath() + "/data/yago/familyNameInAsian", "r");
                MapEntityID = new HashMap<String, Integer>();
                String line = null;
                while ((line = file.readLine()) != null) {

                    String[] columns = line.split("\t");

                    MapEntityID.put(columns[0], Integer.parseInt(columns[1]));
                }
                file.close();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        return MapEntityID;

    }

    public static String getRootPath() {
        return System.getenv("GILP_HOME");
    }
}