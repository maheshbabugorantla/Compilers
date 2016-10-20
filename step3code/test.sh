#!/bin/bash

#array_val=(1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21)
array_val=(1 5 6 7 8 9 11 13 14 16 18 19 20 21)

for val in ${array_val[@]}; do
	file="test"
	input_val=$file$val
	output_val="$input_val.out"
	input_val="$input_val.micro"
	
	echo $input_val
	
	java -cp lib/antlr.jar:classes/ Micro "./testcases/input/$input_val" > output
	diff output "./testcases/output/$output_val"

	cat output
	
	echo ""
done

rm output

exit 0
