package codingTest;

import java.util.Scanner;

public class Ex1001 {
    public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		
		int A,B;
		
		A = scan.nextInt();
		B = scan.nextInt();

		System.out.println(A-B);

		scan.close();
	}
}