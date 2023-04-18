using System;
using System.Threading;

namespace ParallelMin
{
    class Program
    {
        private const int DIM = 100;
        private const int THREAD_NUM = 4;

        private static int finalMin = DIM;
        private static int index = 0;

        private static int[] arr = new int[DIM];

        private static void initArr()
        {
            for (int i = 0; i < DIM; i++)
            {
                arr[i] = i;
            }
        }

        private static int partMin(int startIndex, int finishIndex)
        {
            int min = DIM;
            for (int i = startIndex; i < finishIndex; i++)
            {
                if (arr[i] < min)
                {
                    min = arr[i];
                }
            }
            return min;
        }

        private class PartManager
        {
            private int tasksCount = 0;
            private int partMin = DIM;
            private readonly object lockObj = new object();

            public void SetPartMin(int min)
            {
                lock (lockObj)
                {
                    if (partMin > min)
                    {
                        partMin = min;
                    }
                    tasksCount++;
                    if (tasksCount == THREAD_NUM)
                    {
                        Monitor.Pulse(lockObj);
                    }
                }
            }

            public int GetMin()
            {
                lock (lockObj)
                {
                    while (tasksCount < THREAD_NUM)
                    {
                        Monitor.Wait(lockObj);
                    }
                    return partMin;
                }
            }
        }

        private class StarterThread
        {
            private readonly PartManager partManager;
            private readonly int start;
            private readonly int finish;

            public StarterThread(PartManager partManager, int start, int finish)
            {
                this.partManager = partManager;
                this.start = start;
                this.finish = finish;
            }

            public void Run()
            {
                int min = partMin(start, finish);
                partManager.SetPartMin(min);
            }
        }

        private static int parallelMin()
        {
            int min = DIM;
            StarterThread[] threads = new StarterThread[THREAD_NUM];
            PartManager partManager = new PartManager();
            int partDim = DIM / THREAD_NUM;

            Random rnd = new Random();
            int index = rnd.Next(DIM);
            arr[index] = -10;

            for (int i = 0; i < THREAD_NUM; i++)
            {
                if (i == THREAD_NUM - 1)
                {
                    threads[i] = new StarterThread(partManager, partDim * i, DIM);
                }
                else
                {
                    threads[i] = new StarterThread(partManager, partDim * i, partDim * (i + 1));
                }
                new Thread(threads[i].Run).Start();
            }

            min = partManager.GetMin();

            return min;
        }

        static void Main(string[] args)
        {
            initArr();
            finalMin = parallelMin();
            for (int i = 0; i < arr.Length; i++)
            {
                if (arr[i] == finalMin)
                {
                    index = i;
                    break;
                }
            }
            Console.WriteLine("The minimum element is " + finalMin + " and its index is " + index);
        }
    }
}
