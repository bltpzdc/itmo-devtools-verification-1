public class Test {
    public static void main(String[] args) {
        int a = 10;
        int b = 15;
        if ( a + b < 30 ) {
            a = 15;
            b = 10;
        } else {
            a = b;
        }
    }

    private static void test1()
    {
        int a = 10;
        int b = 42;

        var c = (a + b) / 52 * 100;
    }
}
