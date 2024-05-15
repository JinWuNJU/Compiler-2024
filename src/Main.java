import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.javacpp.BytePointer;

import java.io.IOException;

import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;
import static org.bytedeco.llvm.global.LLVM.LLVMPrintModuleToFile;

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

            if (LLVMPrintModuleToFile(visitor.module, args[1], error) != 0) {
                LLVMDisposeMessage(error);
            }
        }

    }
}
