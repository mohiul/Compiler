	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 hlt

func	 sw 	 funcj1(r0),r2
	 sw 	 funcj2(r0),r3
	 sw 	 funcj3(r0),r4
	 sw 	 funcj4(r0),r5
	 sw 	 funcj5(r0),r6
	 sw 	 funcj6(r0),r7
	 sw 	 funcj7(r0),r8
	 sw 	 funcj8(r0),r9
	 sw 	 funcj9(r0),r10
	 sw 	 funcj10(r0),r11
	 sw 	 funcj11(r0),r12
	 sw 	 funcj12(r0),r13
	 sw 	 funcj13(r0),r14
x	dw 0
funcres	dw 0
funcj1	dw 0
funcj2	dw 0
funcj3	dw 0
funcj4	dw 0
funcj5	dw 0
funcj6	dw 0
funcj7	dw 0
funcj8	dw 0
funcj9	dw 0
funcj10	dw 0
funcj11	dw 0
funcj12	dw 0
funcj13	dw 0
funcj14	dw 0
