// Ashley Voglewede
// COP 4520 Spring 2022
// Programming Assignment 2 - Minotaur's Crystal Vase

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

// This program uses multithreading to solve the Minotaur's Crystal Vase problem, where
// every guest must see the crystal vase, but can only enter when the showroom sign 
// shows "Available." The program ends when all guests have entered the showroom and seen
// the crystal vase.
public class MinotaursCrystalVase extends Thread {
    public static ArrayList<Showroom> threads = new ArrayList<Showroom>();
    ReentrantLock lock = new ReentrantLock();
    int numThreads;

    Showroom getThread(int index) {
        return threads.get(index);
    }

    // Constructor to assign the number of guests that the user inputs.
    MinotaursCrystalVase(int numThreads) {
        this.numThreads = numThreads;
    }

    void PartyTime(MinotaursCrystalVase mainThread) throws InterruptedException {
        // Add threads with assigned values of 1 - n.
        for (int i = 1; i <= mainThread.numThreads; i++)
            threads.add(new Showroom(i, mainThread));

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).start();

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).join();

        System.out.println("Hooray!! All " + mainThread.numThreads + " guests have entered the showroom and seen the Minotaur's Crystal Vase.");
    }
    public static void main(String args[]) throws InterruptedException {   
        Scanner scan = new Scanner(System.in);
        System.out.print("Please Enter the Number of Guests: ");

        // Scan in the users input, only allowing positive integer values as a valid number of guests.
        while (!scan.hasNextInt()) {
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
        MinotaursCrystalVase mainThread = new MinotaursCrystalVase(numGuests);
        mainThread.PartyTime(mainThread);
        final long end = System.currentTimeMillis();
        final long executionTime = end - start;
        System.out.println("Total Execution Time: " + executionTime + " ms");
    }
}

class Showroom extends Thread {
    // The showroom sign indicates whether the showroom is available. If set to true, it 
    // shows "Available" and a guest can enter. If set to false, it shows "Busy" and the guest
    // cannot enter. 
    static AtomicBoolean showroomSign = new AtomicBoolean(true);
    static AtomicBoolean stopThreads = new AtomicBoolean(false);
    MinotaursCrystalVase mainThread;
    boolean seenVase = false;
    int threadNumber;

    Showroom(int threadNumber, MinotaursCrystalVase mainThread) {
        this.threadNumber = threadNumber;
        this.mainThread = mainThread;
    }

    @Override
    public void run() {
        while (true) {
            if (stopThreads.get())
                break;

            boolean stop = true;

            // Conditional to check if the showroom is set to "Available" (true) or "Busy" (false). 
            // If the room is set to "Available" and the guest enters, they are responsible to 
            // set the sign to "Busy" when entering, and back to "Available" upon exit.
            if (showroomSign.get()) {
                showroomSign.set(false);
                this.seenVase = true;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                showroomSign.set(true);
            }
            else {
                continue;
            }
                
            // Ensure that every guest has seen the vase.
            for (int i = 0; i < mainThread.numThreads; i++)
                if (!mainThread.getThread(i).seenVase) {
                    stop = false;
                    break;
                }
            
            if (stop) {
                stopThreads.set(true);
                break;
            }
        }
    }
}