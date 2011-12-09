package jamsex.framework;

public class LongEncoder {

	public static void main(String[] args) {
		printStat(0l);
		long val = 1;
		while (val > 0) {
			printStat(val);
			val = val * 2;			
		}
		printStat(Long.MAX_VALUE);		
		printStat(60466176);
		printStat(60466176 -1);
		System.out.println("http://jamsession.me/60466175".length() + " " + "http://jms.ms/zzzzz".length());
		System.out.println("http://jamsessions.me/1".length());
		System.out.println("http://jms.ms/1".length());
		System.out.println("http://jamsessions.me/9223372036854775807".length());
		System.out.println("http://jms.ms/1y2p0ij32e8e7".length());
		
		findLength();
	}
	
	private static void findLength() {
		long val = 1;
		int last = -1;
		while (val != Long.MAX_VALUE) {
			String base36Val = Long.toString(val, 36);
			int len = base36Val.length(); 
			if (len != last) {
				System.out.println(val + " --> " + base36Val + "(" + len + ")");
			}
			last = base36Val.length();
			val++;
		}
	}

	private static void printStat(long val) {
		String strVal = Long.toString(val);
		String base36Val = Long.toString(val, 36);
		System.out.print(val + " (" + strVal.length() + ")");
		System.out.println("--> " + base36Val + " (" + base36Val.length() + ")");
	}
}
