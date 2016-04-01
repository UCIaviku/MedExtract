package test;

public class Tests {

	public static void main(String[] args) {
		RegexTests.test_DateTimeRegex_FilterOn();
		RegexTests.test_DateTimeRegex_FilterOff();
		
		DictTests.test_SmallDict_FilterOn();
		DictTests.test_SmallDict_FilterOff();
		DictTests.test_LargeDict_FilterOn();
		DictTests.test_LargeDict_FilterOff();
		
	}
}
