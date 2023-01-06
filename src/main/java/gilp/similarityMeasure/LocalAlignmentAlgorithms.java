package gilp.similarityMeasure;



import java.util.ArrayList;
import java.util.Scanner;

public class LocalAlignmentAlgorithms {

    public static void main(String[] args) {
        // input will be used for taking in user input
     //   Scanner input = new Scanner(System.in);

        // Get sequences from user
        // System.out.println("Enter sequence 1: ");
        // String x = input.nextLine();
        // System.out.println("Enter sequence 2: ");
//		String y = input.nextLine();

        String x ="Aberdeen_F.C.";// "AABBA";
        String y = "Aberdeen";//"BBAAA";
        // Get score values from user
        // System.out.println("Enter score for match: ");
        int match = 1; // input.nextInt();
        // System.out.println("Enter score for mismatch: ");
        int mismatch = 0; // input.nextInt();
        // System.out.println("Enter score for spaces: ");
        int space = -1; // input.nextInt();

        long time = System.currentTimeMillis();
   //     for (int i = 0; i < 100; i++) {
            System.out.println(SmithWaterman(x, y, match, mismatch, space));
            System.out.println(DivideAndConquer(x, y, match, mismatch, space));
            System.out.println(greedy(x, y, match, mismatch, space));
            System.out.println(BruteForce(x, y, match, mismatch, space));
      //      break;
     //   }
        System.out.println((System.currentTimeMillis() - time));
        System.exit(0);

        // Run each algorithm
        System.out.println(SmithWaterman(x, y, match, mismatch, space));
        System.out.println(DivideAndConquer(x, y, match, mismatch, space));
        System.out.println(greedy(x, y, match, mismatch, space));
        System.out.println(BruteForce(x, y, match, mismatch, space));
    }

    // Written by Ali
    public static String SmithWaterman(String x, String y, int match, int mismatch, int space) {

        int[][] scoreChart = new int[x.length() + 1][y.length() + 1];
        int highestScore = 0;
        int Xco = 0;
        int Yco = 0;

        for (int i = 1; i <= x.length(); i++) {
            for (int j = 1; j <= y.length(); j++) {

                int score1 = scoreChart[i][j - 1] + space;
                int score2 = scoreChart[i - 1][j] + space;
                int score3 = 0;
                if (x.substring(i - 1, i).equalsIgnoreCase(y.substring(j - 1, j))) {
                    score3 = scoreChart[i - 1][j - 1] + match;
                } else {
                    score3 = scoreChart[i - 1][j - 1] + mismatch;
                }

                if (score1 > score2 && score1 > score3 && score1 > 0) {
                    scoreChart[i][j] = score1;
                } else if (score2 > score3 && score2 > 0) {
                    scoreChart[i][j] = score2;
                } else if (score3 > 0) {
                    scoreChart[i][j] = score3;
                }

                if (scoreChart[i][j] > highestScore) {
                    highestScore = scoreChart[i][j];
                    Xco = i;
                    Yco = j;
                }

            }
        }
        boolean found = false;
        int i = Xco;
        int j = Yco;

        while (!found) {
            if (scoreChart[i - 1][j - 1] > 0) {
                i--;
                j--;
            } else if (scoreChart[i - 1][j] > scoreChart[i][j - 1] && scoreChart[i - 1][j] > scoreChart[i][j]) {
                i--;
            } else if (scoreChart[i][j - 1] > scoreChart[i][j]) {
                j--;
            } else {
                found = true;
            }
        }

//		System.out.println("Smith-Watermann:");
//		System.out.println("Highest Score was: " + highestScore);
//		System.out.println("The highest score was found at: (" + (Xco - 1) + "," + (Yco - 1) + ")");
//		System.out.println("Subsequence in first sequence: " + x.substring(i - 1, Xco));
//		System.out.println("Subsequence in second sequence: " + y.substring(j - 1, Yco));
        String output = "";
        output = "Smith-Watermann:" + System.lineSeparator() + "Highest Score was: " + highestScore
                + System.lineSeparator() + "The highest score was found at: (" + (Xco - 1) + "," + (Yco - 1) + ")"
                + System.lineSeparator() + "Subsequence in first sequence: " + x.substring(i - 1, Xco)
                + System.lineSeparator() + "Subsequence in second sequence: " + y.substring(j - 1, Yco);
        return output;

        /*
         * for(int k = 0; k < x.length() + 1; k++) { for(int l = 0; l < y.length() + 1;
         * l++) { System.out.print(" " + scoreChart[k][l]); } System.out.println(""); }
         */
    }

    // Written by Ali
    public static String DivideAndConquer(String x, String y, int match, int mismatch, int space) {
        int k = 2;

        String[] divisionsX = new String[x.length()];
        int[] indicesX = new int[x.length()];
        for (int i = 0; i < x.length(); i++) {
            if (i + k < x.length()) {
                divisionsX[i] = x.substring(i, i + k);
                indicesX[i] = i;
            }
        }

        String[] divisionsY = new String[y.length()];
        int[] indicesY = new int[y.length()];
        for (int i = 0; i < y.length(); i++) {
            if (i + k < y.length()) {
                divisionsY[i] = y.substring(i, i + k);
                indicesY[i] = i;
            }
        }

        int[] seedX = new int[divisionsX.length * divisionsY.length];
        int[] seedY = new int[divisionsX.length * divisionsY.length];
        int[] score = new int[divisionsX.length * divisionsY.length];
        int seedCount = 0;

        for (int i = 0; i < divisionsX.length; i++) {
            for (int j = 0; j < divisionsY.length; j++) {
                try {
                    if (divisionsX[i].equals(divisionsY[j])) {
                        seedX[seedCount] = indicesX[i];
                        seedY[seedCount] = indicesY[j];
                        score[seedCount] = match * k;
                        seedCount++;
                    }
                } catch (Exception e) {
                    // System.out.println(divisionsX[i]);
                    // System.out.println(divisionsY[j]);
                }
            }
        }

        if (seedCount == 0) {
            // System.out.println("No matches with size: " + k);
            return "Divide and Conquer: " + System.lineSeparator() + "No matches with size: " + k;
        } else {
            int highestscore = match * k;
            int highestScoreLocX = seedX[0];
            int highestScoreLocY = seedY[0];
            int size = k;
            for (int i = 0; i < seedCount; i++) {
                int leftside = 0;
                int rightside = 0;
                int curSize = k;
                // QA infinite loop possible FIXME
                while (leftside < k && rightside < k) {
                    if (seedX[i] - 1 >= 0 && seedY[i] - 1 >= 0) {
                        seedX[i] = seedX[i] - 1;
                        seedY[i] = seedY[i] - 1;
                        curSize += 1;
                        if (x.substring(seedX[i], seedX[i] + 1).equals(y.substring(seedY[i], seedY[i] + 1))) {
                            score[i] += match;
                            leftside = 0;
                        } else {
                            score[i] += mismatch;
                            leftside += 1;
                        }

                        if (score[i] > highestscore) {
                            highestscore = score[i];
                            highestScoreLocX = seedX[i];
                            highestScoreLocY = seedY[i];
                            size = curSize;
                        }
                    } else if (seedX[i] <= 0 || seedY[i] <= 0) {
                        leftside += k;
                    }

                    if (seedX[i] + (curSize + 1) < x.length() && seedY[i] + (curSize + 1) < y.length()) {
                        curSize += 1;
                        if (x.substring(seedX[i] + curSize, seedX[i] + (curSize + 1))
                                .equals(y.substring(seedY[i] + curSize, seedY[i] + (curSize + 1)))) {
                            score[i] += match;
                            rightside = 0;
                        } else {
                            score[i] += mismatch;
                            rightside += 1;
                        }

                        if (score[i] > highestscore) {
                            highestscore = score[i];
                            highestScoreLocX = seedX[i];
                            highestScoreLocY = seedY[i];
                            size = curSize;
                        }
                    } else if (seedX[i] + (curSize + 1) >= x.length() - 1
                            || seedY[i] + (curSize + 1) >= y.length() - 1) {
                        rightside += k;
                    }
                }
                boolean clean = false;
                while (!clean) {
                    if (curSize == 0) {
                        clean = true;
                    } else if (!(x.substring(seedX[i], seedX[i] + 1).equals(y.substring(seedY[i], seedY[i] + 1)))) {
                        seedX[i] = seedX[i] + 1;
                        seedY[i] = seedY[i] + 1;
                        curSize -= 1;
                    } else {
                        clean = true;
                    }
                }
            }
//			System.out.println("Divide and Conquer:");
//			System.out.println("Highest Score was: " + highestscore);
//			System.out.println("The highest score was found at: (" + highestScoreLocX + "," + highestScoreLocY + ")");
//			System.out.println(
//					"Subsequence in first sequence: " + x.substring(highestScoreLocX, highestScoreLocX + size));
//			System.out.println(
//					"Subsequence in second sequence: " + y.substring(highestScoreLocY, highestScoreLocY + size));

            String outpString = "";
            outpString = "Divide and Conquer: " + System.lineSeparator() + "Highest Score was: " + highestscore
                    + System.lineSeparator() + "The highest score was found at: (" + highestScoreLocX + ","
                    + highestScoreLocY + ")";
            outpString = outpString + System.lineSeparator() + "Subsequence in first sequence: "
                    + x.substring(highestScoreLocX, highestScoreLocX + size);
            outpString = outpString + System.lineSeparator() + "Subsequence in second sequence: "
                    + y.substring(highestScoreLocY, highestScoreLocY + size);
            return outpString;

        }

    }

    // Written by Torgeir
    public static String greedy(String a, String b, int match, int mismatch, int space) {
        String outputString;
        int starta = 0;
        int startb = 0;
        int score = 0;
        boolean startFound = false;
        outer: for (int i = 0; i < a.length() - 1; i++) {
            for (int j = 0; j < b.length() - 1; j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    starta = i;
                    startb = j;
                    System.out.println(i + " " + j);
                    startFound = true;
                    break outer;
                }
            }
        }
        if (!startFound) {
            return "Greedy " + System.lineSeparator() + "No matching characters found";
        }
        int count = 0;
        while (a.length() >= starta + count && b.length() >= startb + count) {
            if (a.charAt(starta + count) == b.charAt(startb + count)) {
                count++;
            } else {
                break;
            }

        }
        score = count * match;
        String returnString = "Greedy" + System.lineSeparator();
        returnString = returnString + "Score : " + score + System.lineSeparator();
        returnString = returnString + "Substring found : " + a.substring(starta, (starta + count));
        return returnString;
    }

    // BruteForce finds local alignment using a brute force algorithm
    // Inputs:
    // x is sequence 1
    // y is sequence 2
    // The other 3 inputs are values for scoring
    // Written by Ali Khan
    public static String BruteForce(String x, String y, int match, int mismatch, int space) {
        // Initialize array lists to hold all subsequences of x and y
        ArrayList<String> xIterations = new ArrayList<String>();
        ArrayList<String> yIterations = new ArrayList<String>();

        // Add all subsequences of x to array list
        for (int i = 0; i < x.length(); i++) {
            for (int j = i + 1; j < x.length(); j++) {
                xIterations.add(x.substring(i, j));
            }
        }

        // Add all subsequences of y to array list
        for (int i = 0; i < y.length(); i++) {
            for (int j = i + 1; j < y.length(); j++) {
                yIterations.add(y.substring(i, j));
            }
        }

        // Keep track of highest score and its sequences
        int highestscore = Integer.MIN_VALUE;
        String highestScoreX = "";
        String highestScoreY = "";

        // Loop through all subsequences comparing them to one another
        for (int i = 0; i < xIterations.size(); i++) {
            for (int j = 0; j < yIterations.size(); j++) {
                // Initialize variables to keep rack of current score
                int curScore = 0;
                String a = xIterations.get(i);
                String b = yIterations.get(j);

                // Check for gaps and update score
                if (a.length() != b.length()) {
                    curScore = Math.abs(a.length() - b.length()) * space;
                }

                // Go through each letter in sequences to check for matches and mismatches.
                // Update score accordingly
                if (a.length() < b.length()) {
                    for (int k = 0; k < a.length() - 1; k++) {
                        if (a.substring(k, k + 1).equalsIgnoreCase(b.substring(k, k + 1))) {
                            curScore += match;
                        } else {
                            curScore += mismatch;
                        }
                    }
                } else {
                    for (int k = 0; k < b.length() - 1; k++) {
                        if (a.substring(k, k + 1).equalsIgnoreCase(b.substring(k, k + 1))) {
                            curScore += match;
                        } else {
                            curScore += mismatch;
                        }
                    }
                }

                // Update highest score
                if (curScore > highestscore) {
                    highestscore = curScore;
                    highestScoreX = a;
                    highestScoreY = b;
                }

            }
        }

//		System.out.println("Brute Force:");
//		System.out.println("Highest Score was: " + highestscore);
//		System.out.println("Subsequence in first sequence: " + highestScoreX);
//		System.out.println("Subsequence in second sequence: " + highestScoreY);

        String retrurnString = "Brute Force:" + System.lineSeparator();
        retrurnString = retrurnString + "Highest Score was: " + highestscore + System.lineSeparator();
        retrurnString = retrurnString + "Subsequence in first sequence: " + highestScoreX + System.lineSeparator();
        retrurnString = retrurnString + "Subsequence in second sequence: " + highestScoreY;
        return retrurnString;
    }

}
