package com.glotitude.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "Unary    : Token operator, Expr right",
                "Array    : List<Expr> values",
                "Variable : Token name",
                "Dict     : Map<String,Expr> values",
                "Range    : Expr lowerBound, Expr upperBound"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Binding    : Token eventName, Token functionName, List<Token> params, List<Stmt> body",
                // fixme we should emit more than one event at a time
                "Emit       : Token eventName, Expr payload",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body",
                "For        : Token name, Expr iterable, Stmt body"
        ));
    }

    private static void defineAst(
            String outputDir,
            String baseName,
            List<String> types) throws IOException
    {
        String path = String.format("%s/%s.java", outputDir, baseName);
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.glotitude.jtlm;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("import java.util.Map;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
            PrintWriter writer,
            String baseName,
            List<String> types)
    {
        writer.println("\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(String.format(
                    "\t\tR visit%s%s(%s %s);",
                    typeName,
                    baseName,
                    typeName,
                    baseName.toLowerCase()
            ));
        }

        writer.println("\t}");
    }

    private static void defineType(
            PrintWriter writer,
            String baseName,
            String className,
            String fieldsList)
    {
        writer.println(String.format("\tstatic class %s extends %s {", className, baseName));

        String[] fields = fieldsList.split(", ");
        for (String field : fields) {
            writer.println(String.format("\t\tfinal %s;", field));
        }

        writer.println();

        writer.println(String.format("\t\t%s(%s) {", className, fieldsList));
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(String.format("\t\t\tthis.%s = %s;", name, name));
        }
        writer.println("\t\t}");

        writer.println();

        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println(String.format("\t\t\treturn visitor.visit%s%s(this);", className, baseName));
        writer.println("\t\t}");

        writer.println("\t}");
        writer.println();
    }
}
