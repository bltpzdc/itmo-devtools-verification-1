public class Test {
    public static void main(String[] args) {
        outer: while (true) {
            a = 10;
            while (true) { 
                a = 11;
                break;
            }
            a = 12;
        }
    }
}
