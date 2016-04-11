	 entry
	 sub 	 r3,r3,r3
	 addi 	 r3,r3,1
	 sw 	 z(r0),r3

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,45
	 add 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 putc 	 r1
	 sub 	 r3,r3,r3
	 addi 	 r3,r3,1
	 lw 	 r5,z(r0)
	 sub 	 r1,r1,r1
	 add 	 r0,r1,r5
	 lw 	 r4,y(r0)
	 sub 	 r1,r1,r1
	 add 	 r0,r1,r5
	 sw 	 y(r0),r3

	 lw 	 r1,y(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,45
	 add 	 r3,r1,r2
	 sw 	 t1(r0),r3

	 lw 	 r1,t1(r0)
	 putc 	 r1
	 hlt

z	dw 0
y	res 1
t0	 dw 	 0
t1	 dw 	 0
