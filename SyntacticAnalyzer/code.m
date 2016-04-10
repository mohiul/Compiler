	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,2
	 sw 	 y(r0),r1

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,2
	 div 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,y(r0)
	 lw 	 r2,t0(r0)
	 mul 	 r3,r1,r2
	 sw 	 t1(r0),r3

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,4
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,6
	 mul 	 r3,r1,r2
	 sw 	 t2(r0),r3

	 lw 	 r1,t1(r0)
	 lw 	 r2,t2(r0)
	 sub 	 r3,r1,r2
	 sw 	 t3(r0),r3

	 lw 	 r1,x(r0)
	 lw 	 r2,t3(r0)
	 add 	 r3,r1,r2
	 sw 	 t4(r0),r3

	 lw 	 r1,t4(r0)
	 sw 	 x(r0),r1

	 hlt

x	dw 0
y	dw 0
t0	 dw 	 0
t1	 dw 	 0
t2	 dw 	 0
t3	 dw 	 0
t4	 dw 	 0
