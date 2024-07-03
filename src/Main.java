import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.bytedeco.llvm.global.LLVM.*;

public class Main {

    public static final BytePointer error = new BytePointer();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("args error");
        }else{
            String source = args[0];
            CharStream input = CharStreams.fromFileName(source);
            SysYLexer sysYLexer = new SysYLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
            SysYParser sysYParser = new SysYParser(tokens);
            ParseTree tree = sysYParser.program();

            MyVisit visitor = new MyVisit();

            visitor.visit(tree);
            AsmBuilder asmBuilder = new AsmBuilder(visitor.module);
            asmBuilder.operate();

            File f = new File(args[1]);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(asmBuilder.buffer.toString().getBytes());

        }

    }
}
