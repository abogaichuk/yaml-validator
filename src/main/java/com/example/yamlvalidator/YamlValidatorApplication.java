package com.example.yamlvalidator;

import com.example.yamlvalidator.entity.Execution;
import com.example.yamlvalidator.services.YamlService;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;

public class YamlValidatorApplication {
    private static final int WIDTH = 80;
    private static final int LEFT_PAD = 3;
    private static final int DESC_PAD = 5;

    public static void main(String[] args) throws ParseException, IOException {
        var parser = new DefaultParser();
        var cmdLine = parser.parse(options(), args);

        if (needHelp(cmdLine)) {
            printHelp(options());
        } else {
            var execution = Execution.builder()
                    .definition(cmdLine.getOptionValue("d"))
                    .resource(cmdLine.getOptionValue("r"))
                    .resolvePlaceholders(cmdLine.hasOption("resolve-placeholders"))
                    .includeDefaults(cmdLine.hasOption("includeDefaults"))
                    .includeSecrets(cmdLine.hasOption("includeSecrets"))
                    .preview(cmdLine.hasOption("preview"))
                    .build();
            new YamlService().execute(execution);
        }
        System.exit(0);
    }

    private static Options options() {
        var options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("HELP").build());
        options.addOption(Option.builder("r")
                .longOpt("resource")
                .hasArg().numberOfArgs(1)
                .desc("path to resource file").build());
        options.addOption(Option.builder("d")
                .longOpt("definition")
                .hasArg().numberOfArgs(1)
                .desc("path to definition file, mandatory parameter")
                .build());
        options.addOption(Option.builder()
                .longOpt("resolve-placeholders")
                .desc("resolve placeholders or not")
                .build());
        options.addOption(Option.builder()
                .longOpt("includeDefaults")
                .desc("include defaults or not")
                .build());
        options.addOption(Option.builder()
                .longOpt("includeSecrets")
                .desc("include secrets or not, need admin privileges")
                .build());
        options.addOption(Option.builder("p")
                .longOpt("preview")
                .desc("by default preview in yaml, if set preview in json")
                .build());
        options.addOption(Option.builder("e").desc("enrichment").build());
        return options;
    }

    private static boolean needHelp(CommandLine cmd) {
        return cmd.getOptions().length == 0 || cmd.hasOption("h");
    }

    private static void printHelp(Options options) {
        var commandLineSyntax = "java -jar ./target/*.jar --definition=\"definition.yaml\"";
        var writer = new PrintWriter(System.out);
        var helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                WIDTH,
                commandLineSyntax,
                "Options: ",
                options,
                LEFT_PAD,
                DESC_PAD,
                "--- HELP ---",
                false);
        writer.flush();
    }
}
