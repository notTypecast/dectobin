package binconv;
import java.lang.Math;

public class Converter {
	
	private static int getMinBits(int number) {
		int bits = 1;
		int lnx = (int) (Math.log(Math.abs(number)) / Math.log(2)); 
		
		while (bits <= lnx)
			++bits;
		
		return bits;
	}
	
	private static String getBinaryValue(int number) {
		
		String binary = "";
		String remainders = "";
		
		if (number == 0)
			return "0";
		
		while (number != 0) {
			remainders += Math.abs(number % 2);
			number /= 2;
			
		}
		
		for (int i = remainders.length() - 1; i >= 0; --i)
			binary += remainders.charAt(i);
		
		return binary;
		
	}
	
	private static String getBinaryValue(double number) {
		
		int int_part = (int) number;
		double fract_part = number - int_part;
		
		String int_bin = getBinaryValue(int_part);
		String resulting_bits = "";
		
		if (fract_part == 0.0)
			resulting_bits = "0";
		
		while (fract_part != 0.0) {
			fract_part *= 2;
			resulting_bits += (int) fract_part;
			if (fract_part >= 1.0)
				fract_part -= 1;
		}
		
		return int_bin + '.' + resulting_bits;
		
	}
	
	private static boolean isPowerofTwo(int number) {
		String binary = getBinaryValue(number);
		
		int one_count = 0;
		
		for (int i = 0; i < binary.length(); ++i) {
			if (binary.charAt(i) == '1')
				++one_count;
		}
		
		return one_count == 1;
	}
	
	private static String invertBinary(String binary) {
		String inverted = "";
		
		for (int i = 0; i < binary.length(); ++i) {
			if (binary.charAt(i) == '1')
				inverted += '0';
			else if (binary.charAt(i) == '0')
				inverted += '1';
		}
		
		return inverted;
	}
	
	private static String extendBinaryValueSign(String bin_value) {
		int trailing_zeros = 4 - bin_value.length() % 4;
		String extended = "";
		
		for (int i = 0; i < trailing_zeros; ++i)
			extended += '0';
				
		return extended + bin_value;
	}
	
	private static String i3e754Normalize(String binary) {
		
		String normalized = "";
		
		if (binary.charAt(0) == '0') {
			
			int one_index = 0;
			
			for (int i = 2; one_index == 0; ++i) {
				if (binary.charAt(i) == '1')
					one_index = i;
			}
			
			normalized = binary.charAt(one_index) + "." + binary.substring(one_index + 1);
			
			if (normalized.charAt(normalized.length() - 1) == '.')
				normalized += '0';
			
		}
		
		else if (binary.charAt(0) == '1' && binary.charAt(1) == '.')
			return binary;
		
		else {
			int dec_point = binary.indexOf('.');
			normalized = "1." + binary.substring(1, dec_point) + binary.substring(dec_point + 1);
		}
		
		return normalized;
	}
	
	private static int i3e754getExponent(String initial) {
		int exponent = 0;
		
		if (initial.charAt(0) == '1')
			exponent = initial.indexOf('.') - 1;
		else
			exponent = 1 - initial.indexOf('1');
		
		return exponent;
	}
	
	public static String intToSignMagnitude(int number) {
		
		String binary = "";
		
		if (number >= 0)
			binary += '0';
		else
			binary += '1';
		
		binary += getBinaryValue(number);
		
		return binary;		
		
	}
	
	
	public static String intToExcessk(int number, int k_u) {
		
		int n = getMinBits(number);
		
		if (!isPowerofTwo(number))
			++n;
		
		int k = (int) Math.pow(2, n - 1);
		int c = 0;
		
		if (k_u > k) {
			k = k_u;
			while (k > Math.pow(2, c))
				++c;
		}
		else
			c = n - 1;
		
		++c;
		
		String binary = "";
		String bin_value = getBinaryValue(number + k);
		
		for (int i = 0; i < (c - bin_value.length()); ++i)
			binary += '0';
		
		return binary + bin_value;
		
	}
	
	public static String intToExcessk(int number) {
		return intToExcessk(number, 0);
	}
	
	public static String intTo1sC(int number) {
		String binary = getBinaryValue(number);
		
		binary = extendBinaryValueSign(binary);
		
		if (number >= 0)
			return binary;
		else
			return invertBinary(binary);
		
	}
	
	public static String intTo2sC(int number) {
		String binary = getBinaryValue(number);
		
		binary = extendBinaryValueSign(binary);
		
		if (number >= 0)
			return binary;
	
		int last_one = -1;
		
		for (int i = binary.length() - 1; i >= 0; --i) {
			if (binary.charAt(i) == '1') {
				last_one = i;
				break;
			}
		}
		
		String inverted_part = invertBinary(binary.substring(0, last_one));
		
		return inverted_part + binary.substring(last_one);
		
	}
	
	public static String doubleToSinglePrecision(double number) {
		
		String single_prec = "";
		
		if (number == 0.0) {	
			for (int i = 0; i < 32; ++i)
				single_prec += '0';
			
			return single_prec;
		}
		
		String value = getBinaryValue(number);
		String normalized = i3e754Normalize(value);
		int exponent = i3e754getExponent(value);
		
		single_prec = (number > 0 ? '0' : '1') + intToExcessk(exponent, 127) + normalized.substring(2);
		
		if (single_prec.length() > 32)
			single_prec = single_prec.substring(0, 32);
		
		else if (single_prec.length() < 32) {
			while (single_prec.length() != 32)
				single_prec += '0';
		}
		
		return single_prec;
	}
	
public static String doubleToDoublePrecision(double number) {
		
		String double_prec = "";
		
		if (number == 0.0) {	
			for (int i = 0; i < 64; ++i)
				double_prec += '0';
			
			return double_prec;
		}
		
		String value = getBinaryValue(number);
		String normalized = i3e754Normalize(value);
		int exponent = i3e754getExponent(value);
		
		double_prec = (number > 0 ? '0' : '1') + intToExcessk(exponent, 1023) + normalized.substring(2);
		
		if (double_prec.length() > 64)
			double_prec = double_prec.substring(0, 64);
		
		else if (double_prec.length() < 64) {
			while (double_prec.length() != 64)
				double_prec += '0';
		}
		
		return double_prec;
	}

}
