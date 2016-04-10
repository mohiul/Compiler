	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,2
	 sw 	 y(r0),r1
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 lw 	 r2,y(r0)
	 add 	 r3,r1,r2
	 sw 	 t0(r0),r3
	 lw 	 r1,t0(r0)
	 sw 	 x(r0),r1
	 hlt

x	dw 0
y	dw 0
t0	 dw 	 0
