	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,2
	 sw 	 y(r0),r1

	 lw 	 r1,x(r0)
	 lw 	 r2,y(r0)
	 cgt 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 bz 	 r1, else0

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,3
	 sw 	 x(r0),r1

	 lw 	 r1,x(r0)
	 lw 	 r2,y(r0)
	 clt 	 r3,r1,r2
	 sw 	 t1(r0),r3

	 lw 	 r1,t1(r0)
	 bz 	 r1, else1

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 j 	 endif1
else1 	 
endif1 	 
	 j 	 endif0
else0 	 
endif0 	 
	 hlt

x	dw 0
y	dw 0
t0	 dw 	 0
t1	 dw 	 0
