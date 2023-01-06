package prediction_type.ConfE;

import java.io.*;
import java.util.*;

import static prediction_type.ConfE.GlobalValue.*;
import static prediction_type.ConfE.Utils.rand_max;
import static prediction_type.Preprocess.*;
public class BuildNoise {
	
	private static void GlobalValueInit() {
        entity2id = new HashMap<>();
        type2id = new HashMap<>();
    }
    public static void build_run() throws IOException {
        GlobalValueInit();
       
        entity_num = Read_Data("ConfE-master\\resource\\data\\FB15k\\entity2id.txt", entity2id,id2entity);
		type_num = Read_Data("ConfE-master\\resource\\data\\FB15k\\type2id.txt", type2id,id2type);

		File f = new File("ConfE-master\\resource\\data\\FB15k_Entity_Types\\FB15k_Entity_Type_valid.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        Map<Integer, Set<Integer>> et2type = new HashMap<>();
        List<String> fb_ht = new ArrayList<>();
        List<String> fb_lt = new ArrayList<>();
        String line;
        int k = 0;

        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String heade = split_data[0];
            String tailet = split_data[1];
        	if (!et2type.containsKey(entity2id.get(heade))) {
        		et2type.put(entity2id.get(heade), new HashSet<>());
        	}
        	Set<Integer> type_set = et2type.get(entity2id.get(heade));
        	type_set.add(type2id.get(tailet));
    		fb_ht.add(heade);
    		fb_lt.add(tailet);
        }
        int tuple_num = fb_ht.size();
        for(int i = 0; i < tuple_num; i ++) {
        	int head_id = entity2id.get(fb_ht.get(i));
        	int jj = rand_max(tuple_num);
        	Set<Integer> values = et2type.get(head_id);
			while (values.contains(fb_lt.get(jj))) {
				jj = rand_max(tuple_num);
			}
    		fb_ht.add(fb_ht.get(i));
    		fb_lt.add(fb_lt.get(jj));
        }
		Write_Vec2File("ConfE-master\\resource\\data\\FB15k\\FB15k_Entity_Type_valid_noise.txt", fb_ht,
				fb_lt);        
    }

}
