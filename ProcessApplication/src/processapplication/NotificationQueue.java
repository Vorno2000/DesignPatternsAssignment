/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

//Notification Queue class
/**
 * created in server alongside worker pools - worker pools are then 
 * passed into the addListener to assign that worker pool to this queue
 * After a task is added - the notify method is called to tell the workers
**/

public class NotificationQueue extends AbstractQueue<Task> implements Subject{
    private ArrayList<Listener> listeningWorkers;
    private AbstractQueue<Task> taskQueue;
    private ProcessServer server;
    private NotifierThread thread;

    public NotificationQueue() {
        taskQueue = new LinkedBlockingQueue();
        listeningWorkers = new ArrayList<>();
        thread = new NotifierThread(this);
        Thread t = new Thread(thread);
        t.start();
    }
    
    public NotificationQueue(ProcessServer server) {
        this.server = server;
        taskQueue = new LinkedBlockingQueue();
        listeningWorkers = new ArrayList<>();
        thread = new NotifierThread(this);
        Thread t = new Thread(thread);
        t.start();
    }

    public NotificationQueue(AbstractQueue<Task> queue) {
        this.taskQueue = queue;
        listeningWorkers = new ArrayList<>();
        thread = new NotifierThread(this);
        Thread t = new Thread(thread);
        t.start();
    }

    //Add a worker listener so that when the queue changes, the listener is notified
    @Override
    public void addListener(Listener addListener) {
        listeningWorkers.add(addListener);
    }

    @Override
    public void removeListener(Listener removeListener) {
        listeningWorkers.remove(removeListener);
    }

    //passes the task to workers process and notifies threads
    @Override
    public void notifyListeners() {
        if(!listeningWorkers.isEmpty()) {
            for(Listener listener : listeningWorkers)
                listener.process(taskQueue.poll());
        }
        else
            server.sendTask(taskQueue.poll());
    }

    @Override
    public Iterator<Task> iterator() {
        return taskQueue.iterator();
    }

    @Override
    public int size() {
        return taskQueue.size();
    }

    @Override
    public boolean offer(Task e) {
        return taskQueue.offer(e);
    }

    @Override
    public Task poll() {
        return taskQueue.poll();
    }

    @Override
    public Task peek() {
        try {
            Task task = taskQueue.peek();
            if(task != null)
                return task;
            else
                return null;
        } catch(NullPointerException e) {return null;}
    }
    
    public void stop() {
        thread.stop();
    }
}

//thread to check when a task is added to the queue
class NotifierThread implements Runnable {
    private NotificationQueue notificationQueue;
    private boolean isAlive = true;

    public NotifierThread(NotificationQueue notificationQueue) {
        this.notificationQueue = notificationQueue;
    }

    @Override
    public void run() {
        while(isAlive) {
            try {
                Task task = notificationQueue.peek();

                if(task != null) {
                    notificationQueue.notifyListeners();
                }
            } catch(NullPointerException e) {}

        }
    }

    public void stop() {
        isAlive = false;
    }
}

