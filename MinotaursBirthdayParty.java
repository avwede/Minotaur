// Ashley Voglewede
// COP 4520 Spring 2022
// Programming Assignment 2 - Minotaur's Birthday Party

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

// This program uses multithreading to solve the Minotaur's Birthday Party problem, where
// guests can enter the Minotaur's labyrinth and eat a birthday cupcake. The program 
// ends when one guest announces that all guests have entered the Minotaur's labyrinth. 
public class MinotaursBirthdayParty extends Thread {
    public static ArrayList<Labyrinth> threads = new ArrayList<>();
    ReentrantLock lock = new ReentrantLock();
    int numThreads;

    Labyrinth getThread(int index) {
        return threads.get(index);
    }

    // Constructor to assign the number of guests that the user inputs.
    MinotaursBirthdayParty(int numThreads) {
        this.numThreads = numThreads;
    }

    void PartyTime(MinotaursBirthdayParty mainThread) throws InterruptedException {
        // Assigns a random guest as the leader, who will keep track of all guests
        // to make sure that they have all entered the labyrinth.
        int leader = (int)(Math.random() * mainThread.numThreads + 1);

        // Add threads with assigned values of 1 - n.
        for (int i = 1; i <= mainThread.numThreads; i++) {
            if (i == leader)
                threads.add(new Labyrinth(i, mainThread, true));
            else
                threads.add(new Labyrinth(i, mainThread, false));
        }

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).start();

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).join();
    }

    public static void main(String args[]) throws InterruptedException {   
        Scanner scan = new Scanner(System.in);
        System.out.print("Please Enter the Number of Guests: ");

        // Scan in the users input, only allowing positive integer values as a valid number of guests.
        while(!scan.hasNextInt()){
            System.out.print("Error: Invalid input. Please input a positive integer value.\n");
            System.out.print("Please Enter the Number of Guests: \n");
            scan.nextLine();
        }

        int numGuests = scan.nextInt();

        if (numGuests < 0) {
            System.out.println("Error: Invalid input. Please try again with a positive integer value.");
            scan.close();
            return;
        }
            
        scan.close();

        final long start = System.currentTimeMillis();
        MinotaursBirthdayParty mainThread = new MinotaursBirthdayParty(numGuests);
        mainThread.PartyTime(mainThread);
        final long end = System.currentTimeMillis();
        final long executionTime = end - start;
        System.out.println("Total Execution Time: " + executionTime + " ms");
    }
}

class Labyrinth extends Thread {
    static AtomicBoolean stopThreads = new AtomicBoolean(false);
    static AtomicBoolean cupcake = new AtomicBoolean(true);
    MinotaursBirthdayParty mainThread;
    boolean isLeader = false, eatenCupcake = false;
    int threadNumber, visits;
    
    Labyrinth(int threadNumber, MinotaursBirthdayParty mainThread, boolean isLeader) {
        this.threadNumber = threadNumber;
        this.mainThread = mainThread;
        this.isLeader = isLeader;
    }

    @Override
    public void run() {   
        if (isLeader) {
            this.visits = 1;
        }
            
        while(true) {   
            mainThread.lock.lock();

            if (stopThreads.get()) {
                mainThread.lock.unlock();
                break;
            }
            
            // Yay! All guests have entered the labryinth. Stop all threads and unlock the Reentrant Lock.
            if (this.visits == mainThread.numThreads) {
                System.out.println("Hooray!! All guests have entered the Minotaur's Labyrinth.");
                stopThreads.set(true);
                mainThread.lock.unlock();
                break;
            }

            try {
                if (this.eatenCupcake) {
                    return;
                } else if (cupcake.get() && !isLeader) {
                    cupcake.set(false);
                    this.eatenCupcake = true;
                } else {   
                    if (isLeader) {
                        cupcake.set(true);
                        this.visits += 1;
                    }
                }
            } finally {
                mainThread.lock.unlock();
            }
        }
    }
}