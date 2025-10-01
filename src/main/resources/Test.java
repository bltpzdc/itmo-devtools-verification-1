public class Test {
    int fib(int n) {
        int a = 0, b = 1, c, i;
        if (n < 2) return n;
        for (i = 1; i < n; i++)
        {
            c = a + b;
            a = b;
            b = c;
        }

        return c;
    }

    void sort(int[] a) {
        for ( int i = 0; i < a.length; ++i ) {
            for ( int j = i + 1; j < a.length; ++j ) {
                if ( a[i] > a[j] ) {
                    int tmp = a[i];
                    a[i] = a[j];
                    a[j] = tmp;
                }
            }
        }
    }

    void testBreak() {
        int i = 0;
        outer: do { 
            ++i;
            if ( i == Integer.MAX_VALUE ) break;
            if ( i % 2 == 0 ) {
                break outer;
            }
        } while (true);
    }

    void testContinue() {
        int i = 0;
        outer: while ( i++ < Integer.MAX_VALUE ) {
            inner: if ( i % 2 == 0 ) {
                continue outer;
            } else if ( i % 3 == 0 ) {
                continue;
            }

            System.out.println(i);
        }
    }

    void testSwitch() {
        int i = 0;
        outer: switch ( i ) {
            case 1:
                ++i;
                i *= 2;
                break;

            case 2:
                i *= 3;
                break outer;

            case 3:
                i *= 4;
            case 4:
            case 5:
                i += 5;
            default:
                i = 0;
        }
    }
}
