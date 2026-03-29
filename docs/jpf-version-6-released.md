# Version 6 Released 
I have just pushed a new major jpf-core release to the babelfish repo - v5 is dead, long live v6. Welcome to a new internal design - and a whole new set of bugs: I expect this will take some time to consolidate.

For the most part, this should be transparent if you didn't directly work with state management (Restorer and Serializer), or directly used the DynamicArea instead of the Heap interface. This was the major objective: make object allocation time linear in terms of heap size. Unfortunately, the DynamicArea was pretty much hardwired into the CollapsingRestorer and FilteringSerializer, so both state-storage/restore and state-matching infrastructure had to be rewritten too. Long story short (the long story will go to the developers section of the wiki once I have recovered), the problem is solved: allocation and garbage collection are much faster, and overall memory consumption is much lower, esp. in the gc and serializing peaks.

Two side effects you might want to employ in your JPF applications: we now keep track of which threads access an object, and at least with the SparseClusterArrayHeap you can also see which thread allocated the object (reference values are thread based). You will also see a different search graph, since the old recursive and very expensive prospective reachability analysis has been replaced with the thread access bitmap. As a downside Thread.start() now always has to be a transition break if you want to detect data races (don't fiddle with cg.threads.break_start unless you really have to). I'm sure this topic will be subject to more wiki postings.

Since there already were so many changes, I did throw in two things that might require some work in your projects. JPF now has a full type system for Fields and FieldInfos. We now store arrays as the target types, not unconditionally as int[] arrays. This not only makes conversions (e.g. from listeners) and arraycopy much faster, but can esp. save some memory if your SUT is heavy on Strings or byte[] arrays. I expect less problems from that, but more from the FieldInfo type hierarchy (which resembles the host VM). JPF is not cavalier about storing int values into non int fields anymore - it checks the types on both field/array getters and setters. Can be a little annoying if you were used to Q&D, but the added type safety is worth it.

I'm sure I forgot a ton of changes, but we do them one at a time. Let the mayhem begin.

Posted: 2010-11-30 09:51
Author: pmehlitz@NDC.NASA.GOV
Categories: jpf-core enhancement
