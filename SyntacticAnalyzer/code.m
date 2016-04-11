	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 getc 	 r1
	 sw 	 x(r0),r1
	 lw 	 r1,x(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 putc 	 r1
	 hlt

x	dw 0
t0	 dw 	 0
