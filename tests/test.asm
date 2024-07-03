  .data
x:
  .word 1

  .data
y:
  .word 2

  .data
z:
  .word 3

  .data
a:
  .word 4

  .data
b:
  .word 5

  .data
c:
  .word 6

  .data
d:
  .word 7

  .data
e:
  .word 8

  .data
f:
  .word 9

  .data
g:
  .word 10

  .data
h:
  .word 11

  .data
i:
  .word 12

  .data
j:
  .word 13

  .data
k:
  .word 14

  .data
l:
  .word 15

  .data
m:
  .word 16

  .data
n:
  .word 17

  .data
o:
  .word 18

  .data
p:
  .word 19

  .data
q:
  .word 20

  .text
  .global main
main:
  addi sp, sp, -160
mainEntry:
  li t0, 1
  sw t0, 156(sp)
  lw t0, 156(sp)
  sw t0, 152(sp)
  li t0, 2
  sw t0, 148(sp)
  lw t0, 148(sp)
  sw t0, 144(sp)
  li t0, 3
  sw t0, 140(sp)
  lw t0, 140(sp)
  sw t0, 136(sp)
  li t0, 4
  sw t0, 132(sp)
  lw t0, 132(sp)
  sw t0, 128(sp)
  li t0, 5
  sw t0, 124(sp)
  lw t0, 124(sp)
  sw t0, 120(sp)
  li t0, 6
  sw t0, 116(sp)
  lw t0, 116(sp)
  sw t0, 112(sp)
  li t0, 7
  sw t0, 108(sp)
  lw t0, 108(sp)
  sw t0, 104(sp)
  li t0, 8
  sw t0, 100(sp)
  lw t0, 100(sp)
  sw t0, 96(sp)
  li t0, 9
  sw t0, 92(sp)
  lw t0, 92(sp)
  sw t0, 88(sp)
  li t0, 10
  sw t0, 84(sp)
  lw t0, 84(sp)
  sw t0, 80(sp)
  li t0, 11
  sw t0, 76(sp)
  lw t0, 76(sp)
  sw t0, 72(sp)
  li t0, 12
  sw t0, 68(sp)
  lw t0, 68(sp)
  sw t0, 64(sp)
  li t0, 13
  sw t0, 60(sp)
  lw t0, 60(sp)
  sw t0, 56(sp)
  li t0, 14
  sw t0, 52(sp)
  lw t0, 52(sp)
  sw t0, 48(sp)
  li t0, 15
  sw t0, 44(sp)
  lw t0, 44(sp)
  sw t0, 40(sp)
  li t0, 16
  sw t0, 36(sp)
  lw t0, 36(sp)
  sw t0, 32(sp)
  li t0, 17
  sw t0, 28(sp)
  lw t0, 28(sp)
  sw t0, 24(sp)
  li t0, 18
  sw t0, 20(sp)
  lw t0, 20(sp)
  sw t0, 16(sp)
  li t0, 19
  sw t0, 12(sp)
  lw t0, 12(sp)
  sw t0, 8(sp)
  li t0, 20
  sw t0, 4(sp)
  lw t0, 4(sp)
  sw t0, 0(sp)
  li a0, 840
  addi sp, sp, 160
  li a7, 93
  ecall