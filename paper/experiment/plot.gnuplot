set term png
set ylabel "Time (ms)"
set xlabel "# of threads"
set xtics font ", 18"
set ytics font ", 18"
set output "out.png"
plot [1:16][0:2500000] \
	"kaks.dat" using 1:2 with linespoints title "A", \
	"kaks.dat" using 1:3 with linespoints title "B", \
	"kaks.dat" using 1:4 with linespoints title "C", \
	"kaks.dat" using 1:5 with linespoints title "D", \
	"kaks.dat" using 1:6 with linespoints title "E", \
	"kaks.dat" using 1:7 with linespoints title "F", \
	"kaks.dat" using 1:8 with linespoints title "G", \
	"kaks.dat" using 1:9 with linespoints title "H", \
	"kaks.dat" using 1:10 with linespoints title "I", \
	"kaks.dat" using 1:11 with linespoints title "J"
