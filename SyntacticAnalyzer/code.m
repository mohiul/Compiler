	 entry
	 sub 	 r2,r2,r2
	 addi 	 r2,r2,1
	 sub 	 r3,r3,r3
	 addi 	 r3,r3,2
	 sub 	 r4,r4,r4
	 addi 	 r4,r4,3
	 jl 	 r15,func
	 sw 	 t0(r0),r1
	 lw 	 r0,t0(r0)
	 sw 	 i(r0),r0

	 hlt

func	 sw 	 funci(r0),r2
	 sw 	 funcj(r0),r3
	 sw 	 funck(r0),r4
	 lw 	 r1,funci(r0)
	 sw 	 funcres(r0),r1
	 jr 	 r15
i	dw 0
t0	 dw 	 0
funcres	dw 0
funci	dw 0
funcj	dw 0
funck	dw 0
