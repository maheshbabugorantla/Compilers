#!/bin/bash

array_val=(1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21)



for val in ${array_val[@]}; do
	file="test"
	input_val=$file$val
	output_val="$input_val.out"
	input_val="$input_val.micro"
	
	./myParser < "testcases/input/$input_val" > output && diff output "testcases/output/$output_val"
	
done

exit 0
