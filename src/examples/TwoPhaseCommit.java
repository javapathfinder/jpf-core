/*

@author Ankush Desai

*/


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Thread;
/**
 A simplified two phase commit protocol to demonstrate how we can perform conformance testing using JPF
 */

public class TwoPhaseCommit {

    public static class BoundedBuffer {
        //--- the bounded buffer implementation
        protected Object[] buf;
        protected int in = 0;
        protected int out = 0;
        protected int count = 0;
        protected int size;

        public BoundedBuffer(int size) {
            this.size = size;
            buf = new Object[size];
        }

        public synchronized void put(Object o) throws InterruptedException {
            while (count == size) {
                wait();
            }
            buf[in] = o;
            //System.out.println("PUT from " + Thread.currentThread().getName());
            ++count;
            in = (in + 1) % size;
            notify(); // if this is not a notifyAll() we might notify the wrong waiter
        }

        public synchronized Object get() throws InterruptedException {
            while (count == 0) {
                wait();
            }
            Object o = buf[out];
            buf[out] = null;
            //System.out.println("GET from " + Thread.currentThread().getName());
            --count;
            out = (out + 1) % size;
            notify(); // if this is not a notifyAll() we might notify the wrong waiter
            return (o);
        }
    }

    // declaring all events
    public static class ePrepareSuccess {
        public ePrepareSuccess() {

        }
    }

    public static class eGlobalAbort {
        public eGlobalAbort() {
        }
    }

    public static class eGlobalCommit {
        public eGlobalCommit() {
        }
    }

    public static class ePrepareFailed {
        public ePrepareFailed() {
        }
    }

    public static class ePrepare {
        public Coordinator coor;

        public ePrepare(Coordinator c) {
            coor = c;
        }
    }

    //--- the Coordinator
    public static class Coordinator extends Thread {
        List<Participant> participants;
        BoundedBuffer buf;

        public Coordinator(List<Participant> participants) {
            buf = new BoundedBuffer(10);
            (this).participants = participants;
        }

        public void Send(Object message) {
            try {
                buf.put(message);
            } catch (InterruptedException e) { }
        }
        public Object Recv() {
            try {
                Object ret = buf.get();
                CoordinatorConformanceCheckingMonitor.CheckConformance(ret);
                return ret;
            } catch (InterruptedException e) {
                return null;
            }
        }
        public void SendToAllParticipants(Object message) {
            for (int i = 0; i < participants.size(); i++) {
                CoordinatorConformanceCheckingMonitor.CheckConformance(message);
                participants.get(i).Send(message);
            }
        }

        @Override
        public void run() {
            int totalSuccReceived = 0;
            int currentState = 0;
            while (true) {

                switch (currentState) {
                    case 0: {
                        //send prepare message to all participants
                        SendToAllParticipants(new ePrepare(this));
                        currentState = 1;
                    }
                    break;
                    case 1: {
                        //wait for responses
                        Object message = Recv();
                        if (message instanceof ePrepareSuccess) {
                            totalSuccReceived++;
                            if (totalSuccReceived == participants.size()) {
                                SendToAllParticipants(new eGlobalCommit());
                                currentState = 2;
                            }
                        } else if (message instanceof ePrepareFailed) {
                            SendToAllParticipants(new eGlobalAbort());
                            currentState = 2;
                        }
                    }
                    break;
                    default:
                }

            }
        }
    }

    public static class CoordinatorConformanceCheckingMonitor {
        static int stateofmonitor = 0;
        static int count = 0;
        public static void CheckConformance(Object message) {
            switch(stateofmonitor)
            {
                case 0: {
                    //coordinator first sends 2 prepares
                    assert message instanceof ePrepare: "Expecting ePrepare Message";
                    count++;
                    if(count == 2)
                    {
                        stateofmonitor = 1;
                        count = 0;
                    }
                    break;

                }
                case 1:
                    //coordinator must wait for prepare success or prepare failure messages
                    if(message instanceof ePrepareSuccess)
                    {
                        count++;
                        if(count == 2)
                        {
                            stateofmonitor = 2;
                        }
                    }
                    else if(message instanceof ePrepareFailed)
                    {
                        stateofmonitor = 3;
                    }
                    else
                    {
                        assert false: "Expected to receive ePrepareSuccess or ePrepareFailed, but saw " + message.getClass().getSimpleName();
                    }
                    break;
                case 2:
                    assert message instanceof  eGlobalCommit: "Expected eGlobalCommit but saw " + message.getClass().getSimpleName();
                    stateofmonitor = 10;
                    break;
                case 3:
                    assert message instanceof  eGlobalAbort: "Expected eGlobalAbort but saw " + message.getClass().getSimpleName();
                    stateofmonitor = 10;
                    break;

                default:


            }
        }
    }
    //--- the consumer
    public static class Participant extends Thread {
        BoundedBuffer buf;

        public Participant() {
            buf = new BoundedBuffer(10);
        }

        @Override
        public void run() {
            int currentState = 0;
            while (true) {
                switch (currentState) {
                    case 0: {
                        //wait for responses
                        Object message = Recv();
                        if (message instanceof ePrepare) {
                            Random random = new Random(0);
                            if (random.nextBoolean()) {
                                ((ePrepare) message).coor.Send(new ePrepareFailed());
                            } else {
                                ((ePrepare) message).coor.Send(new ePrepareSuccess());
                            }
                            currentState = 1;
                        }
                        break;
                    }
                    default:
                }
            }
        }


        public void Send(Object message) {
            try {
                buf.put(message);
            } catch (InterruptedException e) {
            }
        }

        public Object Recv() {
            try {
                return buf.get();
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    //--- the test driver
    public static void main(String[] args) {
        //System.out.printf("running BoundedBuffer with buffer-size %d, %d producers and %d consumers\n", BUFFER_SIZE, N_PRODUCERS, N_CONSUMERS);
        List<Participant> parts = new ArrayList<>();
        parts.add(new Participant());
        parts.add(new Participant());
        new Coordinator(parts).start();
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).start();
        }
    }
}
