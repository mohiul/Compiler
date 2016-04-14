	 entry
	 sub 	 r3,r3,r3
	 addi 	 r3,r3,0
	 sw 	 t(r0),r3

gofor2 	 
	 lw 	 r1,t(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,100
	 cle 	 r3,r1,r2
	 sw 	 t6(r0),r3

	 lw 	 r1,t6(r0)
	 bz 	 r1, endfor2
	 j 	 forstat2
goforIncr2 	 
	 lw 	 r1,t(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t7(r0),r3

	 lw 	 r3,t7(r0)
	 sw 	 t(r0),r3

	 j 	 gofor2
forstat2 	 
	 getc 	 r1
	 sw 	 sample(r0),r1

	 jl 	 r15,randomize
	 sw 	 t8(r0),r1
	 lw 	 r3,t8(r0)
	 sw 	 f(r0),r3

	 j 	 goforIncr2
endfor2 	 
	 lw 	 r2,sample(r0)
	 jl 	 r15,findMax
	 sw 	 t9(r0),r1
	 lw 	 r3,t9(r0)
	 sw 	 maxValue(r0),r3

	 lw 	 r2,sample(r0)
	 jl 	 r15,findMin
	 sw 	 t10(r0),r1
	 lw 	 r3,t10(r0)
	 sw 	 minValue(r0),r3

	 sub 	 r3,r3,r3
	 addi 	 r3,r3,10
	 sw 	 utility(r0),r3

	 sub 	 r3,r3,r3
	 addi 	 r3,r3,2
	 sub 	 r1,r1,r1
	 addi 	 r0,r1,1
	 sw 	 arrayUtility(r0),r3

	 lw 	 r1,maxValue(r0)
	 putc 	 r1
	 lw 	 r1,minValue(r0)
	 putc 	 r1
	 hlt

findMax	 sw 	 findMaxarray(r0),r2
	 sub 	 r1,r1,r1
	 addi 	 r0,r1,100
	 lw 	 r0,findMaxarray(r0)
	 sw 	 findMaxmaxValue(r0),r0

	 sub 	 r3,r3,r3
	 addi 	 r3,r3,99
	 sw 	 idx1(r0),r3

gofor0 	 
	 lw 	 r1,findMaxidx(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,0
	 cgt 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 bz 	 r1, endfor0
	 j 	 forstat0
goforIncr0 	 
	 lw 	 r1,findMaxidx(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 sub 	 r3,r1,r2
	 sw 	 t1(r0),r3

	 lw 	 r3,t1(r0)
	 sw 	 findMaxidx(r0),r3

	 j 	 gofor0
forstat0 	 
	 lw 	 r1,findMaxarray(r0)
	 lw 	 r2,findMaxmaxValue(r0)
	 cgt 	 r3,r1,r2
	 sw 	 t2(r0),r3

	 lw 	 r1,t2(r0)
	 bz 	 r1, else0

	 lw 	 r4,findMaxidx(r0)
	 sub 	 r1,r1,r1
	 add 	 r0,r1,r4
	 lw 	 r3,findMaxarray(r0)
	 sw 	 findMaxmaxValue(r0),r4

	 j 	 endif0
else0 	 
endif0 	 
	 j 	 goforIncr0
endfor0 	 
	 lw 	 r1,findMaxmaxValue(r0)
	 sw 	 findMaxres(r0),r1
	 jr 	 r15
findMin	 sw 	 findMinarray(r0),r2
	 sub 	 r1,r1,r1
	 addi 	 r0,r1,100
	 lw 	 r4,findMinarray(r0)
	 sw 	 findMinminValue(r0),r4

	 sub 	 r3,r3,r3
	 addi 	 r3,r3,1
	 sw 	 idx1(r0),r3

gofor1 	 
	 lw 	 r1,findMinidx(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,99
	 cle 	 r3,r1,r2
	 sw 	 t3(r0),r3

	 lw 	 r1,t3(r0)
	 bz 	 r1, endfor1
	 j 	 forstat1
goforIncr1 	 
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t4(r0),r3

	 lw 	 r3,t4(r0)
	 sw 	 findMinidx(r0),r3

	 j 	 gofor1
forstat1 	 
	 lw 	 r1,findMinarray(r0)
	 lw 	 r2,findMinmaxValue(r0)
	 clt 	 r3,r1,r2
	 sw 	 t5(r0),r3

	 lw 	 r1,t5(r0)
	 bz 	 r1, else1

	 lw 	 r4,findMinidx(r0)
	 sub 	 r1,r1,r1
	 add 	 r0,r1,r4
	 lw 	 r3,findMinarray(r0)
	 sw 	 findMinmaxValue(r0),r4

	 j 	 endif1
else1 	 
endif1 	 
	 j 	 goforIncr1
endfor1 	 
	 lw 	 r1,findMinminValue(r0)
	 sw 	 findMinres(r0),r1
	 jr 	 r15
randomize	 div 	 r3,r1,r2
	 sw 	 t11(r0),r3

	 lw 	 r2,t11(r0)
	 add 	 r3,r1,r2
	 sw 	 t11(r0),r3

	 mul 	 r3,r1,r2
	 sw 	 t12(r0),r3

	 lw 	 r3,t12(r0)
	 sw 	 randomizevalue(r0),r3

	 mul 	 r3,r1,r2
	 sw 	 t13(r0),r3

	 sub 	 r3,r1,r2
	 sw 	 t14(r0),r3

	 add 	 r3,r1,r2
	 sw 	 t15(r0),r3

	 lw 	 r2,t15(r0)
	 add 	 r3,r1,r2
	 sw 	 t15(r0),r3


	 lw 	 r1,randomizevalue(r0)
	 sw 	 randomizeres(r0),r1
	 jr 	 r15
findMaxres	dw 0
findMaxmaxValue	dw 0
findMaxidx	dw 0
idx1	dw 0
t0	 dw 	 0
t1	 dw 	 0
t2	 dw 	 0
findMinres	dw 0
findMinminValue	dw 0
findMinmaxValue	dw 0
findMinidx	dw 0
idx1	dw 0
t3	 dw 	 0
t4	 dw 	 0
t5	 dw 	 0
idx	dw 0
maxValue	dw 0
minValue	dw 0
t	dw 0
t6	 dw 	 0
t7	 dw 	 0
t8	 dw 	 0
t9	 dw 	 0
t10	 dw 	 0
t11	 dw 	 0
t12	 dw 	 0
t13	 dw 	 0
t14	 dw 	 0
t15	 dw 	 0
findMaxarray	res 100
findMinarray	res 100
sample	res 100
f	res 2
utility	res 5
arrayUtility	res 1260
randomizeres	res 2
randomizevalue	res 2
