	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,0
	 sw 	 i(r0),r1

gofor0 	 
	 lw 	 r1,i(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,3
	 clt 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 bz 	 r1, endfor0
	 j 	 forstat0
goforIncr0 	 
	 lw 	 r1,i(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t1(r0),r3

	 lw 	 r1,t1(r0)
	 sw 	 i(r0),r1

	 j 	 gofor0
forstat0 	 
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,0
	 sw 	 j(r0),r1

gofor1 	 
	 lw 	 r1,j(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,3
	 clt 	 r3,r1,r2
	 sw 	 t2(r0),r3

	 lw 	 r1,t2(r0)
	 bz 	 r1, endfor1
	 j 	 forstat1
goforIncr1 	 
	 lw 	 r1,i(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t3(r0),r3

	 lw 	 r1,t3(r0)
	 sw 	 j(r0),r1

	 j 	 gofor1
forstat1 	 
	 lw 	 r1,i(r0)
	 sw 	 x(r0),r1

	 j 	 goforIncr1
endfor1 	 
	 j 	 goforIncr0
endfor0 	 
	 hlt

x	dw 0
i	dw 0
t0	 dw 	 0
t1	 dw 	 0
j	dw 0
t2	 dw 	 0
t3	 dw 	 0
