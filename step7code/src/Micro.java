
import org.antlr.v4.runtime.*;
import java.io.*;
import java.lang.Exception;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Micro
{
	public static void main(String[] args) throws Exception
	{
		ANTLRFileStream token_stream = new ANTLRFileStream(args[0]);
		MicroLexer microLexer = new MicroLexer(token_stream);
		
		CommonTokenStream commonTokenStream = new CommonTokenStream(microLexer);
		MicroParser microParser = new MicroParser(commonTokenStream);

		// Change this to CustomErrorStrategy
		microParser.setErrorHandler(new BailErrorStrategy());
		
		try {
			ParseTree parseTree = microParser.program();
			ParseTreeWalker parseTreeWalker = new ParseTreeWalker(); // This used the Depth-First Traversal to traverse through the ParseTree
			Micro468Listener micro468Listener = new Micro468Listener(); // This is used to listen to changes in the Scope State. such as Entry and Exit
			parseTreeWalker.walk(micro468Listener, parseTree);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		return;
	}
}