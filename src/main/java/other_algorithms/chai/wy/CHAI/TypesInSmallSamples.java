package other_algorithms.chai.wy.CHAI;

import gilp.comments.Comment;
import gilp.rdf3x.RDF3XEngine;
import gilp.rdf3x.Triple;
import gilp.sparql.Sparql;
import gilp.utils.KVPair;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updating the CHAI model, select the small sample datasets, based on the yago
 * and dbpedia to get the types, Through the types, we can get the new criteria
 * based on the types.
 * <p>
 * entity->types->entities For example, the datasets: FB13--train.txt
 * <p>
 * fixed One dataset:FB13
 *
 * @author wy
 */
public class TypesInSmallSamples {

//	public HashMap<String, Integer> MapRelationID = new HashMap<String, Integer>() ;

    public static HashMap<String, Integer> MapEntityID = null;// new HashMap<String, Integer>();

    public HashMap<String, HashSet<String>> MapEntityTypes = new HashMap<String, HashSet<String>>();

    // public HashMap<String, HashSet<String>> typeToEntity = null;

    public HashMap<String, HashSet<String>> type2Entity = new HashMap<String, HashSet<String>>();

    public HashSet<String> readLearnRelation() throws IOException {
        String path = "/home/wy/Desktop/datasets-CHAI/FB13/relations.txt";
        HashSet<String> relations = new HashSet<String>();

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
        String line = "";
        while ((line = read.readLine()) != null)
            relations.add(line.split("\t")[0].trim());

        read.close();
        System.out.println(relations);
        return relations;

    }

    public void getTypesFromDbepedia() throws Exception {
        HashMap<String, Integer> readEntitiesID = readEntitiesID();
        System.out.println(readEntitiesID.size());
        String path4 = "/home/wy/Desktop/datasets-CHAI/FB13/new-result/typesInDbpedia.txt";
        BufferedWriter output1 = new BufferedWriter(new OutputStreamWriter
                (new FileOutputStream(path4), StandardCharsets.UTF_8));
        int k = 1;
        for (String entity : readEntitiesID.keySet()) {
            output1.write(entity + "\t");
         //   int entityId = readEntitiesID.get(entity);

            HashSet<String> existedTypes = new HashSet<String>();

            String bigEntity = capitalWords(entity);
//------------------------------------------------------------------------------dbpedia
            HashSet<String> dbrTypes = dbrTypes(entity);
            if (!dbrTypes.isEmpty())
                existedTypes.addAll(dbrTypes);

            HashSet<String> dbrBigTypes = dbrTypes(bigEntity);
            if (!dbrBigTypes.isEmpty())
                existedTypes.addAll(dbrBigTypes);

            if (!dbrBigTypes.isEmpty()) {
                for (String type : existedTypes)
                    output1.write(type + "\t");
            }
            output1.write("\n");
            System.out.println(k++ +"\n");
        }

        output1.close();
    }

    public void getEntitiesTypeBasedOnDbpeida() throws Exception {
        // the entity choose the dbr, the entity-type choose the dbo
        //   String path = "/home/wy/Desktop/datasets-CHAI/FB13/entities.txt";
        // String path1 =
        // "/home/wy/Desktop/datasets-CHAI/FB13/entities--types-dbpedia.txt";
        // String path2 = "/home/wy/Desktop/datasets-CHAI/FB13/entitiesID.txt";
        HashMap<String, Integer> readEntitiesID = readEntitiesID();

        HashMap<Integer, HashSet<Integer>> singleEntity2TypesId = new HashMap<Integer, HashSet<Integer>>();//<entity-id,types-id>

        LinkedHashMap<String, Integer> allTypesId = new LinkedHashMap<String, Integer>();

        SetValuedMap<HashSet<Integer>, Integer> idFromTypes2Entities = new HashSetValuedHashMap<>();//<types-id,entites-id>
        LinkedHashMap<ByteBuffer, Integer> types2Id = new LinkedHashMap<>();
        int i = 1;

//        String path6="/home/wy/Desktop/datasets-CHAI/FB13/types-id-in-entity.txt";
//        BufferedWriter output6 = new BufferedWriter(new OutputStreamWriter
//                (new FileOutputStream(path6), StandardCharsets.UTF_8));


        for (String entity : readEntitiesID.keySet()) {

            int entityId = readEntitiesID.get(entity);

            HashSet<String> existedTypes = new HashSet<String>();

            String bigEntity = capitalWords(entity);
//------------------------------------------------------------------------------dbpedia
            HashSet<String> dbrTypes = dbrTypes(entity);
            if (dbrTypes != null)
                existedTypes.addAll(dbrTypes);

            HashSet<String> dbrBigTypes = dbrTypes(bigEntity);
            if (dbrBigTypes != null)
                existedTypes.addAll(dbrBigTypes);


//            HashSet<String> dboTypes = dboTypes(entity);
//            if (dboTypes != null)
//                existedTypes.addAll(dboTypes);
//
//            HashSet<String> dboBigTypes = dboTypes(bigEntity);
//            if (dboBigTypes != null)
//                existedTypes.addAll(dboBigTypes);
            HashSet<Integer> oneTypesId = new HashSet<Integer>();
            for (String key : existedTypes) {
                if (!allTypesId.containsKey(key)) {
                    allTypesId.put(key, i++);
                }
                oneTypesId.add(allTypesId.get(key));
            }


            // String stringTypes = hash2String(existedTypes);
            //   byte[] entityByte = entity.getBytes(StandardCharsets.UTF_8);
            //  ByteBuffer buffer = ByteBuffer.wrap(stringTypes.getBytes("UTF-8"));
//            if (!types2Id.containsKey(buffer)) {
//                types2Id.put(buffer, i++);
//             //   output6.write();
//            }
//            System.out.println(i + "\n");

            //    int typesId = types2Id.get(buffer);
            singleEntity2TypesId.put(entityId, oneTypesId);
            idFromTypes2Entities.put(oneTypesId, entityId);

//            if(i>3)
//                break;

        }


//        for (ByteBuffer type : types2Id.keySet()) {
//            Object[] entities=  idFromTypes2Entities.get(id).toArray();
//            for (Object entity : entities) {
//                output1.write(getKeyFromValue(readEntitiesID,entity) + "\t");
//            }
//            ByteBuffer types= (ByteBuffer) getKeyFromValue(types2Id, id);
//            String converted = new String(types.array(), "UTF-8");
//            output1.write(":"+  converted+"\t");
//            output1.write("\n");
//        }
//        output1.close();

//        String path4="/home/wy/Desktop/datasets-CHAI/FB13/classifyEntitiesBasedTypes--dbpedia1.txt";
//        BufferedWriter output1 = new BufferedWriter(new OutputStreamWriter
//                (new FileOutputStream(path4), StandardCharsets.UTF_8));
//        for (HashSet<Integer> id : idFromTypes2Entities.keySet()) {
//          Object[] entities=  idFromTypes2Entities.get(id).toArray();
//            for (Object entity : entities) {
//                output1.write(getKeyFromValue(readEntitiesID,entity) + "\t");
//            }
//            ByteBuffer types= (ByteBuffer) getKeyFromValue(types2Id, id);
//            String converted = new String(types.array(), "UTF-8");
//            output1.write(":"+  converted+"\t");
//            output1.write("\n");
//        }
//         output1.close();

//------------------------------
        String path3 = "/home/wy/Desktop/datasets-CHAI/FB13/types-id-dbpedia1.txt";
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter
                (new FileOutputStream(path3), StandardCharsets.UTF_8));
        for (String type : allTypesId.keySet()) {
            output.write(type + "\t" + allTypesId.get(type) + "\n");
        }
        output.close();

        String path5 = "/home/wy/Desktop/datasets-CHAI/FB13/entity-entities-byTypes-dbpedia.txt";
        BufferedWriter output5 = new BufferedWriter(new OutputStreamWriter
                (new FileOutputStream(path5), StandardCharsets.UTF_8));
        for (Integer id : singleEntity2TypesId.keySet()) {
            output5.write(id + ":\t");
            // output5.write(getKeyFromValue(readEntitiesID, id) + ":\t");

            HashSet<Integer> typesId = singleEntity2TypesId.get(id);

            HashSet<Integer> ids = new HashSet<>();

            Object[] entities = idFromTypes2Entities.get(typesId).toArray();


            for (Object entity : entities) {
                ids.add((Integer) entity);

            }
            if (ids.size() > 1) {//1:1; delete the entity doesn't have candidates
                for (int key : ids)
                    output5.write(key + "\t");
            }

            output5.write("\n");
        }
        output5.close();


    }

    boolean contains(List<byte[]> arrays, byte[] other) {
        for (byte[] b : arrays)
            if (Arrays.equals(b, other)) return true;
        return false;
    }


    private static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    private String hash2String(HashSet<String> types) {
        StringBuilder str = new StringBuilder();
        for (String key : types)
            str.append(key).append(",");

        return str.toString();
    }

    public void getEntitiesID() throws Exception {
        String path = "/home/wy/Desktop/datasets-CHAI/WN18-AR/entities.txt";

        String path3 = "/home/wy/Desktop/datasets-CHAI/WN18-AR/entitiesID.txt";
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path3), "UTF-8"));
        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line = "";
        int ict = 1;
        while ((line = read.readLine()) != null) {
            String entity = line.split("\t")[0].trim();
            output.write(entity + "\t" + ict + "\n");
            ict++;
        }
        read.close();
        output.close();
    }

    public static HashMap<String, Integer> readEntitiesID() throws Exception {
        HashMap<String, Integer> readEntitiesID = new HashMap<>();
        String path3 = "/home/wy/Desktop/datasets-CHAI/FB13/entitiesID.txt";

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path3), "UTF-8"));
        String line = "";
        while ((line = read.readLine()) != null) {
            String entity = line.split("\t")[0].trim();
            int id = Integer.parseInt(line.split("\t")[1].trim());
            readEntitiesID.put(entity, id);
        }
        read.close();
        return readEntitiesID;

    }

    public static String removePointBrackets(String str) {
        str = str.replace("<", "");
        str = str.replace(">", "");
        return str;
    }

    public static HashSet<Comment> getTestFiles() throws Exception {
//umberto_i_of_italy	cause_of_death	tyrannicide	1
//		umberto_i_of_italy	cause_of_death	cerebral_aneurysm	-1
        String pathToFBfile = "/home/wy/Desktop/datasets-CHAI/FB13/test.txt";
        HashSet<Comment> comments = new HashSet<Comment>();

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(pathToFBfile), "UTF-8"));
        String line = "";

        while ((line = read.readLine()) != null) {
            String[] lists = line.split("\t");

            String subject, predicate, object;
            subject = lists[0].trim();
            predicate = lists[1].trim();
            object = lists[2].trim();

            String value = lists[3].trim();

            Triple triple = new Triple(subject, predicate, object);

            Comment comment = new Comment(triple, (value.equals("1")));
            comments.add(comment);

        }
        read.close();

        // System.out.println("READING all comments: " + comments.size());78490
        return comments;
    }

    public static HashSet<Triple> readTrainTriples() throws Exception {
        String path3 = "/home/wy/Desktop/datasets-CHAI/FB13/train.txt";
        HashSet<Triple> triples = new HashSet<Triple>();

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path3), "UTF-8"));
        String line = "";

        while ((line = read.readLine()) != null) {
            String[] lists = line.split("\t");

            String subject, predicate, object;
            subject = lists[0].trim();
            predicate = lists[1].trim();
            object = lists[2].trim();

            Triple triple = new Triple(subject, predicate, object);

            triples.add(triple);
            System.out.println(triple);
        }
        read.close();
        System.out.println("READING all comments: " + triples.size());// 285208
        return triples;

    }

    public KVPair<HashSet<String>, HashSet<String>> subjects(HashSet<Triple> train, HashSet<Comment> test, String relation) {

        HashSet<String> allsbujects = new HashSet<String>();

        HashSet<String> selectEntityEvaluate = new HashSet<String>();

        HashSet<String> trainsAllbujects = new HashSet<String>();

        HashSet<String> relationDomain = new HashSet<String>();

        HashSet<String> relationRange = new HashSet<String>();

        HashSet<String> testPosbujects = new HashSet<String>();// all positive

        HashSet<String> testNegbujects = new HashSet<String>();// all negative

        HashSet<Triple> positiveTriples = new HashSet<Triple>();

        HashSet<Triple> negativeTriples = new HashSet<Triple>();

        for (Triple t : train) {

            trainsAllbujects.add(t.get_subject());
            if (t.get_predicate().equals(relation)) {
                relationDomain.add(t.get_subject());
                relationRange.add(t.get_obj());
            }
        }
        for (Comment c : test) {

            Triple t = c.get_triple();
            String sub = t.get_subject();

            if (c.get_decision() && t.get_predicate().equals(relation)) {
                testPosbujects.add(sub);
                positiveTriples.add(t);
            } else if (!c.get_decision() && t.get_predicate().equals(relation)) {
                testNegbujects.add(sub);
                negativeTriples.add(t);
            }
        }

        allsbujects.addAll(relationDomain);
        allsbujects.addAll(testPosbujects);
        // allsubjects are the training triples.

        selectEntityEvaluate.addAll(trainsAllbujects);
        selectEntityEvaluate.removeAll(testPosbujects);


        HashMap<String, String> groundTruthForRelation = new HashMap<String, String>();
        HashSet<Triple> groundTruthForRelationTriples = new HashSet<Triple>();
        for (Triple k : positiveTriples) {
            String sub = k.get_subject();

            if (selectEntityEvaluate.contains(sub)) {
                groundTruthForRelation.put(sub, k.get_obj());
                groundTruthForRelationTriples.add(k);
            }
        }

        //range ,domain, distances
        //types


        return new KVPair<HashSet<String>, HashSet<String>>(allsbujects, selectEntityEvaluate);
    }


    public void getEntitiesType() throws Exception {
        String path = "/home/wy/Desktop/datasets-CHAI/FB13/entities.txt";
        // String path1 =
        // "/home/wy/Desktop/datasets-CHAI/FB13/entities--types-dbpedia.txt";
        // String path2 = "/home/wy/Desktop/datasets-CHAI/FB13/entitiesID.txt";
        String path3 = "/home/wy/Desktop/datasets-CHAI/FB13/entities--types-entitiesID--yago1.txt";

        // HashMap<String, Set<String>> ST = new HashMap<String, Set<String>>();

        BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line = "";
        int ict = 0;
        while ((line = read.readLine()) != null) {
            // int count = 0;
            HashSet<String> existedTypes = new HashSet<String>();

            String entity = line.trim();
            // System.out.println("\n" + entity);
            String bigEntity = capitalWords(entity);

            MapEntityID.put(entity, ict);
            ict++;
//-------------------------------------------------------------------------------yago

            HashSet<String> yagoSmallType = yagoType(entity);
            if (yagoSmallType != null)
                existedTypes.addAll(yagoSmallType);

            HashSet<String> yagoBigType = yagoType(bigEntity);
            if (yagoBigType != null)
                existedTypes.addAll(yagoBigType);
//------------------------------------------------------------------------------dbpedia
//			HashSet<String> dbrTypes = dbrTypes(entity);
//			if (dbrTypes != null)
//				existedTypes.addAll(dbrTypes);
//
//			HashSet<String> dbrBigTypes = dbrTypes(bigEntity);
//			if (dbrBigTypes != null)
//				existedTypes.addAll(dbrBigTypes);
//
//			HashSet<String> dboTypes = dboTypes(entity);
//			if (dboTypes != null)
//				existedTypes.addAll(dboTypes);
//
//			HashSet<String> dboBigTypes = dboTypes(bigEntity);
//			if (dboBigTypes != null)
//				existedTypes.addAll(dboBigTypes);

            if (existedTypes != null) {

                for (String t : existedTypes) {

                    if (type2Entity.get(t) == null) {
                        HashSet<String> set = new HashSet<String>();
                        set.add(entity);
                        type2Entity.put(t, set);

                    } else {
                        HashSet<String> values = type2Entity.get(t);

                        values.add(entity);
                        type2Entity.put(t, values);
                    }

                }

            }

            MapEntityTypes.put(entity, existedTypes);
        }
        read.close();

        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path3), "UTF-8"));
        // HashMap<String, Set<String>>
        for (String entity : MapEntityTypes.keySet()) {

            HashSet<Integer> ids = new HashSet<Integer>();

            HashSet<String> types = MapEntityTypes.get(entity);

            for (String type : types) {

                HashSet<String> entitiesFromTypes = type2Entity.get(type);

                for (String enti : entitiesFromTypes) {

                    int id = MapEntityID.get(enti);

                    ids.add(id);
                }
            }

            output.write(entity + ";");
            for (int k : ids) {
                output.write(k);
                for (int j = 0; j < ids.size() - 1; j++)
                    output.write(",");
            }

            output.write("\n");
        }

        output.close();
    }


    public HashSet<String> yagoType(String name) {
        String sparql = "select * where{ <" + name + "> <rdf:type> ?type.}";
        return new RDF3XEngine().getDistinctEntity(sparql);
    }

    public HashSet<String> dboTypes(String name) {
        //ontology is roughly th same as the T-Box.
        String sqlIndbpedia = "select * where{ dbo:" + name + " a ?type." +
                "filter(regex(?type,\"http://dbpedia.org/ontology/\"))}";

        return new Sparql().getSingleVariable(sqlIndbpedia);


    }

    public HashSet<String> dbrTypes(String name) {
        // resouce: the facts about the instances called the A-Box.
        String sql = "select * where {dbr:" + name + " a ?type. " +
                "filter(regex(?type,\"http://dbpedia.org/ontology/\"))}";
        // System.out.println(sql);
        HashSet<String> types = new Sparql().getSingleVariable(sql);
        HashSet<String> newTypes = new HashSet<>();
        for (String t : types) {
            newTypes.add(t.replace("http://dbpedia.org/ontology/", ""));
        }
        return newTypes;
    }

    public HashSet<String> dbrRDFDbpediaTypes(String name) {
        String sql = "select * where {<http://dbpedia.org/resource/" + name + "> a ?type. }";//change the rdf3x database to dbpedia
        // System.out.println(sql);
        //   HashSet<String> types = new Sparql().getSingleVaraible(sql);
        HashSet<String> types = new RDF3XEngine().getDistinctEntity(sql);
        HashSet<String> newTypes = new HashSet<>();
        for (String t : types) {
            newTypes.add(t.replace("http://dbpedia.org/ontology/", ""));
        }
        return newTypes;
    }

    public static boolean stringContainsNumber(String s) {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(s);

        return m.find();
    }

    // Capitalize the first letter of all words
    public static String captureName(String name) {
//		name = name.substring(0, 1).toUpperCase() + name.substring(1);
//		return name;
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    // here, meet one problem, the null is not recognized, we need to use the is empty.
    public static String capitalWords(String s) {
        String interval = "_";
        String[] split = s.split(interval);
        StringBuilder name = new StringBuilder(captureName(split[0]));
        for (int i = 1; i < split.length; i++) {
            if (!split[i].isEmpty() && !stringContainsNumber(split[i]))
                name.append(interval).append(captureName(split[i]));
            else
                name.append(interval).append(split[i]);
        }
        return name.toString();
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        TypesInSmallSamples infer = new TypesInSmallSamples();

        // infer.getEntitiesType();
        //readTrainTriples();
        // infer.getEntitiesTypeBasedOnDbpeida();
//System.out.println(infer.dbrTypes("United_States"));

        infer.getTypesFromDbepedia();
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");

    }

}
