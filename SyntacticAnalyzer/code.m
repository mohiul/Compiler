	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 hlt

func	 sw 	 funci(r0),r2
	 sw 	 funcj(r0),r3
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 funcres(r0),r1
	 jr 	 r15
x	dw 0
funcres	dw 0
funci	dw 0
funcj	dw 0
