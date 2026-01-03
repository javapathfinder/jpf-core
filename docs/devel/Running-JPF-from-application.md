# Embedded JPF #
JPF can also be used embedded, i.e. called from another Java application. A basic code sequence to start JPF looks like this:

~~~~~~~~ {.java}
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;

void runJPF (String[] args) {
   ..
   MyListener listener = new MyListener(..);

   // [optionally] if you pass through command line args, 
   // 'null' any consumed args not to be JPF-processed
   listener.filterArgs( args);
   ..

   Config config = JPF.createConfig( args);
   // set special config key/value pairs here..

   JPF jpf = new JPF( config);
   jpf.addListener( listener);
   jpf.run();
   ..
}
~~~~~~~~

Of course, you can also call `gov.nasa.jpf.JPF.main(args)` from within your application, if you don't need to control JPF's configuration or process it's output. 

