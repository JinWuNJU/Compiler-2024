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
  li a0, 840
  addi sp, sp, 160
  li a7, 93
  ecall