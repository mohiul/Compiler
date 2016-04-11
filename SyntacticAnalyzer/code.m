	 entry
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 x(r0),r1

	 lw 	 r2,x(r0)
	 jl 	 r15,func
	 sw 	 t0(r0),r1
	 lw 	 r1,t0(r0)
	 sw 	 x(r0),r1

	 hlt

func	 sw 	 funcy(r0),r2
	 sub 	 r1,r1,r1
	 addi 	 r1,r1,1
	 sw 	 funcres(r0),r1
	 jr 	 r15
x	dw 0
t0	 dw 	 0
funcres	dw 0
funcy	dw 0
