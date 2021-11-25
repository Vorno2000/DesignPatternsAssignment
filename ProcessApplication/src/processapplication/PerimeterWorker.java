/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *PeriterWorker class uses information of a shape and length 
 *of one edge to calculate the petimeter of that 2D shape
 *
 *@author 
 */

public class PerimeterWorker extends process implements Listener, Runnable {
    private static Queue<Task> queue = new ConcurrentLinkedQueue();
    private static NotificationQueue nextQueue;
    private ArrayList<PerimeterWorker> workerThreads;
    private boolean stopRequested = false;
    private final Object lock;
        
    public PerimeterWorker(Object lock) {this.lock = lock;}
    
    /**
     * Constructor which takes in parameters of the number of threads to start running and reference to the next queue for tasks to be sent once 
     * PerimeterWorker has calculated the perimeter
     * 
     * @param workerThreadNum	Number of worker threads to create
     * @param nextQueue		The next queue to send tasks to
     */
    public PerimeterWorker(int workerThreadNum, NotificationQueue nextQueue) {
        workerThreads = new ArrayList<>();
        lock = new Object();
        PerimeterWorker.nextQueue = nextQueue;
        for(int i = 0; i != workerThreadNum; i++) {
            PerimeterWorker perimeterWorker = new PerimeterWorker(lock);
            workerThreads.add(perimeterWorker);
            Thread t = new Thread(perimeterWorker);
            t.start();
        }
    }

    @Override
    public void process(Task task) {
        try {
            Task clonedTask = (Task)task.clone();
            queue.offer(clonedTask);
            threadControl();
        } catch (CloneNotSupportedException ex) {
            System.err.println("Error cloning task"+ex);
        }
    }

    /**
     * Method which undertakes the process of calculating the perimeters of shapes based on the task's String and int values
     * 
     * @param task
     */
    @Override
    void processStep(Task task) {
        try {
            System.out.println("Calculating Perimeter for Task:"+task.getIdentifier());
            Thread.sleep(5000);
        } catch (InterruptedException ex) {}
        int edges=0;
        if(task.getShape().equals("square")) {
            edges = 4;
        } else if (task.getShape().equals("triangle")) {
            edges = 3;
        } else if (task.getShape().equals("pentagon")) {
            edges = 5;
        }
        task.setShapePerimeter(task.getShapeOneSide()* edges);
        System.out.println("Completed Perimeter Calculation for Task:"+task.getIdentifier());
        nextQueue.offer(task);
    }
    
    public void requestStop() {
        stopRequested = true;
    }
    
    public void stopWorkers() {
        for(PerimeterWorker perimeterWorker : workerThreads) {
            perimeterWorker.requestStop();
        }
    }
    
    public void threadControl() {
        try {
            synchronized(lock) {
                if(queue.isEmpty())
                    lock.wait();
                else {
                    for(int i = 0; i != queue.size(); i++)
                        lock.notify();
                }
            }   
            
        } catch (InterruptedException ex) {}
    }

    @Override
    public void run() {
        while(!stopRequested) {
            Task task = null;
            if(!queue.isEmpty()) {
                task = queue.poll();
            }
            else {
                threadControl();
            }
            if(task != null) {
                processStep(task);
            }
        }
    }
}
