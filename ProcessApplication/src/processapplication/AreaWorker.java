/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AreaWorker extends process implements Listener, Runnable {
    private static Queue<Task> queue = new ConcurrentLinkedQueue();
    private static NotificationQueue nextQueue;
    private ArrayList<AreaWorker> workerThreads;
    private boolean stopRequested = false;
    private final Object lock;
        
    public AreaWorker(Object lock) {this.lock = lock;}

    /**
     * Constructor which takes in parameters of the number of threads to start running and reference to the next queue for tasks to be sent once 
     * ArearWorker has calculated the perimeter
     * 
     * @param workerThreadNum	Number of worker threads to create
     * @param nextQueue		The next queue to send tasks to
     */
    public AreaWorker(int workerThreadNum, NotificationQueue nextQueue) {
        workerThreads = new ArrayList<>();
        lock = new Object();
        this.nextQueue = nextQueue;
        for(int i = 0; i != workerThreadNum; i++) {
            AreaWorker areaWorker = new AreaWorker(lock);
            workerThreads.add(areaWorker);
            Thread t = new Thread(areaWorker);
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
     * Method which undertakes the process of calculating the areas of shapes based on the task's String and int values
     * 
     * @param task
     */
    @Override
    void processStep(Task task) {
        try {
            System.out.println("Calculating Area for Task:"+task.getIdentifier());
            Thread.sleep(5000);
        } catch (InterruptedException ex) {}
        double area=0;
        if(task.getShape().equals("square")) {
            area = task.getShapeOneSide()*task.getShapeOneSide();
        } else if (task.getShape().equals("triangle")) {
            area = ((Math.sqrt(3)/4) * (task.getShapeOneSide()*task.getShapeOneSide()));
        } else if (task.getShape().equals("pentagon")) {
            area = 0.25 * Math.sqrt(5*((5+2)*(Math.sqrt(5)))*(task.getShapeOneSide()*task.getShapeOneSide()));
        }
        task.setShapeArea(area);
        System.out.println("Completed Area Calculation for Task:"+task.getIdentifier());
        nextQueue.offer(task);
    }

    public void requestStop() {
        stopRequested = true;
    }
    
    public void stopWorkers() {
        for(AreaWorker areaWorker : workerThreads) {
            areaWorker.requestStop();
        }
    }
    
    public void threadControl() {
        try {
            synchronized(lock) {
                if(queue.isEmpty())
                    lock.wait();
                else {
                    lock.notifyAll();
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
