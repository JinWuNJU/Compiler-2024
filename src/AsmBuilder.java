import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;

import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    StringBuffer buffer = new StringBuffer();
    LLVMModuleRef module;
    String ls;

    int cut;
    int stackSize;
    Map<LLVMValueRef,Integer> map = new HashMap<>(); //only temp variable


    AsmBuilder(LLVMModuleRef module){
        this.module = module;
        ls = System.lineSeparator();
    }




        public void add(String dest, String src1, String src2) {
            buffer.append(String.format("  add %s, %s, %s\n", dest, src1, src2));
        }

        public void li(String dest, long imm) {
            buffer.append(String.format("  li %s, %d\n", dest, imm));
        }

    public void la(String dest, String name) {
        buffer.append(String.format("  la %s, %s\n", dest, name));
    }

        public void lw(String dest, int offset, String base) {
            buffer.append(String.format("  lw %s, %d(%s)\n", dest, offset, base));
        }

        public void sw(String src, int offset, String base) {
            buffer.append(String.format("  sw %s, %d(%s)\n", src, offset, base));
        }




        void data(String indent, long num){
            buffer.append("  .data\n" + indent + ":\n  .word " + num + "\n\n");
        }

    public String getResult() {
        return buffer.toString();
    }
    void end(){
        buffer.append("  addi sp, sp, " + stackSize + '\n' +
                    "  li a7, 93\n" + "  ecall");
    }

    void text(){
        buffer.append("  .text\n" + "  .global main\n" + "main:\n" + "  addi sp, sp, " + -stackSize + '\n'
                    + "mainEntry:\n");
    }

    void judge(LLVMValueRef value,String allo){// not use for store
        if(LLVMIsAGlobalVariable(value) != null){ // global
            la(LLVMGetValueName(value).getString(),allo);
            lw("t0",0,"t0");
        }else if(map.containsKey(value)){//// temp
            lw(allo,stackSize - map.get(value) * 4,"sp");
        }else{// const
            LLVMValueRef constantInt = LLVMGetOperand(value, 0);
            long num = LLVMConstIntGetSExtValue(constantInt);
            li(allo,num);
        }
    }


    void operate(){
        //System.out.println(-20%256);
        BytePointer irString = LLVMPrintModuleToString(module);
        System.out.println(irString.getString());


        for (LLVMValueRef value = LLVMGetFirstGlobal(module); value != null; value = LLVMGetNextGlobal(value)) {
            data(LLVMGetValueName(value).getString(),  LLVMConstIntGetSExtValue(LLVM.LLVMGetInitializer(value)));
        }
        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func)) {
            for (LLVMBasicBlockRef bb = LLVMGetFirstBasicBlock(func); bb != null; bb = LLVMGetNextBasicBlock(bb)) {
                for (LLVMValueRef inst = LLVMGetFirstInstruction(bb); inst != null; inst = LLVMGetNextInstruction(inst)){
                    if(!LLVMGetValueName(inst).getString().isEmpty()){
                        cut++;
                        map.put(inst,cut);
                    }
                }
            }
        }
        stackSize = (cut % 4 != 0) ? cut * 4 + (16 - (cut * 4) % 16) : cut * 4;
        text();
        // cut = 10
        //total = 48
        // first one 44(sp)
        // (48 - 1 * 4)(sp)
        for (LLVMValueRef func = LLVMGetFirstFunction(module); func != null; func = LLVMGetNextFunction(func)) {
            for (LLVMBasicBlockRef bb = LLVMGetFirstBasicBlock(func); bb != null; bb = LLVMGetNextBasicBlock(bb)) {
                for (LLVMValueRef inst = LLVMGetFirstInstruction(bb); inst != null; inst = LLVMGetNextInstruction(inst)) {
                    int opcode = LLVMGetInstructionOpcode(inst);
                    int operandNum = LLVMGetNumOperands(inst);

                    switch (opcode){
                        case LLVMRet:
                            judge(inst,"a0");
                            break;
//                        case LLVMAlloca:
//                            LLVMValueRef next = LLVMGetNextInstruction(inst);
//                            LLVMValueRef constantInt = LLVMGetOperand(next, 0);
//                            long value = LLVMConstIntGetSExtValue(constantInt);
//                            li("t0",value);
//                            sw("t0",stackSize - map.get(inst) * 4,"sp");
//                            inst = next;
//                            break;
//                        case LLVMAdd:
//                            LLVMValueRef op1 = LLVMGetOperand(inst, 0);
//                            LLVMValueRef op2 = LLVMGetOperand(inst, 1);
//                            judge(op1,"t0");
//                            judge(op2,"t1");
//                            add("t0","t0","t1");
//                            sw("t0",stackSize - map.get(inst) * 4,"sp");
//                            break;
//                        case LLVMStore:
//                            LLVMValueRef oop1 = LLVMGetOperand(inst, 0);
//                            LLVMValueRef oop2 = LLVMGetOperand(inst, 1);
//                            judge(oop1,"t0");
//                            if(LLVMIsAGlobalVariable(oop2) != null){ // global
//                                la(LLVMGetValueName(oop2).getString(),"t1");
//                                sw("t0",0,"t1");
//                            }else {// temp
//                                sw("t0",stackSize - map.get(oop2) * 4,"sp");
//                            }
//                            break;
//                        case LLVMLoad:
//                            judge(LLVMGetOperand(inst, 0),"t0");
//                            sw("t0",stackSize - map.get(inst) * 4,"sp");
//                            break;
                        default:
                            break;
                    }

                }

            }

        }
        end();


    }

}
