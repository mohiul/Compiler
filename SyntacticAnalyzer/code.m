	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,2
	 sw 	 y(r0),r1

	 lw 	 r1,x(r0)
	 not 	 r3,r1
	 bz 	 r3,zero0
	 addi 	 r1,r0,1
	 sw 	 t0(r0),r1
	 j 	 endand0
zero0	 sw 	 t0(r0), r0
endand0
	 lw 	 r1,t0(r0)
	 sw 	 x(r0),r1

	 hlt

x	dw 0
y	dw 0
t0	 dw 	 0
