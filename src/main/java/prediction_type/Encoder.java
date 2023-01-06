package prediction_type;

import Exception.experiment.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Encoder {
    static Map<String, String> entity2Id, id2Entity;

    static int entityCount, typeCount, predicateCount;

    static void encode(String entity, int entityType) {
        entity = entity.trim().replaceAll("\\s+", " ");
        //\s+匹配任何空白字符，包括空格、制表符、换页符等等, 等价于[ \f\n\r\t\v]
        if (entity2Id.containsKey(entity))
            return;
        if (entityType == 0) {
            entityCount++;
            entity2Id.put(entity, "e" + entityCount);
        } else if (entityType == 1) {
            typeCount++;
            entity2Id.put(entity, "t" + typeCount);
        } else {
            predicateCount++;
            entity2Id.put(entity, "p" + predicateCount);
        }
    }

    static void encode(String path,String outPath) {
        System.out.println("Start with encoding entities and predicates.");
        entity2Id = new HashMap<String, String>();
        entityCount = typeCount = predicateCount = 0;
        List<String> lines = Utils.readLines(path);
        //<Love_Is_Colder_Than_Death_(film)>	<hasImdb>	<0064588>
        for (String line : lines) {
            line = line.substring(1, line.length() - 1);//Love_Is_Colder_Than_Death_(film)>	<hasImdb>	<0064588
            String[] parts = line.split(">\t<");//Love_Is_Colder_Than_Death_(film) hasImdb 0064588

            // encode entity
            encode(parts[0], 0);//Love_Is_Colder_Than_Death_(film)
            encode(parts[2], 0);//0064588

            // encode predicate name
            encode(parts[1], 2);//hasImdb


            if (parts[1].equals("subClassOf")) {//rdfs:Class
                encode(parts[0], 1);
                encode(parts[2], 1);
                continue;
            }
            if (parts[1].equals("type")) {//rdf:type
                encode(parts[2], 1);
                continue;
            }

            //-----可以添加predicate domain,range
            if (parts[1].equals("domain")) {//rdfs:domain
                encode(parts[0], 2);
                encode(parts[2], 1);
                continue;
            }
            if (parts[1].equals("range")) {//rdfs:range
                encode(parts[0], 2);
                encode(parts[2], 1);
            }


        }

        try {
            Writer encodeFileWriter = new BufferedWriter(new FileWriter(outPath));//encode.txt
            for (String entity : entity2Id.keySet()) {
                String id = entity2Id.get(entity);
                encodeFileWriter.write(entity + "\t" + id + "\n");
            }
            encodeFileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Done with encoding entities and predicates.");
    }
}
