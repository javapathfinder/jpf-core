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

package gov.nasa.jpf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * simple generic structure to hold repository info for source files
 * 
 * <2do> extend this to find out what the status of the repository is, i.e. if
 * there are any local modifications, update revision etc.
 */
public class RepositoryEntry {
  
  static HashMap<String,RepositoryEntry> dict = new HashMap<String,RepositoryEntry>();
  
  String fileName;
  String repositoryType;
  String repository;
  String revision;

  static RepositoryEntryFactory searchers[] = {
    new SvnRepositoryEntryFactory(),
    new HgRepositoryEntryFactory(),
    new GitRepositoryEntryFactory()
  };

  public static RepositoryEntry getRepositoryEntry (String fullFileName) {
    RepositoryEntry e = dict.get(fullFileName);
    
    if (e == null) {
      for (RepositoryEntryFactory factory : searchers) {
        if ((e = factory.getRepositoryEntry(fullFileName)) != null)
          break;
      }
    }
    
    dict.put(fullFileName, e); // no need to look this up more than once
    return e;
  }
  
  public RepositoryEntry (String fileName, String repositoryType, String repository, String revision) {
    this.fileName = fileName;
    this.repositoryType = repositoryType;
    this.repository = repository;
    this.revision = revision;
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public String getRepositoryType() {
    return repositoryType;
  }
  
  public String getRepository() {
    return repository;
  }
  
  public String getRevision() {
    return revision;
  }

}

interface RepositoryEntryFactory {
  RepositoryEntry getRepositoryEntry(String fullFileName);
}

class SvnRepositoryEntryFactory implements RepositoryEntryFactory {

  /*
   * <2do> doesn't work on Windows, where the .svn/entries is apparently
   * not stored as an XML file
   */
  @Override
  public RepositoryEntry getRepositoryEntry(String fullFileName) {
    File f = new File(fullFileName);
    String fname = f.getName();
    String dName = f.getParent();
    
    File fEntries = new File(dName + File.separatorChar + ".svn" + File.separatorChar + "entries");
    if (fEntries.exists()) {
      String repository = "?";
      String revision = "?";
      
      Pattern pName = Pattern.compile(" *name=\"([a-zA-Z0-9.]+)\"");
      Pattern pRep = Pattern.compile(" *url=\"([a-zA-Z0-9.:/\\-]+)\"");
      Pattern pRev = Pattern.compile(" *committed-rev=\"([0-9]+)\"");
      try {
        BufferedReader r = new BufferedReader(new FileReader(fEntries));
        for (String line=r.readLine(); line != null; line = r.readLine()) {
          Matcher mRep = pRep.matcher(line);
          if (mRep.matches()) {
            repository = mRep.group(1);
          } else {
            Matcher mRev = pRev.matcher(line);
            if (mRev.matches()) {
              revision = mRev.group(1);
            } else {
              Matcher mName = pName.matcher(line);
              if (mName.matches() && mName.group(1).equals(fname)) {
                // Ok, got everything
                return new RepositoryEntry(fname, "svn", repository, revision);
              }
            }
          }
        }
      } catch (Throwable t) {}
    }
    
    return null;
  }
}

class HgRepositoryEntryFactory implements RepositoryEntryFactory {

  @Override
  public RepositoryEntry getRepositoryEntry(String fullFileName) {
    File file = new File(fullFileName);

    if (!file.exists())
      return null;

    File currentDir = file.getParentFile();

    searchForHg:
    while (currentDir != null) {
      for (String childName : currentDir.list())
        if (childName.equals(".hg"))
          break searchForHg;

      currentDir = currentDir.getParentFile();
    }

    if (currentDir != null) {
      try {
        File hgrcFile = new File(currentDir, ".hg/hgrc");
        
        String repoURL = "";
        BufferedReader r = new BufferedReader(new FileReader(hgrcFile));
        for (String line=r.readLine(); line != null; line = r.readLine()) {
          String keyVal[] = line.split("=");
          if (keyVal[0].trim().equals("default")) {
            repoURL = keyVal[1].trim();
            break;
          }

        }

        File branchHeads = new File(currentDir, ".hg/branchheads.cache");
        r = new BufferedReader(new FileReader(branchHeads));
        String revision = r.readLine().split(" ")[1];

        return new RepositoryEntry(fullFileName, "hg", repoURL, revision);
      }
      catch (Exception ex) {
        return null;
      }
    }

    return null;
  }

}

class GitRepositoryEntryFactory implements RepositoryEntryFactory {

  @Override
  public RepositoryEntry getRepositoryEntry(String fullFileName) {
    File file = new File(fullFileName);

    if (!file.exists())
      return null;

    File currentDir = file.getParentFile();

    searchForHg:
    while (currentDir != null) {
      for (String childName : currentDir.list()) {
        if (childName.equals(".git")) {
          break searchForHg;
        }
      }

      currentDir = currentDir.getParentFile();
    }

    if (currentDir != null) {
      try {
        File hgrcFile = new File(currentDir, ".git/config");

        String repoURL = "";
        BufferedReader r = new BufferedReader(new FileReader(hgrcFile));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
          String keyVal[] = line.split("=");
          if (keyVal[0].trim().equals("url")) {
            repoURL = keyVal[1].trim();
            break;
          }

        }

        // git doesn't has revision numbers so we will read last revision's hash instead
        File gitHeadHash = new File(currentDir, ".git/refs/heads/master");
        r = new BufferedReader(new FileReader(gitHeadHash));
        String revision = r.readLine();

        return new RepositoryEntry(fullFileName, "git", repoURL, revision);

      } catch (Exception ex) {
        return null;
      }
    }

    return null;
  }
}
