import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Sean Gerhardt on 5/26/2015.
 */
public class HorseRace {
    //Used to help us start threads as close to the same time as possible
    static CyclicBarrier gate = null;

    // Display a message, preceded by the name of the current thread
    static void threadMessage(String message) {
        String threadName =
                Thread.currentThread().getName();
        System.out.format("%s: %s%n",
                threadName,
                message);
    }


    private static class Horse implements Runnable {
        private int distanceTravelled = 0;
        private String name = "";

        private static int place = 1;

        Horse(String horseName) {
            name = horseName;
        }

        public void run() {
            try {
                gate.await();
                while (distanceTravelled < 100) {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    distanceTravelled += this.gallop();
                    // Print a message
                    if (distanceTravelled < 100) {
                        threadMessage(name + " has galloped " + distanceTravelled + " meters");
                    } else {
                        String placeSuffix;
                        if (place == 1) {
                            placeSuffix = "st";
                        } else if (place == 2) {
                            placeSuffix = "nd";
                        } else if (place == 3) {
                            placeSuffix = "rd";
                        } else {
                            placeSuffix = "th";
                        }
                        threadMessage(name + " has crossed the finish line and finished " + place + placeSuffix);
                        // Increment the static variable. This variable is shared across all instances of horse.
                        place++;
                    }
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private int gallop() {
            Random rand = new Random();
            int upperBound = 15;
            int lowerBound = 6;
            return rand.nextInt(upperBound - lowerBound) + lowerBound;
        }
    }


    public static void main(String args[]) throws InterruptedException, BrokenBarrierException {
        // Delay, in milliseconds before we interrupt MessageLoop thread (default one hour).
        long patience = 1000 * 60 * 60;

        // If command line argument present, gives patience in seconds.
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();

        Horse[] horses = new Horse[]{new Horse("Show Me The Money"), new Horse("American Pharoah"),
                new Horse("Sea Hero"), new Horse("Street Sense")};

        Thread[] threads = new Thread[horses.length];

        gate = new CyclicBarrier(horses.length + 1);

        for (int i = 0; i < horses.length; i++) {
            threads[i] = new Thread(horses[i]);
            threads[i].start();
        }
        //The count on the gate is now met, open the gate and start the race!
        gate.await();
        threadMessage("Waiting for Horse threads to finish");
        // loop until the last horse/thread finishes
        for (int i = 0; i < horses.length; i++) {
            while (threads[i].isAlive()) {
                threadMessage("Still waiting...");
                // The join method allows one thread to wait for the completion of another.
                // Wait a maximum of 1 second for the thread to finish.
                threads[i].join(1000);
                if (((System.currentTimeMillis() - startTime) > patience)
                        && threads[i].isAlive()) {
                    threadMessage("Tired of waiting!");
                    threads[i].interrupt();
                    // Shouldn't be long now
                    // -- wait indefinitely
                    threads[i].join();
                }
            }
        }
        threadMessage("Finally!");
    }

}
