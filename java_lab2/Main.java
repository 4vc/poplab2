public class Main {
    public static void main(String[] args) {
        int dim = 1337;
        int threadNum = 2;
        ArrClass arrClass = new ArrClass(dim, threadNum);

        int min = arrClass.threadMin();
        System.out.println("Min element -> " + (min));
        System.out.println("Index min -> " + arrClass.index_min(min));
    }
}