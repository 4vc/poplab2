public class ThreadMin implements Runnable {
    private final ArrClass arrClass;
    private final int startIndex;
    private final int finishIndex;
    public ThreadMin(int startIndex, int finishIndex, ArrClass arrClass) {
        this.arrClass = arrClass;
        this.startIndex = startIndex;
        this.finishIndex = finishIndex;
    }

    @Override
    public void run() {
        int min = arrClass.minPart(startIndex, finishIndex);
        arrClass.collectMin(min);
        arrClass.incThreadCount();
    }
}
