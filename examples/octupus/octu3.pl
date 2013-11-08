#!/usr/bin/perl
#---  octu3.pl  - rdb derived from octu2.pl on 4/27/12
#                 besides preserving existing code to simplify later 
#                 (possibly extensive) comparisons. 
#                 Goal of this version is to get all satisfactory hits
#                 and pick the one with the longest match region -- or 
#                 possibly other criteria.
#
# Way Sung 10/2008
# Edited by Dan Bergeron March/April 2012
#
# rdb: This version is an attempt to reduce the i/o. It maintains all 
#      octu information in hash tables, so it never needs to re-read it; it
#      still needs to append to the octulist file when a new octu is created
#      and re-write it whenever an octu's consensus is changed.
#---------------------------------------------------------------------
#    This file is part of OCTUPUS.

#    OCTUPUS is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.

#    OCTUPUS is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.

#    You should have received a copy of the GNU General Public License
#    along with OCTUPUS.  If not, see <http://www.gnu.org/licenses/>.
#
# History as of 03/29/2012 rdb
#----------------- octu2.pl -----------------------------------------------
# 03/29-4/5/2012 rdb
#    Functional changes
#    1. revised finishing code that used "sed"; it didn't work on Mac OS X
#    2. changed command line parameter conventions and checking
#          -test for non-numeric and invalid % and length
#          -changed default directory to one composed of fasta file name, 
#           % and min length such as: harvardSite_95_100. To use cwd, use "."
#    3. Found bug in test for recomputing consensus; code tested exactMatchCount
#       for the octu (%matchNum) rather than the octu count (%octuCount). This
#       should actually give better results (for more time) since it was
#       essentially nearly always recomputing the consensus.
#       For a 500 sequence test, the matchNum version is about 50% slower than
#       the octuCount version (32 secs vs 22 secs on macbook air). The octu
#       result differences were very small; it looks like all the octus were
#       the same, but 9 of the consensus sequences were slightly different:
#       1 had 18 fewer nucleotides, 1 had 4 fewer, 1 had 7 fewer; the other 
#       3 had 1 less nucleotide and 3 were the same length but with 1 or more
#       different nucleotides. From tath point of view, the decision to 
#       not recompute the consensus too often was a good one.
#       However, there decision to not ever reassign sequences is more 
#       questionable. 
#    4. Changed minimumSimilarity test to be true if similarity equals the
#       minimum. Old code required it to be < minimum.
#
#    Style/organization changes
#
#    3. Edited code that uses $_ to specify explicit variables.
#    4. Used "use English" for other magic perl variables.
#    5. Added subroutines: formatdb, assignToOctu, cleanUp, readSequence
#    6. Renamed checkDistance to makeConsensus
#    7. Changed blast call to return best hit, without regard to similarity.
#       The min similarity test is done on the result.
#
# 04/04/12 rdb
#    Added functionality to test sequence/consensus similarity at end of exec.
#    For the 500 sequence data set, 80 of the sequences are not in the "best"
#    cluster at the end. 
#
# 04/06/12 rdb
#    Started conversion to storing all info in main memory; 
#      -- added sequence hash (%dnaSeq)
#      -- added hash to store list of sequences in each octu (%octuSeqIds)
#      -- added hash to store the octu for each sequence id (%seqOctu)
#      -- added hash to store consensus for each octu (%octuConsensus)
#
# 04/12/12
#    Dumped the 4 new hashes in a log file: octu.log
#    NEED TO: change Blast parameters and parsing; as in blastTest.pl.
#    NEED TO: take advantage of (now) having all info in memory
#
# 04/13/12
#    Almost have the conversion to blastTest.pl version of doBlast working,  
#    but the diversity values are not the same. Need to check the calculations
# 04/16/12
#    Had to recompute diff field to get diversity fields to match; have now
#    finished conversion of doBlast. Still getting inconsistent results with
#    old code.
#
#    NEED TO: Start using hashes instead of reading data again
#    NEED TO: reassign the free sequences
# 04/17/12 
#    Found Way bug that produced inconsistent results; octu.pl and octu2.pl
#    now produce same results, but wayoctu.pl (in spite of "correcting" the
#    bug) still produces different results than octu.pl and octu2.pl. Fixing
#    the bug in wayoctu only made minor differences in div file for small.fna
#
# 04/18/12
#    Now Rewrite octulist file from hash values, rather than re-reading the file
#          there is no improvement in elapsed time -- just about the same.
#          Too bad -- the code is simpler however; about 10 lines rather than 60.
#
# 04/19/12
#    - ran octu2 on small.fna with recalc consensus until 50 in octu => always.
#      Only very minor differences; time went from 20 secs to 35.
#    Try 1c below; delete and reassign all "small" clusters at end.
#    Summary: 
#        recalculating the consensus is a problem; I don't think the consensus
#           generation is consistent with the blast algorithm for similarity.
#           often a 2nd seq is added to an octu; the consensus is recalculated
#           and both sequences are no longer within the tolerance of the new
#           consensus; that's an unstable algorithm.
#        recalculating all the time is a problem; sometimes sequences 
#           suddenly get rejected; e.g. add 16 sequences to an octu 
#           without a problem; the 17th matches 99%, but once the consensus
#           is recomputed for that 17th one, 10 of the existing are now out of
#           the octu. Code also isn't really correct; should delete 1 sequence
#           (the furthest away from the consensus), recalculate the consensus
#           then check the distances of all the remaining members.
#        However, the results seem to be reasonably consistent and stable:
#            n = clusters with fewer than n sequences get deleted and reassigned
#            
#           recalc limit = 10
#                        n  =  0    1     2     3
#           # cl deleted      0     91    122   123 
#           # seq reassign    0     91    153   156
#           # cl at end       155   146   140   140
#           # ambig seq       9     10     3     3
#
#           recalc limit = 50 (essentially after every seq assignment)
#                        n  =  0    1     2     3
#           # cl deleted      0     91    122   123 
#           # seq reassign    0     91    153   156
#           # cl at end       156   148   139   139
#           # ambig seq       9     10     3     3
#
#        The check at the end is pretty consistent
#           all are in spec -- Once in a while there are strange things where
#           a very short match is considered better than a very long one??
#        
#    NEED TO: evaluate consensus generation and its relation to similarity.
#    NEED TO: develop tightness criteria for each cluster and its outliers.
# 04/20/12
#    Added clusterStats. Very few outliers?? Only 2 for 500 sequence test
#            AND one of those was a prefect match with the consensus, but
#            that was an outlier! 
#    NEED TO: recompute consensus at the end; the consensus should
#             be consensus of final set of members, even if don't re-do it
#             every time as we go.
#    NEED TO: blast all consensus sequences against the each other; ask for
#             2 hits returned from blast.
# 
# 04/22/12
#    Revisited tests. Reorganized blast code (again), to make it easier to
#       get 2nd best hit, needed for seeing how consensus sequences compare.
#       Added &findOctu method and changed parameters for doBlast and parseBlastLine.
#    
#    Added compile-time option to compute similarity based on entire query
#      sequence, rather than the portion of the query that matched the subject.
#      For the 500 seq d.s. this added exactly one more cluster -- for the
#      sequence that was matching 100% for 29 nucleotides and was being mis
#      assigned. This distance measure option is set with $newDisctanceCalc.
#      ******** The above does not work very well -- too many octus **********
#      ***********************************************************************
#
#    For the "old" distance calculation, I added a threshold that requires
#      at least x % of the sequence to match where x is defined by the global
#      variable $minMatchLengthPct.
#      For the 500 seq test data set:
#         minMatchLengthPct = 50 -> 140 octus -- same as with no limit
#         minMatchLengthPct = 75 => 148 octus
# 04/23/12
#    Revised handling of testing match % of entire subject, so it is handled
#    at a higher level. Revisions to parseBlastLine and findOctu and checkClusters.
# 04/25/12
#    Added at end to blast consensus sequences against each other and
#    if the consensus of an octu is within tolerance of another octu,
#           the one with fewer sequences is deleted and their seq reassigned
#    THIS HAD ESSENTIALLY NO EFFECT.
# 04/27/12
#    Generated octu3.pl to try experiments for ways of dealing with "ambiguous"
#    clusters.
# 04/29/12
#    Revised code so that only use octuId everywhere instead of mixed
#    octuId and octuId. Have verified that results are the same -- as octu2
#    EXCEPT the order of the reassigning sequences can get different results
#    and the order is different since the hash keys are different. I can get
#    the same results by forcing both versions to process the small octus
#    by their "number" order.
#
# 04/30/12
#    Start change to record all "good" hits with each sequence; this will make
#       it feasible/easy to do sequence based reassignment at the end, rather
#       than the octu-based reassignment that I have been doing.
# 05/03/12
#     Replaced file reading in checkAllClusters with hash data
#     Changed parseBlastLine to only return a hit if it passes the similarity
#          test; much cleaner to do it there -- esp. given the 2 different
#          similarity measures and convenience for trying others in the future.
#     This change exposed bug in 4/29 code.
#     Renamed checkAllClusters to checkSeqAssign
#  05/07/12
#      Verified that all the changes to octu3 are "correct" in that the results
#         now match that of octu2. 
#  05/14/12
#      Started sequence and cluster ambiguity computations. Created a
#          new hash, seqMatchCnt, which is the count of # clusters the seq
#          "could" go in -- Should we put sequences in multiple clusters, 
#          perhaps with a designation of whether it is the best fit or not?
#  05/15/12 - saved version as octu3a.
#      Converted code in checkSeqAssign to use array of arrays for hit
#          data, rather than array of string.
#      Decided to add sequences (at the end) to every cluster they fall in
#         with an ambiguity designation of some kind
#      Seq hashes need list of octus
#      Octu seq list needs annotation to identify secondary hits, or 2 lists
#      Octu stats need 2 versions, 1 for only best hits, 1 for all hits or just
#           secondary hits
#      Each octu should have info about 
#           sole hits - seqs that only map to this octu
#           best - those for which this is the best of multiple
#           good - satisfactory hits, but not best  
#  05/24/12 - fixed a major bug in handling of "good" hits 
#           added output file octu2seq.txt
#  06/01/12 - Add count of all good hits for each sequence to seq2octu.txt 
#            ***********************************************************
#            * PROBLEM (Maybe): checkOctu re-blasts every seq that appears
#            *    in multiple octus multiple times. 
#            *    Should be doing a checkSeq instead, so it only blasts once?
#            *    We'd have to check if a seq was in an octu, but isn't any 
#            *    more so we can remove it and adjust div calculation.
#            ***********************************************************           
#-------------------------------------------------------------------
# rdb questions:
# 1. No cluster is ever deleted; no clusters are ever merged; 
#    a. what if redistribute small clusters at end; if they are truly outliers, 
#       they'll come right back.
#    b. can we find a way to identify "overlapping" clusters, or identify
#       all sequences that could fit into more than 1 cluster.
#    c. what if re-assign all small clusters as a final step; maybe some will
#       disappear.
# 2. Current algorithm doesn't recompute consensus after 10 sequences are in
#    the octu. However, because of a bug in the original code; it actually
#    recomputed after every sequence is added. The current code corrects that
#    so the issue stilll is: a) That is an arbitrary number, and b) it could 
#    instability and order-dependence; can try to fix as we go (how much will 
#    that slow it down?) or perhaps check at the end for anomalies and try 
#    to fix at the end which may be cheaper when no problems occur or 
#    lead to its own instability.
# 3. diversity calculation; why divide by count-1 ??? instead of count? 
#    I presume since consensus is not one of the "real" sequences, but 
# 4. Is there no other way of incrementally updating a blastdb?
# 5. ******************************************************************
#    4/22: one sequence (F0QL1L401D7PW0_444_2) is matching 100% for just 
#          29 nucleotides out of 444; I can't get a 2nd hit even if I ask
#          for it. So, this goes into an octu that it probably shouldn't
#          be in. I think there should be a minimum length for the hit -- 
#          at least in the case where all the sequences are essentially
#          the same sequence from different organisms.
#
#          Do we want the criterion to be % of the sequence that matches
#          the subject or the % of the portion of the matching sequence.
#
#
#------------------------------- usage() ------------------------------
sub usage()
{
   print STDERR q
   ( 
   Usage: octu.pl <fastaFile> <minimumSimilarity> <minSeqLen> [<directory>]

   This program takes in a large set of site specific 454 sequences 
   and creates Operational Clustered Taxonomical Units.

   Parameters
   minimumSimilarity - % seq similarity of clustering (eg: 95 = 95% similarity)
   minSeqLen - minumum fasta sequence length to use for clustering
   directory - directory for output files 
               default fastaFilePrefix_minimumSimilarity_minSeqLen

   Output files
   octuall.seq - the sequences for each octu separated by *octu#
   octulist - the consensus of each octu
   div - the sequence counts and diversity for each octu consensus
   seq2octu.txt - for each sequence, info about the best octu it maps to
   octu2seq.txt - for each octu, info about all sequences that map to it
   check.log - for each sequence all multiple hit info as well as 
               rare cases where no hits or the "wrong" hit is found at the end.
);
   exit;
}
#
use strict;
use warnings;
use Cwd;
use English '-no_match_vars';

#------------- global variables ---------------------------
#---- global compile time parameters
my $newConsensusSizeLimit = 10; # Don't recompute consensus after reach this count
my $smallOctuDefn = 2;    # Octus with this many or fewer sequences are "small"
my $outlierFactor = 3;    # 
my $newDistanceCalc = 0;  # new distance measure is of ALL nucleotides in the
                          #   query sequence that doesn't match the subject
                          # original distance measure used only the difference
                          #   count for the matching portion of the sequences.
my $minMatchLenPct = 50;  # For the "old" distance calculation, consider it
                          #   an inadequate match if at least 50% of the subject
                          #   sequence matches the query sequence
my $minMatchRatio = $minMatchLenPct /100.0; # convenience
my $maxHits = 4;          # max blast hits when percent match is main criterion

#---- debugging switches
my $rmTmp  = 1;    # if !0 delete all *tmp and *temp* files
my $rmDB   = 1;    # if !0 delete the octulist file
my $rmLog  = 0;    # if !0 delete all logs
my $lastFile  = "";
#my $debugSeq = "F0QL1L401D7PW0_444_2";
my $debugSeq = "";

#---- executables 
my $blastEXE = "megablast";
my $formatdbEXE = "formatdb";
my $muscleEXE = "muscle";

#----- variables initialized in setup
my $fastaFile;            # input sequence file
my $minSim;               # min similarity for assigning to existing octu
my $minSeqLen;            # min sequence length for assigning to octu
my $outDir;               # directory for all output files

#-- following file names will be revised in setup() to prepend directory
my $divFile = "div";      
my $blastDB = "octulist"; 
my $tmpOctuFile = "octu1.tmp"; 
my $octuSeqFile = "octuall.seq"; 
my $octuLogFile = "octu.log";
my $blastOut = "blast.tmp";


#----- data hashes and arrays
# Note: the hashes based on the octuId could be arrays, but the performance
#       of hash and array are essentially indistinguishable (for this program)
#       so I've used hashes (and left the others as hashes) since it will make
#       it trivial to switch to using the octuId in the future if that were
#       desirable.     rdb
my %octuConsensus = ();   # octuId -> consensus sequence
my %octuSeqIds = ();      # octuId -> space-separated seqIds
my %exactMatchCount = (); # octuId -> number matches
my %octuDiff = ();        # octuId -> sum of nuc difference counts
my %octuCount = ();       # octuId -> # seqs in octu
my %octuMatchLen = ();    # octuId -> sum of match lengths for seqs in octu
my %octuPctDiff = ();     # octuId -> sum of % differences of seqs in octu
my %octuPctDiff2 = ();    # octuId -> sum of % differences squared of seqs in octu
my %octuLength = ();      # octuId -> length of consensus sequence

my %goodSeqIds = ();      # octuId -> seqIds for good, not best maps
my %goodMatchLen = ();    # octuId -> sum of match lens for "good" seq in octu
my %goodPctDiff = ();     # octuId -> sum of % differences of good seqs in octu
my %goodPctDiff2 = ();    # octuId -> sum of % differences squared of good seqs
my %goodCount = ();       # octuId -> # seqs in goodSeqIds hash entry
my %soleCount = ();       # octuId -> # seqs that map only to this octu
my $bestCount = ();       # octuId -> # seqs for which this octu is best map
                          #             includes soleCount

my %octuNew = ();         # octuId -> # seq added since last consensus update

my %dnaSeq   = ();        # seqId -> nucleotide sequence
my %seqOctu  = ();        # seqId -> octu 
my %seq2ndOctu = ();      # seqId -> space-separated octuIds for secondary membership
my %seqHitCount = ();     # seqId -> # clusters it maps to (in the end)

my %seqData = ();         # seqId -> octuId, diff, matchlen, %match
my %seqDiffPct = ();      # seqId -> %diff in its octu

my @seqHitDistr;          # seqHitDistr[i] is count of seqs that map to i clusters
my @freeSeqs = ();        # sequences that need to be re-assigned to octus 

#----- statistics variables
my $makeConsensusCount = 0;
my $keepConsensusCount = 0;

my $sequenceNum = 0;
my $sequence;
my $lastOctu = 0;

&setup();

print "------ Octupus run ----------------\n";
print "Input file: $fastaFile\n";
print "Output directory: $outDir\n";
print "New consensus size limit: $newConsensusSizeLimit\n";
print "Small octu definition max size: $smallOctuDefn\n";
if ( $newDistanceCalc )
{
   print "Distance calculation (%) based on consensus length (new)\n";
}
else
{
   print "Distance calculation (%) based on match length (old)\n";
   print "Minimum acceptable match length as % of consensus length: $minMatchLenPct\n";
}

#Initialization of list, fasta, destination file
# ------------   1. initial assignment of all sequences ---------------
&initialAssignment();
print  "----After 1st pass: #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# ------------  2. update consensus sequences ----------------------
# Make sure final consensus for all clusters is a consensus of all sequences
my $updateCount = &updateAllConsensus();
print "---- Updated $updateCount consensus sequences of ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# ------------  3. reassign sequences that were removed ----------------------
&reassign();
print "---- After reassignment: #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# --------- 4. delete  "small" clusters and reassign sequences --------------
#
my $nClusters = scalar( %octuConsensus );

print "----purging small clusters -----------------\n";
my ( $nClust, $nSeq ) = &purgeSmallClusters();

print  "---- deleted $nClust clusters with $nSeq sequences\n";
print  "---- #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";
&reassign();

print  "----After reassign: #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# #rdb: don't know why I need to do this:
# #local $INPUT_RECORD_SEPARATOR = "\n"; 
#                           
# --------- 5. purge "ambiguous" clusters --------------------------

 print "----purging ambiguous clusters and reassign -----------------\n";
 
 ( $nClust, $nSeq ) = &purgeAmbiguousClusters();
 print  "---- deleted $nClust clusters with $nSeq sequences\n";
 print  "---- #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# print "----evaluate ambiguous clusters and reassign -----------------\n";
# 
# ( $nClust, $nSeq ) = &evalAmbiguousClusters();
# print  "---- deleted $nClust clusters with $nSeq sequences\n";
# print  "---- #octus = ", scalar( keys %octuLength ),
# 
&reassign();

print  "----After reassign: #octus = ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";
                          
# ----------- 6. Keep reassigning as long as #free goes down -------------
my $free = @freeSeqs + 1;
while ( @freeSeqs > 0 && @freeSeqs < $free )
{
   $free = @freeSeqs;
   print  "Still $free unassigned sequences; trying again\n";
   &reassign();
}

if ( @freeSeqs > 0 )
{
   print  "# free did not get smaller: ", scalar( @freeSeqs ), " I give up!\n";
}

# ------------ 7. A final updating of consensus sequences ------------------
$updateCount = &updateAllConsensus();
print "---- Updated $updateCount consensus sequences of ", scalar( keys %octuLength ),
                          " #unassigned = ", scalar( @freeSeqs ), "\n";

# ------------ 8. Final output -------------------------------------------
&makeOctuallFile();

# check all cluster to see if any sequences in a cluster are beyond the 
# specified distance from the consensus sequence.

&checkSeqAssign();

# generate octu2seq.txt file
&octuSeqSummary();

# print diversity information for each octu to the "div" file
&printDiversity();

&clusterStats();

&dumpHashes();  # debugging options

&cleanup( $outDir );

print  "make/keep consensus counts: $makeConsensusCount $keepConsensusCount\n";
my $nOctus = scalar( keys( %octuConsensus ));
print "----- Final octu count: $nOctus    ---------------------\n";

for ( my $i = 0; $i < @seqHitDistr; $i++ )
{
   if ( !defined( $seqHitDistr[ $i ] ))
   {
      $seqHitDistr[ $i ] = 0;
   }
}
print "sequence hit counts: @seqHitDistr\n";

#----------------------- end main -------------------------------------------

#----------------------------- initialAssignment ----------------------------
#
# Read sequences; make initial clusters
#
sub initialAssignment()
{
   open( my $FASTA, "$fastaFile") || die "can't open $fastaFile";
   
   my ($header, $seq) = &readSequence( $FASTA, 1 );
   my $seqLen = length( $seq );
   
   while ( $header )
   {
      $sequenceNum++;
      print STDERR "$sequenceNum\r";
      my ($seqId) = $header =~ /^(\S+)/;
      $seq =~ s/[\n\r]//g;
   
      if ( length( $seq ) >= $minSeqLen )
      {
         $dnaSeq{ $seqId } = $seq;    # save the sequence
         &assignToOctu( $header, $seq );
      }
   
      ( $header, $seq ) = &readSequence( $FASTA );
   }
   print "\n";   
}
#----------------------------- setup ----------------------------------------
#
# Read command line args
# Open key global file handles
# Initialize key global variables
#
sub setup()
{
   if (( @ARGV < 3 ) || $ARGV[0] eq "-h" )   #no arguments or help option
   {
      &usage();
   }
   
   #Initialization of command line variables
   $fastaFile = $ARGV[0];
   $minSim   = $ARGV[1] + 0.0;
   if ( $minSim <= 0 || $minSim > 100 )
   {
      print STDERR "!Bad minimum similarity: $ARGV[1]. Must be in range (0,100)\n";
      exit(1);
   }
   $minSeqLen = int( $ARGV[2] );
   if ( $minSeqLen <= 0 )
   {
      print STDERR "!Bad minimum sequence length: $ARGV[2]. Must be > 0\n";
      exit(1);
   }
   
   $outDir = $ARGV[3];
   
   #if no input directory use pwd
   if ( !defined( $outDir ) )
   {
      $outDir = $fastaFile;
      $outDir =~ s/\.[^.]*?$//;
      $outDir .= "_$minSim"."_$minSeqLen";
      print "----- Writing to $outDir ---------\n";
   }
   
   #---- prepend output file names with directory spec -----
   $blastDB = "$outDir/$blastDB";
   $divFile = "$outDir/$divFile";
   $tmpOctuFile = "$outDir/$tmpOctuFile"; 
   $octuSeqFile = "$outDir/$octuSeqFile";  
   $octuLogFile = "$outDir/$octuLogFile";
   $blastOut    = "$outDir/$blastOut";
   
   #make directory if it doesn't exist
   if ( !(-d $outDir) )
   {
      mkdir( $outDir );
   }
   else # clean out old files in the directory
   {
      &cleanup( $outDir );
      if ( -e $blastDB )
      {
         my $status = unlink( $blastDB );
         if ( $status != 1 )
         {
            print STDERR "!*** setup: Unable to delete $blastDB\n";
            exit( -1 );
         }
      }
   }   
}

#----------------------------- reassign() ----------------------------
#
# reassign sequences
#
sub reassign()
{
   print "++++Reassigning: ", scalar( @freeSeqs ), "\n";

   my @localFree = @freeSeqs;
   @freeSeqs = ();
   for my $seqName( @localFree )
   {
      #$debugSeq = $seqName;
      #print "$seqName\n";
      my $sequence = $dnaSeq{ $seqName };
      &assignToOctu( $seqName, $sequence );
   }
}

#----------------------------- updateAllConsensus() ----------------------------
#
# make sure all octu's have up-to-date consensus
#
sub updateAllConsensus()
{
   my $count = 0;
   #for my $octuId ( sort {$a<=>$b} keys( %octuCount ) )
   for ( my $id = 1; $id <= $lastOctu; $id++ ) # use this so sorted by octu #
   {
      my $octuId = "octu$id";
      my $newSeqs = $octuNew{ $octuId };
      if ( defined( $newSeqs ) && $newSeqs > 0 )
      {
         #print "Consensus for $octuId\n";
         $count += &makeConsensus( $octuId );
      }
   }
   return $count;
}
#----------------------------- purgeSmallClusters() ------------------------
#
# delete all small clusters -- need to reassign their sequences
# return #clusters deleted and # sequences 
#
sub purgeSmallClusters()
{
   my $purgeOctu = 0;
   my $seqCount = 0;

#    
#    foreach my $octuId ( keys %octuCount )
#    {
#       if ( $count <= $smallOctuDefn )
#       {
#          $purgeOctu++;
#          $seqCount += &deleteOctu( $octuId );
#       }
#    }
# ---- version of code below is used only so we can compare the
#      results of octu3 with octu2; this insures the reassignments are
#      done in the same order in both versions. The slightly more efficient
#      code above could be used once we don't need to compare results.
   for ( my $i = 1; $i < $lastOctu; $i++ )
   {
      my $octuId = "octu$i";
      my $count = $octuCount{ $octuId };
      if ( defined($count) && $count <= $smallOctuDefn )
      {
         $purgeOctu++;
         $seqCount += &deleteOctu( $octuId );
      }
   }
   &makeDB();
   return ( $purgeOctu, $seqCount );
}

#----------------------------- purgeAmbiguousClusters() --------------------
#
# Find any cluster pairs whose consensus sequences are within the tolerance
# Delete the smaller of the pairs.
# Return the number of clusters deleted and the number of sequences in those
#   clusters.
#
sub purgeAmbiguousClusters()
{
#    open( my $FASTA, "$blastDB") || die "cant open $blastDB";
#    my ($header, $seq) = &readSequence( $FASTA, 1 );
#    my $seqLen = length( $seq );
   my @ambiguous;
   
   open( SAVEBLAST, ">blast3.save" ) or die "Can't open blast3.save";

   #for my $octuId ( keys( %octuConsensus ))
   for ( my $i = 1; $i <= $lastOctu; $i++ ) # this processes in number order
   {
      my $octuId = "octu$i";
      my $seq = $octuConsensus{ $octuId };
      my $seqLen = $octuLength{ $octuId };
      
      if ( defined( $seq ))
      {
         my @blastHits = doBlast( $octuId, $seq );  # get all hits < minSim
         if ( @blastHits >= 2 )  # > 2 hits implies ambiguous
         {
               print SAVEBLAST "!----------------- $octuId ------------------\n";
               print SAVEBLAST "@blastHits\n";
               print SAVEBLAST "!--------------------------------------------\n";
   
            my ( $good, $octuId2, $diff, $matchLen, $pctSame )
                 = &parseBlastLine( $octuId, $seqLen, $blastHits[ 1 ] );
            if ( $good )
            {
               push( @ambiguous, "$octuId $octuId2 $pctSame%" );
            }
         }
         else
         {
            #print "No 2nd hit for $octuId; first: $blastHits[0]\n";
         }
      }
   }
   close( SAVEBLAST );
   my $octuCt = 0;
   my $seqCt = 0;
   for my $amb ( @ambiguous )
   {
      my ( $octu1, $octu2, $pctMatch ) = split( ' ', $amb );
      print "ambiguous: $octu1 $octu2 $pctMatch\n";
      my $nSeq = &purgeSmaller( $octu1, $octu2 );
      if ( $nSeq > 0 )
      {
         $seqCt += $nSeq;
         $octuCt++;
      }
   }
   &makeDB();
   return ( $octuCt, $seqCt );   
}
#-------------------------- purgeSmaller( octu1, octu2 ) -----------------
#
# The consensus sequences of the two octus are within tolerance of each other,
# delete the octu that is smaller (if it hasn't already been deleted)
#
sub purgeSmaller( )
{
   my ($octu1, $octu2 ) = @ARG;
   my $size1 = $octuCount{ $octu1 };
   my $size2 = $octuCount{ $octu2 };
   
   my $seqCount = 0;
   if ( defined( $size1 ) && defined( $size2 ))
   {
      if ( $size1 < $size2 )
      {
         print "Deleting $octu1 $size1 vs $size2\n";
         $seqCount = &deleteOctu( $octu1 );
      }
      else
      {
         print "Deleting $octu2 $size2 vs $size1\n";
         $seqCount = &deleteOctu( $octu2 );
      }
   }
}
#---------------------- deleteOctu( octuId ) ---------------------
#
# delete the octu and return the number of sequences added to the the
# free list (to be reassigned).
#
sub deleteOctu()
{
   ( my $octuId ) = @ARG;
   #print "Deleting $octuId\n";
   
   my $seqCount = $octuCount{ $octuId };
   if ( !defined( $seqCount ))
   {
      $seqCount = 0;
   }
   else
   {
      &rmAllSeqFromOctu( $octuId );
      delete( $octuSeqIds{ $octuId } );
      delete( $octuConsensus{ $octuId } );
      delete( $octuNew{ $octuId } );
      delete( $octuCount{ $octuId } );
      delete( $octuLength{ $octuId } );
      delete( $exactMatchCount{ $octuId } );
      delete( $octuMatchLen{ $octuId } );
      delete( $octuDiff{ $octuId } );
      delete( $octuPctDiff{ $octuId } );
      delete( $octuPctDiff2{ $octuId } );
      # and remove the file
      unlink( "$outDir/$octuId.tmp" ); 
   }
   return $seqCount;
}
#---------------------- rmAllSeqFromOctu( octuId ) ---------------------
#
# remove all sequences from the octu, return the # sequences removed
#
sub rmAllSeqFromOctu()
{   
   ( my $octuId ) = @ARG;
   #print "<<<<< rmAllSeqFromOctu: $octuId ", scalar(@freeSeqs), "\n";

   my $seqIds = $octuSeqIds{ $octuId };
   my @seqIdArray = split( ' ', $seqIds );
   my $sCount = @seqIdArray;
   for my $seqId ( @seqIdArray )
   {
      push( @freeSeqs, $seqId );
      #print "++++ reassign: $seqId\n";
      delete( $seqData{ $seqId } );
      delete( $seqDiffPct{ $seqId } );
   } 
   #print ">>>>> rmAllSeqFromOctu: $octuId ", scalar(@freeSeqs), "\n";
   
   return $sCount;
}
#---------------------- rmSeqFromOctu( octuId, seqId ) ---------------------
#
# remove the sequence from the octu it is in, update all stats hashes
#
sub rmSeqFromOctu()
{
   my ( $octuId, $seqId, $oldConsensus ) = @ARG;
   
   # update the octu stats info to reflect loss of this sequence
   my $oldData = $seqData{ $seqId };
   my $oldSeqPctDiff = $seqDiffPct{ $seqId };
   my ( $oldId, $oldDiff, $oldLen, $oldPctSame ) = split( ' ', $oldData );
   if ( $oldId ne $octuId )
   {
      print STDERR "!**** Code error in rmSeqFromOctu old,cur octu for ".
                    " $seqId: data says $oldId; but found in $octuId\n";
      $oldId = $octuId;
   }
   my $octuSeqIds = $octuSeqIds{ $octuId };
   
   if ( $octuSeqIds =~ s/$seqId// )
   {
      $octuSeqIds{ $octuId } = $octuSeqIds;
   } 
   else
   {
      print STDERR "!**** rmSeqFromOctu error: $seqId not in seq list for $octuId\n";
      print STDERR "!Seq list: |$octuSeqIds|\n";
   }
   if ( $octuCount{ $octuId } <= 1 )
   {
      &deleteOctu( $octuId );
   }
   else
   {
      $octuMatchLen{ $octuId } -= $oldLen;
      $octuDiff{ $octuId } -= $oldDiff;
      $octuPctDiff{ $octuId } -= $oldSeqPctDiff;
      $octuPctDiff2{ $octuId } -= $oldSeqPctDiff * $oldSeqPctDiff;
      $octuCount{ $octuId }--;
      if ( $dnaSeq{ $seqId } eq $oldConsensus )
      {
         $exactMatchCount{ $octuId }--;
      }
   }
   delete( $seqData{ $seqId } );
   delete( $seqDiffPct{ $seqId } );
}
#----------------------------- makeOctuallFile ----------------------------
#
# re-create the octuall.seq file containing all current octu consensus sequences
#
sub makeOctuallFile()
{
   #concatenate individual octu seq files into one file, octuall.seq
   open( OCTUALL, ">$octuSeqFile" ) || die "can't open octuSeqFile";
   
   for ( my $i = 1; $i <= $lastOctu; $i++ ) # this approach -> numerical order
   {
      my $octuId = "octu$i";
      my $oCount = $octuCount{ $octuId };
      if ( defined( $oCount ) && $oCount > 0 )
      {
         print OCTUALL "*$octuId\n";
         my @seqIds = split( ' ', $octuSeqIds{ $octuId } );
         for my $seqId ( @seqIds )
         {
            # Revised output: add extra info on header; seq printed 60 nucs/line
            print OCTUALL ">$seqId $seqData{ $seqId }\n";
            my @lines = unpack( "(A60)*", $dnaSeq{ $seqId } );
            my $dnaByLine = join( "\n", @lines );
            print OCTUALL "$dnaByLine\n";
            
            #compatibility test version (no extra header data, 1 line for seq):
            #print OCTUALL ">$seqId\n";
            #print OCTUALL "$dnaSeq{ $seqId }\n";
         }
      }
   }  
   close OCTUALL;
}

#--------------------------------- dumpHashes --------------------------------
# dump the hashes: octuSeqIds, seqOctu, 
#
sub dumpHashes()
{
   open( LOG, ">$octuLogFile" ) or die "can't create octu.log";
   print LOG "++++++++++++++++++++ sequenceId to octuIdber ++++++++++++++++\n";
   for my $seqId ( sort keys( %seqOctu ) )
   {
      my $octu = $seqOctu{ $seqId };
      print LOG ">$seqId $octu\n";
   }
   print LOG "++++++++++++++++++++ octuIdber to seqIds ++++++++++++++++\n";
   #for my $octuId ( sort {$a<=>$b} keys( %octuSeqIds ) )
   for ( my $id = 1; $id <= $lastOctu; $id++ ) # use this so sorted by octu #
   {
      my $octuId = "octu$id";
      my $seqIdList = $octuSeqIds{ $octuId };
      if ( defined( $seqIdList ))
      {
         print LOG "=$octuId $seqIdList\n";
      }
   }
   close( LOG );
}
#--------------------------------- cleanup --------------------------------
# delete temporary files
#
sub cleanup()
{
   ( my $outDir ) = @ARG;
   
   my $filenames = "";
   if ( $rmTmp )
   {
      $filenames .= "$outDir/*tmp $outDir/*temp* ";
   }
   if ( $rmLog )
   {
      $filenames .= "$outDir/*log ";
   }
   if ( $rmDB )
   {
      $filenames .= "$blastDB.* ";
   }

   my @filesToDelete = glob( $filenames );
   if ( @filesToDelete > 0 )
   {
      my $status = unlink( @filesToDelete );
      my $deletedFiles = join( ", ", @filesToDelete );
      #print STDERR "!Files deleted: $deletedFiles", "\n";
   }
}
#---------------------- clusterStats --------------------------------
#
# Generate statistics about each cluster
#     Want a clean distance measure
#           I think it should be sequence length independent so it makes
#           sense to compute the mean and stddev.
#           It should also be low when close and high when far away.
#           % difference should satisfy that; 
#           However, we might want to consider something like bit score or 
#           e-value instead. e-value works somewhat better since it is small
#           when close, but it is also dependent on db, which isn't good.
#       For now use %diff
#     nucleotide diversity, mean and std deviation of distance, and similarity
#
#  This computes std dev based on assumption that it is a finite population with
#     equal probability of all points (from wikipedia):
#    stddev = sqroot( 1/N * sum( xi*xi ) - mean*mean )
sub clusterStats()
{
   open( STATS, ">$outDir/octuStats.csv" ) or die "Can't open $outDir/octuStats.csv";
   print STATS "octu,#,mean,stddev,min,max,#outliers\n";
   #for my $octuId ( sort {$a<=>$b} keys( %octuCount ))
   for ( my $id = 1; $id <= $lastOctu; $id++ ) # use this so sorted by octu #
   {
      my $octuId = "octu$id";
      my $n = $octuCount{ $octuId }; 
      if ( defined( $n ))
      {
         my $meanDist = $octuPctDiff{ $octuId } / $n;
         my $stddev = sqrt( $octuPctDiff2{ $octuId }/$n - $meanDist * $meanDist );
         my $outTolerance = $outlierFactor * $stddev;
         my @outliers;
         my @seqIds = split( ' ', $octuSeqIds{ $octuId } );
         my $min = 100;
         my $max = 0;
         for my $seqId ( @seqIds )
         {
            $seqId =~ s/^[*+\-]//;
            my $dist = $seqDiffPct{ $seqId };
            if ( !defined( $dist ))
            {
               print STDERR "$seqId $octuId\n";
            }
            if ( abs( $meanDist - $dist ) > $outTolerance )
            {
               push( @outliers, "$seqId,$dist" );
            }
            if ( $dist > $max )
            {
               $max = $dist;
            }
            if ( $dist < $min )
            {
               $min = $max;
            }
         }
         my $outCount = @outliers;
         print STATS "$octuId,$n,$meanDist,$stddev,$min,$max,$outCount";
         for my $out( @outliers )
         {
            print STATS ",$out";
         }
         print STATS "\n";
      }      
   }
}

#---------------------- printDiversity --------------------------------
#
# Print Octu information as a double diversity table; each row is of form:
#  octuId  
sub printDiversity()
{
   open( DIV, ">$divFile" ) || die "printDiversity can't open $divFile";
   #print pairwise nucleotide diversity estimate
   print DIV "octu\tconsLen\t#bestSeqs\tnucDiv\t#goodSeqs\tnucDiv\n";
   
   for ( my $u=1; $u < ($lastOctu+1); $u++ ) # use this approach so sorted by #
   {
      my $octuId  = "octu$u";
      my $count = $octuCount{ $octuId };
      my $len   = $octuLength{ $octuId };
      if ( defined( $count ) && defined( $len ))
      {
         #print DIV join ("\t", "$octuId", $count, $len );
         #print DIV join ("\t", "$u", $len , $count);
         printf DIV "%8s\t%6d\t%6d", $u, $len, $count;
         
         if ( $count == 1 )
         {
            #print DIV "\t0\t";
            printf DIV "\t%8d", 0;
         }
         else
         {
            # rdb????: why is percent divisor count-1 instead count ?????
            # From wikipedia, nuc div defn, d:
            # d = sum i[1:n] ( sum j[1:n] ( xi * xj * dij )) 
            #   = 2 * sum i[1:n] ( sum j[1:i-1] ( xi * xj * dij ))
            # where xk = freq of sequence k 
            #       dij = # differences per nuc site between xi and xj
            #       n is number of sequences in the sample
            #
            # This code seems to only compare all octu sequences to the
            # octu consensus and all octu sequences are consider to have
            # a frequency of 1/(n-1), but why not 1/n
            # I don't see that the consensus is included in the n sequences;
            # if it were, that would be a reason for n-1.
            
            # octuPctDiff = sum of percentages of the diff/numMatch for each
            # sequence in the octu
            my $pi = (($octuPctDiff{ $octuId }) / ($count - 1 ));
            printf DIV "\t%8.3f", $pi;
         }
         my $goodCount = $goodCount{ $octuId };
         if ( defined( $goodCount ))
         {
            my $totCount = $count + $goodCount;
            my $sumPctDiff = $octuPctDiff{ $octuId } + $goodPctDiff{ $octuId };
            my $piAll = $sumPctDiff / ( $totCount -1 );
            printf DIV "\t%6d\t%8.3f", $totCount, $piAll;
            #print DIV "\t$octuPctDiff{ $octuId }\t$goodPctDiff{ $octuId }";
         }
         print DIV "\n";
      }
   }
   close( DIV );
}

#--------------------------------- assignToOctu --------------------------------
# assignToOctu( header, sequence )
#
# blast this sequence against the current set of consensus sequences
#  and add it to the octu with the best consensus hit
#
sub assignToOctu()
{
   my ( $hdr, $sequence ) = @ARG;
   
   ( my $seqId ) = split( ' ', $hdr, 1 ); 
   my $seqLen = length( $sequence );
   
   
   #---- for first sequence, don't need to blast; 
   if ( $lastOctu == 0 ) 
   {
      &newOctu( $seqId, $sequence );
   }
   else # do the blast
   { 
      my ( $good, $bestOctu, $diff, $matchLen, $pctSame )      
                    = &findOctu( $hdr, $sequence );
      if ( !$good )
      {
         &newOctu( $seqId, $sequence );
      }
      else
      {
         &addToOctu( $seqId, $sequence, $bestOctu, $diff, $matchLen, $pctSame );
      }
   }
   #print "$octuId: $octuCount{ $octuId }  $octuDiff{ $octuId }\n";
}
#-------------------- addToOctu ------------------------------------
#
# Try to add the sequence to the best matched octu
#
# parms: $seqId, $sequence, @matchArgs
# @matchArgs: octuId, matchLen, matchDiff, %IdentMatch, allDiff, %IdentAll
#
sub addToOctu() # ( $seqId, $sequence, @matchArgs )
{
   #print STDERR "!-->@ARG\n";    
   my ( $seqId, $sequence, $octuId, $nDiff, $matchLen, $pctSame ) = @ARG;
   
   my $seqLen = length( $sequence );
   my $seqPctDiff = 100.0 - $pctSame;

   $octuMatchLen{ $octuId } += $matchLen;
   $octuDiff{ $octuId } += $nDiff;
   $octuPctDiff{ $octuId } += $seqPctDiff;
   $octuPctDiff2{ $octuId } += $seqPctDiff * $seqPctDiff;
   $octuCount{ $octuId }++;
   $octuNew{ $octuId }++;
   
   my $octuTmp = "$outDir/$octuId.tmp";
   open( TEMPOCTU, ">>$octuTmp" ) || die "assignToOctu cant open $octuTmp";
   print TEMPOCTU ">", $seqId, "\n", $sequence, "\n";
   close( TEMPOCTU );
   
#    my $newPercentSame = 100.0 - $seqPctDiff;
#    if ( abs( $newPercentSame - $pctSame ) > 1 )
#    {
#       print STDERR "!++++++++++ $newPercentSame != $pctSame %same bad "
#          . " for $octuId and $seqId\n";
#    }
   $seqData{ $seqId } = "$octuId $nDiff $matchLen $pctSame"; #rdb
   $seqDiffPct{ $seqId } = $seqPctDiff; 

   $octuSeqIds{ $octuId } .= " $seqId";
   $seqOctu{ $seqId } = $octuId;
   
   #rdb!!! my $nMatches = $exactMatchCount{ $octuId };
   my $nMatches = $octuCount{ $octuId };
   if ( $nMatches < $newConsensusSizeLimit ) 
   {
      $makeConsensusCount += &makeConsensus( $octuId );
   }
   else
   {
      $keepConsensusCount++;
   }
   if ( $seqId eq $debugSeq )
   {
      print STDERR "!DEBUG: $debugSeq: $octuId $nDiff $matchLen $pctSame\n";
   }
}
#-------------------- newOctu ------------------------------------
#
# Add a new octu with the sequence as the first consensus
#
sub newOctu() # ( $seqId, $sequence )
{
   my ( $seqId, $sequence ) = @ARG;
   $lastOctu++;
   
   my $octuId = "octu$lastOctu";
   my $nDiff = 0;
   my $matchLen = length( $sequence );
   my $seqLen = $matchLen;
   my $pctSame = 100;
   
   #print "---- making $octuId\n";
   $octuCount{ $octuId } = 1;
   $octuDiff{ $octuId } = $nDiff;  # 0
   $octuMatchLen{ $octuId } = $matchLen;  # same as $seqLen
   $exactMatchCount{ $octuId } = 0;  # rdb
   $octuPctDiff{ $octuId } = 0;  
   $octuPctDiff2{ $octuId } = 0;  
   $octuNew{ $octuId } = 0;  
   $octuSeqIds{ $octuId } = "$seqId";
   $seqOctu{ $seqId } = $octuId;
   $octuConsensus{ $octuId } = $dnaSeq{ $seqId };
   $octuLength{ $octuId } = $seqLen;

   open( OCTULIST, ">>$blastDB") || die "assignToOctu can't open $blastDB";
   print OCTULIST ">$octuId $octuCount{$octuId}\n$sequence\n";
   close( OCTULIST );
   &formatdb( "$blastDB" );

   my $tmpFile = "$outDir/$octuId.tmp";
   open( TEMPOCTU, ">$tmpFile" ) || die "assignToOctu can't open $tmpFile";
   print TEMPOCTU ">", $seqId, "\n", $sequence, "\n";
   close( TEMPOCTU );
   
   $seqData{ $seqId } = "$octuId $nDiff $matchLen $pctSame"; 
   $seqDiffPct{ $seqId } = 0;
}
#-------------------- findOctu -----------------------------
#
# Do the blast for the sequence
# Parse the best hit and return ( octuId, nDiff, matchLen, %matchSame, %allSame )
#
sub findOctu() # ( header, bases )
{
   my ( $header, $bases ) = @ARG;
   my $seqLen = length( $bases );
   my $hits = 1;
   if ( $header eq $debugSeq )
   {
      $hits = 2;
   }
   
   my @blastHits = doBlast( $header, $bases, $hits );  # get best hit
   if ( $header eq $debugSeq )
   {
      print "------ hits for $header ----------\n@blastHits\n++++++++++++++++\n";
   }
   if ( @blastHits == 0 )  # if no hits there isn't even a "good" hit
   {
      return ();
   }
   else
   {
      return &parseBlastLine( $header, $seqLen, $blastHits[ 0 ] );
   }
}
#-------------------- doBlast -----------------------------
#
# setup and do the blast, return array of strings; 1 string per hit
#
sub doBlast() # ( header, bases, nHits )
{
   my ( $header, $bases, $nHits ) = @ARG;
   my $blastOpts = "-d $blastDB " # target db
                  . "-o $blastOut "  # output file
                  . "-D 2 "           # traditional output, default -m 0 pairwise
                  . "-a 2 "           # use 2 processors if available
                  . "-F F "           # turn off filter query sequence
                  . "-m 8 " 
                  #-------- options below may be useful to change
                  # The b and v switches will be set to 1 or 2, depending on
                  #   whether want best hit (most of the time) or 2nd best
                  #   (when we are testing the consensus sequences
                  #. "-b 1 "    # limit number alignments shown
                  #. "-v 1 "     # limit 1-line descr. 
                                # b and v options not independent b>=v
                                # behavior of v is not predictable
                  #. "-b $nHits -v $nHits"
                  # It might be useful to explicitly restrict hits that don't
                  #   pass the min similarity, but its useful to know what did
                  #   hit.
                  #. "-p $minSim "   # % identity cutoff
                  ;

   if ( defined( $nHits ))
   {
      $blastOpts .= " -b $nHits -v $nHits";
   }
   else
   {
      $blastOpts .= "-p $minSim -b $maxHits -v $maxHits";
   }
   my $seqFile = "temp.seq";
      
   open( TEMPDEST, ">$seqFile" ) || die "can't open $seqFile";
   print TEMPDEST ">", $header, "\n", $bases;
   close( TEMPDEST );
   
   my $status = system "$blastEXE -i $seqFile $blastOpts";

   if ( $status != 0 )
   { 
      print STDERR "!Blast failed on sequence $header with code: $status\n";
      exit (-1);
   }
   open ( BLAST, $blastOut ) or die "Failed to open blast.tmp";
   my @all = <BLAST>;
   close( BLAST );
   
   return @all;
}
#------------------- sortByMatchLen( @all ) ----------------------
#
# Sort the blast hits (that all "pass" the condition test) by the length
#    of the match. 
#
sub sortByMatchLen() # ( @allHits )
{
   my ( $header, @allHits ) = @ARG;
   my %hitsHash = ();  # matchLen -> ; separated list of blast line for that len
   #print "Multiple hits for $header: ", scalar( @allHits ), "\n";
   for my $hit( @allHits )
   {   
      my @fields = split( '\t', $hit );
      # fields: 
      #   0  1     2      3         4       5     6    7    8    9   10    11
      # qId sId %ident matchLen #mismatch #gaps qBeg qEnd sBeg sEnd eVal bitScore
      my $hitData = $hitsHash{ $fields[ 3 ] };
      my $soctuId = $fields[ 1 ];
      my $matchAllRatio = $fields[ 3 ] / $octuLength{ $soctuId };
      
      if ( $matchAllRatio >= $minMatchRatio )
      {
         if ( defined( $hitData ))
         {
            $hitsHash{ $fields[ 3 ] } = "$hitData;$hit";
         }
         else
         {
            $hitsHash{ $fields[ 3 ] } = "$hit";
         }
      }
   }
   @allHits = ();
   # Now extract all the hits in order of matchLen into single array
   for my $hitLen ( sort { $b<=>$a } keys( %hitsHash ))
   {
      push( @allHits, split( ";", $hitsHash{ $hitLen } ))
   }
   #print STDERR "!blast hits\n@allHits\n";
   return @allHits;
}
#------------------- parseBlastLine --------------------------------
#
# This version assumes the -m 8 format for the -D 2 output option of blast
# This is one tab-separated line for each hit with following fields:
# qid sid %ident alignLen mismatch gaps qStart qEnd sStart sEnd eval bitScore
#
# It returns an array with the following entries:
#     ( $octuId, $nDiff, $matchLen, $pctMatchSame, $pctAllSame )
# But only if the blast hit satisfies the current similarity constraints
#
sub parseBlastLine() # ( $header, $qLen, $blastLine )
{
   my ( $header, $qLen, $blastHit ) = @ARG;
   #
   #parse blast output to get desired information
   #         
   my @fields = split( '\t', $blastHit );
   
   # fields: 
   #   0  1     2      3         4       5     6    7    8    9   10    11
   # qId sId %ident matchLen #mismatch #gaps qBeg qEnd sBeg sEnd eVal bitScore
   my ( $qid, $octuId, $pctMatchSame, $matchLen, $matchDiff, $gaps,
        $qBeg, $qEnd, $sBeg, $sEnd, $eVal, $bitScore )     = @fields;
        
   my $sLen = $octuLength{ $octuId };
   
   if ( !defined( $sLen  ) )
   {
      print STDERR "!Undefined length: $octuId for $header\n";
      if ( $octuConsensus{ $octuId } )
      {
         $sLen = length( $octuConsensus{ $octuId } );
         $octuLength{ $octuId } = $sLen;
      }
      else
      {
         print STDERR "!      also no consensus defined!!!\n";
         $sLen = 500;   # ?????
      }
   }
   
   my ( $close, $diff, $pctSame ) = &distance( $pctMatchSame, $matchLen, $sLen );
   return ( $close, $octuId, $diff, $matchLen, $pctSame );
}
#------------------------- distance -------------------------------------
#
# returns ( $close, $diff, $pctSame ) where
#         $close is 0, if match is NOT within required tolerance, else 1
#         $diff is the number of nucleotide differences between the query
#               and either the "match" region or the entire subject 
#               (depending on which calculation is being used).
#         $pctSame is the % of matching nucleotides in the query compared to
#               either the "match" region or the entire subject
#
sub distance()
{
   my( $pctMatchSame, $matchLen, $subjLen ) = @ARG;
   
   #---- rdb: the diff value returned by -m 8 option does not seem to be
   #          the same as for the default option (old code). Here
   #          I recompute the diff value based on percentId and length.
   #          This matches the previous Way
   my $diff = int( 0.5 + (100.0 - $pctMatchSame )/100.0 * $matchLen );
   
   my $nMatches = int( 0.5 + $pctMatchSame / 100.0 * $matchLen );
   my $pctMatchAll = 100.0 * $nMatches / $subjLen;

   my $pctSame = $pctMatchSame;  # default is original distance

   if ( $newDistanceCalc )
   {
      $pctSame = $pctMatchAll;
      $diff = $subjLen - $nMatches;
   }
   my $close = 1;
   if ( $pctSame < $minSim 
       ||  ( !$newDistanceCalc && $matchLen < $subjLen * $minMatchRatio ))
   {
      $close = 0;
   }
   return ( $close, $diff, $pctSame );
}
   
#------------------------------ checkSeqAssign --------------------------
# Check and report an sequences that are not within the desired similarity
#   of the cluster consensus. 
# Write inconsistencies to check.log 
# Compute and output secondary membership information
#
sub checkSeqAssign()
{
   print  "------------- checkSeqAssign ------------------\n";
   my $seq2octu = "$outDir/seq2octu.txt";
   open( my $SEQ, ">$seq2octu" ) or die "checkClusters can't open $seq2octu";
   print $SEQ "seq\tseqLen\toctu\t#diff\tmatchLen\t%same\n";
   
   my $chklog = "$outDir/check.log";
   open( my $CHKOUT, ">$chklog" ) or die "checkClusters can't open $chklog";
   print $CHKOUT "code\tseq\tseqLen\toctu\t#diff\tmatchLen\t%same\t" 
                . "octu\t#diff\tmatchLen\t%same\n";
                
   # foreach octu<n>.tmp
   #    foreach sequence 
   #       find best octu
   #       if it's not same octu
   #           report
   
   my $checkCount = 0;
   # for my $seqId ( keys( $octuSeqIds ))
   for ( my $i = 1; $i <= $lastOctu; $i++ ) # use this to get order by octu #
   {
      my $thisOctuId = "octu$i";
      if ( defined( $octuLength{ $thisOctuId } ))
      {
         $checkCount += &checkOctu( $CHKOUT, $SEQ, $thisOctuId );
      }
   }
   close( $CHKOUT );
   close( $SEQ );
   print  STDERR "$checkCount sequences have problems\n";
}
#------------------------- checkOctu -------------------------------------
#
# Check the sequences in this octu to see if they have multiple valid hits
#
sub checkOctu() # ( $octuId )
{
   my ( $CHKOUT, $SEQ, $thisOctuId ) = @ARG;
   my $checkCount = 0;
   my $allBestSeqIds = $octuSeqIds{ $thisOctuId };
   my @seqIds = split( ' ', $allBestSeqIds );
   
   # make sure that the count of sequence ids matches the count we gathered
   if ( @seqIds != $octuCount{ $thisOctuId } )
   {
      my $sn = @seqIds;
      my $sn2 = $octuCount{ $thisOctuId };
      print STDERR "***** Seq/count $thisOctuId: $sn $sn2\n";
   }
   $goodPctDiff{ $thisOctuId } = 0;
   $goodPctDiff2{ $thisOctuId } = 0;
   $goodMatchLen{ $thisOctuId } = 0;
   my $soleMapSeqIds = "";
   my $bestNotOnlySeqIds = "";
   my $goodSeqs = "";
   
   for my $seqId( @seqIds )
   {
      my $octuId;
      my $nDiff;
      my $matchLen;
      my $pctSame;
      
      my $hitString = "";
      my $bases = $dnaSeq{ $seqId };
      my $seqLen = length( $bases );
      my $mapping = &getMappedDataString( $seqId );

      my @blastData = &doBlast( $seqId, $bases );
      
      my @goodHits = (); # array of "good" hit data arrays
      
      # If haven't already counted # seq hits to octus, do it now.
      if ( !defined( $seqHitCount{ $seqId } ))
      {
         $seqHitCount{ $seqId } = scalar( @blastData );
      }
      for my $blastHit (@blastData )
      {
         my ( $good, @hitInfo ) # hitInfo = [octuId, diff, len, pctSame]
                     = &parseBlastLine( $seqId, $seqLen, $blastHit );
         if ( $good )  # valid hit
         {
            #push( @goodHits, join( " ", @hitInfo ));
            push( @goodHits, [ @hitInfo ]); # goodHits is array of arrays
         }
      }                       
      if ( !defined( $seqHitDistr[ @goodHits ] ))
      {
         $seqHitDistr[ @goodHits ] = 0;
      }
      $seqHitDistr[ @goodHits ]++;

      if ( @goodHits < 1 )
      {
         &logProblem( $CHKOUT, "NoHit", $seqId, $mapping );
         $checkCount++;
      }
      else 
      {
         my $bestHit = $goodHits[ 0 ];
         $hitString = join( " ", @$bestHit ) . "\t";
         ( $octuId, $nDiff, $matchLen, $pctSame ) = @$bestHit;
         if ( $octuId ne $thisOctuId )
         {
            &logProblem( $CHKOUT, "WrongHit $octuId vs $thisOctuId", $seqId, $hitString );
            #
            #??????????? Should be reassigned, but that could get complicated
            #  in terms of both code and correctness of stats
            # However, maybe have a reassign that does not try to update
            #   consensus seq and maybe not even stats???
            #    ??? &reassignSeq( $thisOctuId, $seqId, $goodHits[0] );
            #????????????
            
            $checkCount++;
         }
         if ( @goodHits == 1 ) # if get error above, shouldn't just continue!
         {
            $soleMapSeqIds .= "*$seqId ";
         }
         else  # multiple hits
         {
            $bestNotOnlySeqIds .= "+$seqId ";
            shift( @goodHits );  # remove best hit; it's already 
            $hitString .= &checkGoodHits( $thisOctuId, $seqId, @goodHits );
            &logProblem( $CHKOUT, "MultiHit", $seqId, $hitString );
         }
      }
      #no hits printf $SEQ ( "%s\t%5d\t%s\n", $seqId, $seqLen, $mapping );
      printf $SEQ ( "%s\t%4d\t%5d\t%s\n", $seqId, $seqHitCount{ $seqId },
                                     $seqLen, $mapping );
   }
   # Now all best and only seqs for thisOctuId are in $soleMapSeqIds
   #  rebuild %octuSeqIds entry with these, plus the bestNotOnly (+) and
   #  the good (-)
   $octuSeqIds{ $thisOctuId }  = $soleMapSeqIds;
   my $numSole = $soleMapSeqIds =~ tr/ / /;  #count sole
   $soleCount{ $thisOctuId }   = $numSole;
   
   my $numBestNotOnly = $bestNotOnlySeqIds =~ tr/ / /;  #count best not only
   my $numBest = $octuCount{ $thisOctuId };
   if ( $numBest != $numSole + $numBestNotOnly )
   {
      print STDERR "***** Bad best counts $thisOctuId: $numBest $numSole $numBestNotOnly\n";
   }
   $octuSeqIds{ $thisOctuId } .= $bestNotOnlySeqIds; 

   return $checkCount;
}
#-------------------- checkGoodHits ------------------------------------------
#
#
#
sub checkGoodHits() # ( $thisOctuId, $seqId, @otherHits )
{
   my ( $thisOctuId, $seqId, @otherHits ) = @ARG;
   my $hitString = "";
   #print STDERR "checkGoodHits: $seqId\n"; 
   $seqId =~ s/^[+*\-](\S+)/$1/;
   for my $goodHit ( @otherHits )
   {
      my ( $octuId, $nDiff, $matchLen, $pctSame ) = @$goodHit; 
      my $seqPctDiff = 100.0 - $pctSame;
      my $goodSeqs = $goodSeqIds{ $octuId };
      if ( !defined( $goodSeqs ))
      {
         $goodPctDiff{ $octuId } = $seqPctDiff;
         $goodPctDiff2{ $octuId } = $seqPctDiff * $seqPctDiff;
         $goodMatchLen{ $octuId } = $matchLen;
         $goodSeqIds{ $octuId } = "-$seqId ";
         $goodCount{ $octuId } = 1;
      }
      elsif ( $octuId eq $thisOctuId ) # already have a hit for this seq and octu
      {
         print STDERR "**** Got 2nd octu hit for seq: $octuId $seqId\n";
      }
      elsif ( index( $goodSeqs, $seqId ) >= 0 )
      {
         print STDERR "***** Got $seqId again as good hit to $octuId\n";
      }
      else
      {
         $goodPctDiff{ $octuId } += $seqPctDiff;
         $goodPctDiff2{ $octuId } += $seqPctDiff * $seqPctDiff;
         $goodMatchLen{ $octuId } += $matchLen;
         $goodSeqIds{ $octuId } .= "-$seqId ";
         $goodCount{ $octuId }++;
      }
      
      $hitString .= join( " ", @$goodHit ) . "\t";
      # Gather multihit stats for this octu
   }
   return $hitString;
}
#-------------------- octuSeqSummary -----------------------------------------
#
# Generate the octu2seq.txt file that summarizes the octu to sequence info 
#
sub octuSeqSummary() # 
{
   my $octu2seq = "$outDir/octu2seq.txt";
   open( OCTU, ">$octu2seq" ) or die "checkClusters can't open $octu2seq";
   print OCTU "octu\tconsLen\t#best\t#sole\t#good\tseqList\n";
   for ( my $i = 1; $i <= $lastOctu; $i++ ) # use this to get order by octu #
   {
      my $octuId = "octu$i";
      my $seqs = $octuSeqIds{ $octuId };
      if ( defined( $seqs ))
      {
         my $numGood = $goodCount{ $octuId };
         my $goodSeqs = "";
         if ( !defined( $numGood ))
         {
            $numGood = 0;
         }
         else
         {
            $goodSeqs = $goodSeqIds{ $octuId };
         }
         print OCTU "$octuId" .
                    "\t$octuLength{$octuId}" .
                    "\t$octuCount{$octuId}" .
                    "\t$soleCount{$octuId}" .
                    "\t$numGood";
         my $allSeqs = $seqs . $goodSeqs;
         $allSeqs =~ tr/ /\t/;    # change blanks to tabs
         print OCTU "\t$allSeqs\n";
      }
   }
   close OCTU;   
}
#-------------------- reassignSeq -----------------------------------------
#
# A sequence was assigned as best map to an octu, but now maps better to another
#   update all stat info
#
sub reassignSeq() # ( $oldOctuId, $seqId, $newOctuId, $diff, $matchLen, $pctSame )
{
}
#-------------------- getMappedData ---------------------------------------
#
# Return an array with the data describing the mapping
#   of a sequence to an octu:  octuId, #nucsDifferent, matchLength, %same,%diff
#
sub getMappedData() # ( $seqId )
{
   my ( $seqId ) = @ARG;
   my $mappedDataString = getMappedDataString( $seqId );
   my @mappedData = split( '\t',  $mappedDataString );
   return @mappedData;
}
#-------------------- getMappedDataString ---------------------------------------
#
# Return a tab-delimited string with data describing the mapping
#   of a sequence to an octu:  
#                octuId, #nucsDifferent, matchLength, %same, %diff
#
sub getMappedDataString() # ( $seqId )
{
   my ( $seqId ) = @ARG;
   my $mappedDataString = $seqData{ $seqId };
   if ( !defined( $mappedDataString ) )
   {
      print "********* No data for $seqId:\n";
      $mappedDataString = "? 0 0 0";
   }
   $mappedDataString =~ s/ /\t/g;
   return $mappedDataString;
}
#---------------------- formatOctuHit ---------------------------------
#
# Uses sprintf to format octu mapping data.
# returns a string with following fields:
#      octuId nDiff matchLen %same %diff
#
sub formatOctuHit() # ( octuId nDiff matchLen %same %diff )
{
   return join( "\t", @ARG ) . "\t";
#    my $string = sprintf( "%s\t%5d\t%5d\t%6.2f\t", @ARG );
#    return $string;
}
#-------------------- logProblem ------------------------------------------
#
# Write information to check.log for a sequence with problems.
#
sub logProblem( ) # ( $FILE, $code, $seqId, $hitString )
{
   my( $CHKOUT, $code, $seqId, $hitString ) = @ARG;
   print $CHKOUT "$code\t$seqId\t$hitString\n";   
}
#-------------------- purgeCluster ----------------------------------------
#
# For each sequence assigned to a cluster, test to see if that sequence
# still belongs in the cluster. If it doesn't remove it from the cluster
# and add it to the list of unassigned sequences.
#
# Questions: 
# 1. Blast all at once? Would be most efficient! But existing code can be
#    reused if do just one at a time -- I'll do that first.
# 2. Should it blast only against the consensus or against all octu
#    consensus sequences?
#
sub purgeCluster() #( octuId )
{
   my ( $octuId, $oldConsensus ) = @ARG;
   
   my $deleteCount = 0;
   my $octuSeqIds = $octuSeqIds{ $octuId };
   my @newOctuSeqIds = ();
   my @seqIds = split( ' ', $octuSeqIds );
   
   #print "purgeCluster $octuId\n";
   
   # check each seq id in the octu   
   for my $seqId( @seqIds )
   {
      my $sequence = $dnaSeq{ $seqId };
      my ( $good, $bestOctu, $diff, $matchLen, $pctSame ) 
                           = &findOctu( $seqId, $sequence );
      if ( !$good || $bestOctu ne $octuId ) # seq must be reassigned
      {
         print "purge: $seqId: $octuId -> $bestOctu w/ $pctSame ", scalar( @freeSeqs), " \n";
         push( @freeSeqs, $seqId );
         $deleteCount++;

         #***********************
         # In principle, could test if seq satisfies sim test in bestOctu
         #   and just add it in now; 
         # But that could lead to recursion issues as this add changes the
         #   consensus of the bestOctu, which triggers a delete from that
         #   octu, etc.
         # It seems safest and easiest to just remove the sequence from the
         #   octu, gather up all the removed sequences and then add them
         #   back in to their best spot on the next cycle.
         #**********************
         &rmSeqFromOctu( $octuId, $seqId, $oldConsensus );
      }    
   }
   if ( $deleteCount > 0 )
   {
      my $count = $octuCount{ $octuId };
      if ( !defined( $count ))
      {
         $count = 0;
      }
      print  "$deleteCount seqs purged from $octuId; now has $count\n";
      if ( $count <= 0 )
      {
         &deleteOctu( $octuId );
      }
      else
      {
         &writeOctuFile( $octuId );
      }
      &makeDB();
   }
}
#----------------- writeOctuFile( $octuId, @octuSeqIds ) ----------------------
#
# 
#
sub writeOctuFile()
{
   my ( $octuId ) = @ARG;
   my $octuTmp = "$outDir/$octuId.tmp";
   open( TEMPOCTU, ">$octuTmp" ) || die "writeOctuFile cant open $octuTmp";
   my @seqIds = split( " ", $octuSeqIds{ $octuId } );
   for my $seqId( @seqIds )
   {
      print TEMPOCTU ">$seqId\n$dnaSeq{ $seqId }\n";
   }
   close( TEMPOCTU );
}
#--------------------------------- makeConsensus --------------------------
#make muscle alignment, create consensus sequence
#   return 1 if consensus sequence changes
#          0 if consensus remains unchanged
#
sub makeConsensus() # ( octuId )
{
   ( my $octuId ) = @ARG;
   my $oldConsensus = $octuConsensus{ $octuId };  # save the old consensus
   
   if ( !defined( $oldConsensus ))
   {
      print "makeConsensus: no oldConsensus for $octuId\n";
      return;
   }
   
   my $octuFile = "$outDir/$octuId.tmp";
   my $muscleOut = "$outDir/temp.aln";
   my $muscleArgs = "-in $octuFile -out $muscleOut -quiet -maxiters 1 -diags"; 
   
   my $status = system "$muscleEXE $muscleArgs >& muscle.log";
   if ( $status != 0 )
   {
      print STDERR "!ERROR: muscle for $octuFile returned non 0 code: $status\n";
   }
   
   #make consensus sequence
   local $INPUT_RECORD_SEPARATOR = ">"; # $/ = ">";
   my $u=0;
   open (MUSCLE, "$muscleOut") || die "cant open $muscleOut";
   my %hash = ();
   my $maxLength;
   while ( my $line = <MUSCLE> )
   {
      chomp( $line );
      my ( $muscleHeader, $muscleSeq ) = split( /\n/, $line, 2 );
      next unless ( $muscleHeader && $muscleSeq );
      $muscleSeq =~ s/[\n\r]//g;
      $hash{$u} = $muscleSeq;
      $maxLength = length( $hash{ $u } );
      $u++;
   }
   my $consSeq = "";
   for ( my $x=0; $x<$maxLength; $x++ )
   {
      my $indelCount = 0;
      my $aCount = 0;
      my $cCount = 0;
      my $gCount = 0;
      my $tCount = 0;
      foreach my $element ( keys %hash )
      {
         my $nuc = substr( $hash{$element}, $x, 1 );
         if ( $nuc eq "-"){
            $indelCount++;
         }
         if ( $nuc eq "A"){
            $aCount++;
         }
         if ( $nuc eq "C"){
            $cCount++;
         }
         if ( $nuc eq "G"){
            $gCount++;
         }
         if ( $nuc eq "T"){
            $tCount++;
         }
      }
      if (    ($indelCount > $aCount) and ($indelCount > $cCount) 
          and ($indelCount > $gCount) and ($indelCount > $tCount))
      {
         next;
      }
      if (($aCount > $cCount) and ($aCount > $gCount) and ($aCount > $tCount))
      {
         $consSeq .= "A";
      }
      if (($cCount > $aCount) and ($cCount > $gCount) and ($cCount > $tCount))
      {
         $consSeq .= "C";
      }
      if (($gCount > $aCount) and ($gCount > $cCount) and ($gCount > $tCount))
      {
         $consSeq .= "G";
      }
      if (($tCount > $aCount) and ($tCount > $cCount) and ($tCount > $gCount))
      {
         $consSeq .= "T";
      }
   }
   close( MUSCLE );

   $octuNew{ $octuId } = 0;  
   if ( $consSeq eq $oldConsensus )
   {
      return 0;
   }
   else
   {
      $octuConsensus{ $octuId } = $consSeq; 
      $octuLength{ $octuId } = length( $consSeq );
      &makeDB( );  #rdb was: &updateDB( $octuId, $consSeq );
      &purgeCluster( $octuId, $oldConsensus );  # remove sequences that don't belong
      return 1;
   }  
}

#------------------ makeDB() -----------------------
#
# rewrite the octulist file and rebuild the blast db
#
sub makeDB()
{
   open ( OCTUFILE, ">$blastDB" ) || die "can't open $blastDB";
   #for my $octuId ( sort {$a<=>$b} keys( %octuConsensus ) )
   for ( my $id = 1; $id <= $lastOctu; $id++ ) # use this so sorted by octu num
   {
      my $octuId = "octu$id";
      my $consSeq = $octuConsensus{ $octuId };
      if ( defined( $consSeq ) && length( $consSeq ) > 0 )
      {
         print OCTUFILE ">$octuId $octuCount{$octuId}\n"
                        ."$consSeq\n";
      }
   }
   close OCTUFILE;
   &formatdb( $blastDB );
}


#------------------- format ----------------------------------------------
# format (reformat) the blast db because we've added another consensus seq
# This is primarily a subroutine so can experiment with the flushing or closing
# of the octulist file -- and add some performance timing to find out how much
# of the time is spent doing this.
#
sub formatdb()
{
   my ( $outFile ) = @ARG;   
   system "$formatdbEXE -o T -p F -i $outFile";
}

#--------------- readSequence -------------------------------
# ( $header, $sequence ) = readSequence( $FASTA_file_handle [, firstRead] );
#
# This subroutine reads a sequence from the fasta file passed as
# the argument. It extracts the header line and the sequence data
# into separate variables and returns them.
#
sub readSequence()
{
   my ( $FASTA, $firstRead ) = @ARG; 
   
   if ( defined( $firstRead ) && $firstRead == 1 )
   {
      my $firstChar = getc( $FASTA );    # read 1st char of file, must be '>'
      if ( !defined( $firstChar ))
      {
         print STDERR "!Nothing to read in |$lastFile|\n";
         return ( $firstChar, $firstChar );  # act as if eof
      }
      if ( $firstChar ne ">" ) 
      {
         print STDERR "!*************************************************\n";
         print STDERR "!Input file does not start with '>'; not a Fasta file\n";
         local $INPUT_RECORD_SEPARATOR = "\n"; 
         my $line = <$FASTA>;
         print STDERR "!Line: $line\n";
         print STDERR "!*************************************************\n";
         exit;
      }
   }
   # For reading the Fasta data, we'll use ">" as line terminator.
   #   This actually sets all end of line notions in Perl to be >, so need 
   #   to be careful.
   #local $/ = ">";                            # changes "end of line" to be '>'
   #local $RS = ">";                           # changes "end of line" to be '>'      
   local $INPUT_RECORD_SEPARATOR = ">";        # changes "end of line" to be '>'
   
   my $header;         # store the sequence header line
   my $nucs;
   
   my $seq = <$FASTA>;
   if ( $seq )
   {
      chomp( $seq );            # takes off ">" of next sequence

      # 1. pull out the sequence header line
      ( $header, $nucs ) = $seq =~ /^(.*?)\n([\s\S]*)/;

      # 2. delete cr and lf from the sequence -- all of them
      $nucs =~ s/[\n\r]//g;
   }
   return ( $header, $nucs ); 
}