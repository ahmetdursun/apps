#! /usr/bin/perl

@serializations = ( "none", "ibis", "sun" );
@ibises = ( "tcp", "net.bytes.gen.tcp_blk", "panda","net.gm" );

$header = 1;
while ( <> ) {
	if ( $header ) {
		$line = $_;
		$header = 0;
		next;
	}
	# printf "header $line current $_\n";
	( $ibis, $ser ) = ($line =~ /ibis = (\S+) ser = (\S+) count = [0-9]+ size = [0-9]+ app-flags =.*/);
	( $calls, $lat ) = ($_ =~ /Latency: ([0-9]+) calls took [0-9.]+ s, time\/call = ([0-9.]+) us/);
	# printf "ibis $ibis; ser $ser; calls $calls; lat $lat\n";
	$header = 1;

	$ix = $ibis . "/" . $ser;

	$average{ $ix } += $lat;
	$n { $ix } ++;
}

printf "Ibis\t Ser\t average(us)\n";
foreach ( @serializations ) {
	$ser = $_;
	foreach ( @ibises ) {
		$ibis = $_;
		$ix = $ibis . "/" . $ser;
		if ( $n { $ix } > 0) {
			printf "$ibis\t $ser\t ";
			printf $average{ $ix } / $n { $ix };
			printf "\n";
		}
	}
}

# foreach ( keys %average ) {
# 	print "$_ : ";
# 	printf "Average ($n{ $_ }) = ";
# 	printf $average{ $_ } / $n { $_ };
# 	printf " us\n";
# }