package com.example.yamlvalidator;

import com.example.yamlvalidator.services.YamlService;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.PrintWriter;

import static java.lang.System.exit;

@SpringBootApplication
public class YamlValidatorApplication implements CommandLineRunner {

	@Autowired
	private YamlService yamlService;

	public static void main(String[] args) {
		var app = new SpringApplication(YamlValidatorApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		var parser = new DefaultParser();
		var cmdLine = parser.parse(options(), args);

		if (needHelp(cmdLine)) {
			printHelp(options());
		} else {
			yamlService.execute(cmdLine.getOptionValue("d"), cmdLine.getOptionValue("r"));
		}
		exit(0);
	}

	@Bean
	public Options options() {
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
		options.addOption(Option.builder("e").desc("enrichment").build());
		return options;
	}

	private boolean needHelp(CommandLine cmd) {
		return cmd.getOptions().length == 0 || cmd.hasOption("h");
	}

	private void printHelp(Options options) {
		var commandLineSyntax = "java -jar ./target/*.jar --definition=\"definition.yaml\"";
		var writer = new PrintWriter(System.out);
		var helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(
				writer,
				80,
				commandLineSyntax,
				"Options: ",
				options,
				3,
				5,
				"--- HELP ---",
				false);
		writer.flush();
	}
}
