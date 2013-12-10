set term png
set ylabel "Time (s)"
set xlabel "# of Workers"
set xtics font ", 18"
set ytics font ", 18"
set output "time.png"
plot [1:16][0:2400] \
	"time.dat" using 1:2 with linespoints title "A", \
	"time.dat" using 1:3 with linespoints title "B", \
	"time.dat" using 1:4 with linespoints title "C", \
	"time.dat" using 1:5 with linespoints title "D", \
	"time.dat" using 1:6 with linespoints title "E"
set ylabel "Speedup"
set xlabel "# Of Workers"
set output "speedup.png"
plot [1:16][0:5] \
	"speedup.dat" using 1:2 with linespoints title "A", \
	"speedup.dat" using 1:3 with linespoints title "B", \
	"speedup.dat" using 1:4 with linespoints title "C", \
	"speedup.dat" using 1:5 with linespoints title "D", \
	"speedup.dat" using 1:6 with linespoints title "E"

