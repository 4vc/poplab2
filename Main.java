import java.util.Random;

public class Main {
    private static final int DIM = 10000;
    private static final int THREAD_NUM = 4;

    private static int finalMin = DIM;
    private static int index = 0;

    private static int[] arr = new int[DIM];

    private static void initArr() {
        for (int i = 0; i < DIM; i++) {
            arr[i] = i;
        }
    }

    private static int partMin(int startIndex, int finishIndex) {
        int min = DIM;
        for (int i = startIndex; i < finishIndex; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }

    private static class PartManager {
        private int tasksCount = 0;
        private int partMin = DIM;

        public synchronized void setPartMin(int min) {
            if (partMin > min) {
                partMin = min;
            }
            tasksCount++;
            if (tasksCount == THREAD_NUM) {
                notify();
            }
        }

        public synchronized int getMin() throws InterruptedException {
            while (tasksCount < THREAD_NUM) {
                wait();
            }
            return partMin;
        }
    }

    private static class StarterThread extends Thread {
        private final PartManager partManager;
        private final int start;
        private final int finish;

        public StarterThread(PartManager partManager, int start, int finish) {
            this.partManager = partManager;
            this.start = start;
            this.finish = finish;
        }

        public void run() {
            int min = partMin(start, finish);
            partManager.setPartMin(min);
        }
    }

    private static int parallelMin() throws InterruptedException {
        int min = DIM;
        StarterThread[] threads = new StarterThread[THREAD_NUM];
        PartManager partManager = new PartManager();
        int partDim = DIM / THREAD_NUM;

        Random rnd = new Random();
        int index = rnd.nextInt(DIM);
        arr[index] = -10;

        for (int i = 0; i < THREAD_NUM; i++) {
            if (i == THREAD_NUM - 1) {
                threads[i] = new StarterThread(partManager, partDim * i, DIM);
            } else {
                threads[i] = new StarterThread(partManager, partDim * i, partDim * (i + 1));
            }
            threads[i].start();
        }

        min = partManager.getMin();

        return min;
    }

    public static void main(String[] args) throws InterruptedException {
        initArr();
        finalMin = parallelMin();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == finalMin) {
                index = i;
                break;
            }
        }
        System.out.println("The minimum element is " + finalMin + " and its index is " + index);
    }
}
