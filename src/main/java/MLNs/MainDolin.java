package MLNs;

import MLNs.eval.CrossValidation;
import MLNs.eval.MeanRankCalc;

import java.util.Arrays;

/**
 * Main controller for Mandolin.
 * 
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class MainDolin {

	public static void main(String[] args) throws  Exception {
		
		String[] argsw = Arrays.copyOfRange(args, 1, args.length);
		
		switch(args[0]) {
		case "plain":
			Mandolin.main(argsw);
			break;
		case "eval":
			MeanRankCalc.main(argsw);
			break;
		case "cv":
			CrossValidation.main(argsw);
			break;
		default:
		}
		
	}

}
