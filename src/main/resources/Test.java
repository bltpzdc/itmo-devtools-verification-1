public class Test {
    int fib(int n) {
        int a = 0, b = 1, c, i;
        if (n < 2) return n;
        outer: for (i = 1, c = 0; i < n; i++)
        {
            c = a + b;
            a = b;
            b = c;
            if ( false ) {
                break outer;
            }
        }
        return c;
    }

    public static void main(String[] args) {
        
    }
}
