package codingTest;

import java.util.Scanner;

public class Ex10926 {
    public static void main(String[] args) {

		// 준하는 사이트에 회원가입을 하다가 joonas라는 아이디가 이미 존재하는 것을 보고 놀랐다. 준하는 놀람을 ??!로 표현한다. 준하가 가입하려고 하는 사이트에 이미 존재하는 아이디가 주어졌을 때, 놀람을 표현하는 프로그램을 작성하시오. 
		// 입력: 첫째 줄에 준하가 가입하려고 하는 사이트에 이미 존재하는 아이디가 주어진다. 아이디는 알파벳 소문자로만 이루어져 있으며, 길이는 50자를 넘지 않는다.
		// 출력: 첫째 줄에 준하의 놀람을 출력한다. 놀람은 아이디 뒤에 ??!를 붙여서 나타낸다.
		Scanner scan = new Scanner(System.in);
		String id = scan.next();
		
		System.out.println(id + "??!");
		
		scan.close();
	}
}

/*  세번 틀렸음, 문제파악이 제대로 이뤄지지 않음,
 *  비교가 아닌 단지 입력값인 아이디 뒤에 놀람 "??!"을 붙여 나타내는 것
 *  하지만 문제는 아무리 읽어봐도 원래 있던 값과 새로 입력된 값을 비교하여 같을 경우 놀람을 표시하라는 뜻 같음..
	String id, newId;
		
	id = "joonas";
		
	Scanner scan = new Scanner(System.in);
	newId = scan.next();

	if (id.equals(newId)) {
		System.out.println("joonas??!");
	}
	
	scan.close(); 
*/