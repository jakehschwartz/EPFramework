Examples
========

EPFramework comes with several programs to show how EPFramework works on several
different kinds of problems. These examples come with their own saved
configuration files and each use a different version of the merge. The Ka/Ks
Calculator takes one input file and the EPFramework will do the spliting and
merging itself. MORE HERE.

Ka/Ks Calculator
----------------
Download URL: https://code.google.com/p/kaks-calculator/

UCLUST
------
Download URL: http://www.drive5.com/usearch/download.html

Octupus
-------
Does not need to be downloaded.

README FROM FOLDER:
03/23/2012 

I gathered this data from Binbin's 2010 project. It was not very clear (at
all) from her documentation what is what. She replicated all data files for
each separate program (C, not perl) she wrote, so it was very confusing 
what was what.

It looks like 1-12octuall.seq might have been the original Octupus output
when it runs all at once and octuall.seq (I changed the name from
"out") is the results from running in parts in parallel and then recombining.

The 2 data files were clearly run with different parameters, however. 
1-12octuall.seq has 129 octus; octuall.seq has 618; they both have
6,044 sequences.

BTW. Binbin's code is useless; she wrote it in C-like C++ and very hard-coded
to each input file. 

Notes
-----
Octupus produces 3 files:

.seq is almost a fasta file of the original input sequences except:
     1. the sequences are grouped by octu and group is preceded by a line
        identifying th octu:
        *octu1
     2. the sequence names seem to have a suffix added of form _397_12;
        I expect this was done prior to octupusl, but I don't know

.div contains statistic info for each octu; this could be appended to the 
     *octu1 line of the .seq file

octlist contains the consensus sequence for each octu; it is a true fasta file

I can regenerate input (minus sequences thrown out), by deleting the *octu
lines from the seq file -- although they really should be randomly
reorganized.
