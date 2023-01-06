package other_algorithms.similarityMeasure;

import gilp.knowledgeClean.RuleLearnerHelper;

import java.util.HashMap;

public class CompareStrSimUtil {
    //Levenshtein distance
    private static int compare(String str, String target, boolean isIgnore) {
        int d[][]; // 矩阵
        int n = str.length();
        int m = target.length();
        int i; // 遍历str的
        int j; // 遍历target的
        char ch1; // str的
        char ch2; // target的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) { // 遍历str
            ch1 = str.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (isIgnore) {
                    if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }
                } else {
                    if (ch1 == ch2) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }
                }

                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }

    private static int min(int one, int two, int three) {
        return (one = one < two ? one : two) < three ? one : three;
    }

    public static float getSimilarityRatio(String str, String target, boolean isIgnore) {
        float ret = 0;
        if (Math.max(str.length(), target.length()) == 0) {
            ret = 1;
        } else {
            ret = 1 - (float) compare(str, target, isIgnore) / Math.max(str.length(), target.length());
        }
        return ret;
    }

    public static void main(String[] args) {
        CompareStrSimUtil lt = new CompareStrSimUtil();
        String str = "" +
                " people";
        String target = "Isle of Man";

        String target1 = "United Kingdom";
        String target2 = "Canada";
        String target3 = "Belarus";
        String target4 = "Ukraine";
        String target5 = "Kazakhstan";
        String target6 = "Uzbekistan";
        String target7 = "Canada";

//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target1, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target2, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target3, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target4, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target5, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target6, true));
//        System.out.println("similarityRatio=" + lt.getSimilarityRatio(str, target7, true));


        HashMap<String, Double> targets = new HashMap<String, Double>();

        targets.put(target, (double) lt.getSimilarityRatio(str, target, true));
        targets.put(target1, (double) lt.getSimilarityRatio(str, target1, true));
       targets.put(target2, (double) lt.getSimilarityRatio(str, target2, true));
//        targets.put(target3, (double) lt.getSimilarityRatio(str, target3, true));
//        targets.put(target4, (double) lt.getSimilarityRatio(str, target4, true));
//        targets.put(target5, (double) lt.getSimilarityRatio(str, target5, true));

        HashMap<String, Double> reverseResults = new RuleLearnerHelper().reverseOrderByValue(targets);
        for (String key : reverseResults.keySet())
            System.out.println(key + "\t" + reverseResults.get(key) + "\n");
        String firstKey = reverseResults.keySet().iterator().next();
        System.out.println(firstKey+"\t"+reverseResults.get(firstKey));
    }


}