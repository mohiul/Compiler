	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 lw 	 r1,x(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,45
	 add 	 r3,r1,r2
	 sw 	 t0(r0),r3

	 lw 	 r1,t0(r0)
	 putc 	 r1
	 lw 	 r2,x(r0)
	 jl 	 r15,func
	 sw 	 t1(r0),r1
	 lw 	 r1,t1(r0)
	 sw 	 y(r0),r1

	 lw 	 r1,y(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,45
	 add 	 r3,r1,r2
	 sw 	 t2(r0),r3

	 lw 	 r1,t2(r0)
	 putc 	 r1
	 hlt

func	 sw 	 funca(r0),r2
	 lw 	 r1,funca(r0)
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 add 	 r3,r1,r2
	 sw 	 t3(r0),r3

	 lw 	 r1,t3(r0)
	 sw 	 funca(r0),r1

	 lw 	 r1,funca(r0)
	 sw 	 funcres(r0),r1
	 jr 	 r15
x	dw 0
y	dw 0
t0	 dw 	 0
t1	 dw 	 0
t2	 dw 	 0
funcres	dw 0
funca	dw 0
t3	 dw 	 0
