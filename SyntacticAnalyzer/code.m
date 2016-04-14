	 entry
	 lw 	 r0,test(r0)
	 sw 	 i(r0),r0

	 hlt

	 sub 	 r1,r1,r1
	 addi 	 r1,r1,0
	 sw 	 testres(r0),r1
	 jr 	 r15
i	dw 0
testres	dw 0
