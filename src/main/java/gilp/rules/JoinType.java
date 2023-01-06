package gilp.rules;

public enum JoinType {
	SS, SO, OS, OO;

	public static String removePointBrackets(String str){
		str = str.replace("<", "");
		str = str.replace(">", "");
		return str;
	}

	private static boolean test(){
		String value = "0.21";
		String range = "(1,1.2]";

		double dv = Double.parseDouble(value);
		String left = range.trim().substring(0, 1);
		String right = range.trim().substring(range.length()-1, range.length());
		String low = range.substring(range.indexOf(left)+1, range.indexOf(","));
		String high = range.substring(range.indexOf(",")+1, range.indexOf(right));
		double d_l = Double.parseDouble(low);
		double d_h = Double.parseDouble(high);
		System.out.println(d_l);
		System.out.println(d_h);

		if (d_l>d_h)
			return false;
		if(left.equals("(") && dv<= d_l)
			return false;
		if(left.equals("[") && dv< d_l)
			return false;
		if(right.equals(")") && dv>=d_h)
			return false;
		if(right.equals("]") && dv>d_h)
			return false;
		return true;
	}

	public static void main(String[] args){
		System.out.println(test());
	}
}
