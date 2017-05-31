/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.report;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * this is our default report generator, which is heavily configurable
 * via our standard properties. Note this gets instantiated and
 * registered automatically via JPF.addListeners(), so you don't
 * have to add it explicitly
 */

public class Reporter extends SearchListenerAdapter {

  public static Logger log = JPF.getLogger("report");

  protected Config conf;
  protected JPF jpf;
  protected Search search;
  protected VM vm;

  protected Date started, finished;
  protected Statistics stat; // the object that collects statistics
  protected List<Publisher> publishers = new ArrayList<Publisher>();
  
  protected Thread probeTimer;
  
  public Reporter (Config conf, JPF jpf) {
    this.conf = conf;
    this.jpf = jpf;
    search = jpf.getSearch();
    vm = jpf.getVM();
    int probeInterval = conf.getInt("report.probe_interval");
    boolean reportStats = conf.getBoolean("report.statistics", false) || (probeInterval > 0);

    started = new Date();

    addConfiguredPublishers(conf);

    for (Publisher publisher : publishers) {
      if (reportStats || publisher.hasToReportStatistics()) {
        reportStats = true;
      }

      if (publisher instanceof JPFListener) {
        jpf.addListener((JPFListener)publisher);
      }
    }

    if (reportStats){
      getRegisteredStatistics();
    }
    
    if (probeInterval > 0){
      probeTimer = createProbeIntervalTimer(probeInterval);
    }
  }

  protected Thread createProbeIntervalTimer (final int probeInterval){
    Thread timer = new Thread( new Runnable(){
        @Override
		public void run(){
          log.info("probe timer running");
          while (!search.isDone()){
            try {
              Thread.sleep( probeInterval * 1000);
              search.probeSearch(); // this is only a request
            } catch (InterruptedException ix) {
              // nothing
            }
          }
          log.info("probe timer terminating");
        }
     }, "probe-timer");
    timer.setDaemon(true);
    
    // we don't start before the Search is started
    
    return timer;
  }
  
  /**
   * called after the JPF run is finished. Shouldn't be public, but is called by JPF
   */
  public void cleanUp(){
    // nothing yet
  }
  
  public Statistics getRegisteredStatistics(){
    
    if (stat == null){ // none yet, initialize
      // first, check if somebody registered one explicitly
      stat = vm.getNextListenerOfType(Statistics.class, null);
      if (stat == null){
        stat = conf.getInstance("report.statistics.class@stat", Statistics.class);
        if (stat == null) {
          stat = new Statistics();
        }
        jpf.addListener(stat);
      }
    }
    
    return stat;
  }
  
  
  void addConfiguredPublishers (Config conf) {
    String[] def = { "console" };

    Class<?>[] argTypes = { Config.class, Reporter.class };
    Object[] args = { conf, this };

    for (String id : conf.getStringArray("report.publisher", def)){
      Publisher p = conf.getInstance("report." + id + ".class",
                                     Publisher.class, argTypes, args);
      if (p != null){
        publishers.add(p);
      } else {
        log.warning("could not instantiate publisher class: " + id);
      }
    }
  }

  public void addPublisher( Publisher newPublisher){
    publishers.add(newPublisher);
  }
  
  public List<Publisher> getPublishers() {
    return publishers;
  }

  public boolean hasToReportTrace() {
    for (Publisher p : publishers) {
      if (p.hasTopic("trace")) {
        return true;
      }
    }

    return false;
  }

  public boolean hasToReportOutput() {
    for (Publisher p : publishers) {
      if (p.hasTopic("output")) {
        return true;
      }
    }

    return false;
  }


  public <T extends Publisher> boolean addPublisherExtension (Class<T> publisherCls, PublisherExtension e) {
    boolean added = false;
    for (Publisher p : publishers) {
      Class<?> pCls = p.getClass();
      if (publisherCls.isAssignableFrom(pCls)) {
        p.addExtension(e);
        added = true;
      }
    }

    return added;
  }

  public <T extends Publisher> void setPublisherItems (Class<T> publisherCls,
                                                        int category, String[] topics){
    for (Publisher p : publishers) {
      if (publisherCls.isInstance(p)) {
        p.setItems(category,topics);
        return;
      }
    }
  }

  boolean contains (String key, String[] list) {
    for (String s : list) {
      if (s.equalsIgnoreCase(key)){
        return true;
      }
    }
    return false;
  }


  //--- the publishing phases
  
  protected void publishStart() {
    for (Publisher publisher : publishers) {
      publisher.openChannel();
      publisher.publishProlog();
      publisher.publishStart();
    }
  }

  protected void publishTransition() {
    for (Publisher publisher : publishers) {
      publisher.publishTransition();
    }
  }

  protected void publishPropertyViolation() {
    for (Publisher publisher : publishers) {
      publisher.publishPropertyViolation();
    }
  }

  protected void publishConstraintHit() {
    for (Publisher publisher : publishers) {
      publisher.publishConstraintHit();
    }
  }

  protected void publishFinished() {
    for (Publisher publisher : publishers) {
      publisher.publishFinished();
      publisher.publishEpilog();
      publisher.closeChannel();
    }
  }

  protected void publishProbe(){
    for (Publisher publisher : publishers) {
      publisher.publishProbe();
    }    
  }
  
  //--- the listener interface that drives report generation

  @Override
  public void searchStarted (Search search){
    publishStart();
    
    if (probeTimer != null){
      probeTimer.start();
    }
  }

  @Override
  public void stateAdvanced (Search search) {
    publishTransition();
  }

  @Override
  public void searchConstraintHit(Search search) {
    publishConstraintHit();
  }

  @Override
  public void searchProbed (Search search){
    publishProbe();
  }

  @Override
  public void propertyViolated (Search search) {
    publishPropertyViolation();
  }

  @Override
  public void searchFinished (Search search){
    finished = new Date();

    publishFinished();
    
    if (probeTimer != null){
      // we could interrupt, but it's a daemon anyways
      probeTimer = null;
    }
  }


  //--- various getters
  
  public Date getStartDate() {
    return started;
  }

  public Date getFinishedDate () {
    return finished;
  }
    
  public VM getVM() {
    return vm;
  }

  public Search getSearch() {
    return search;
  }

  public List<Error> getErrors () {
    return search.getErrors();
  }

  public Error getCurrentError () {
    return search.getCurrentError();
  }

  public String getLastSearchConstraint () {
    return search.getLastSearchConstraint();
  }

  public String getCurrentErrorId () {
    Error e = getCurrentError();
    if (e != null) {
      return "#" + e.getId();
    } else {
      return "";
    }
  }

  public int getNumberOfErrors() {
    return search.getErrors().size();
  }

  public Statistics getStatistics() {
    return stat;
  }

  public Statistics getStatisticsSnapshot () {
    return stat.clone();
  }
  
  /**
   * in ms
   */
  public long getElapsedTime () {
    Date d = (finished != null) ? finished : new Date();
    long t = d.getTime() - started.getTime();
    return t;
  }

  public Path getPath (){
    return vm.getClonedPath();
  }

  public String getJPFBanner () {
    StringBuilder sb = new StringBuilder();
    
    sb.append("JavaPathfinder core system v");
    sb.append(JPF.VERSION);
    
    String rev = getRevision();
    if (rev != null){
      sb.append(" (rev ");
      sb.append(rev);
      sb.append(')');
    }
    
    sb.append(" - (C) 2005-2014 United States Government. All rights reserved.");
    
    if (conf.getBoolean("report.show_repository", false)) {
      String repInfo =  getRepositoryInfo();
      if (repInfo != null) {
        sb.append( repInfo);
      }
    }
    
    return sb.toString();
  }


  protected String getRevision() {
    try {
      InputStream is = JPF.class.getResourceAsStream(".version");
      if (is != null){
        int len = is.available();
        byte[] data = new byte[len];
        is.read(data);
        is.close();
        return new String(data).trim();
        
      } else {
        return null;
      }
      
    } catch (Throwable t){
      return null;
    }
  }
  
  protected String getRepositoryInfo() {
    try {
      InputStream is = JPF.class.getResourceAsStream("build.properties");
      if (is != null){
        Properties revInfo = new Properties();
        revInfo.load(is);

        StringBuffer sb = new StringBuffer();
        String date = revInfo.getProperty("date");
        String author = revInfo.getProperty("author");
        String rev = revInfo.getProperty("rev");
        String machine = revInfo.getProperty("hostname");
        String loc = revInfo.getProperty("location");
        String upstream = revInfo.getProperty("upstream");

        return String.format("%s %s %s %s %s", date,author,rev,machine,loc);
      }
    } catch (IOException iox) {
      return null;
    }

    return null;
  }

  
  public String getHostName () {
    try {
      InetAddress in = InetAddress.getLocalHost();
      String hostName = in.getHostName();
      return hostName;
    } catch (Throwable t) {
      return "localhost";
    }
  }

  public String getUser() {
    return System.getProperty("user.name");
  }

  public String getSuT() {
    return vm.getSUTDescription();
  }
  
  public String getJava (){
    String vendor = System.getProperty("java.vendor");
    String version = System.getProperty("java.version");
    return vendor + "/" + version;
  }

  public String getArch () {
    String arch = System.getProperty("os.arch");
    Runtime rt = Runtime.getRuntime();
    String type = arch + "/" + rt.availableProcessors();

    return type;
  }

  public String getOS () {
    String name = System.getProperty("os.name");
    String version = System.getProperty("os.version");
    return name + "/" + version;
  }

}
