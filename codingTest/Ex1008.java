package codingTest;

import java.util.Scanner;

public class Ex1008 {
    public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		
		double A,B;
		
		A = scan.nextInt();
		B = scan.nextInt();

		System.out.println(A/B);

		scan.close();
	}
}