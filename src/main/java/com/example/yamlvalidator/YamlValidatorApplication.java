package com.example.yamlvalidator;

import com.example.yamlvalidator.services.YamlService;
import org.apache.commons.cli.*;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.PrintWriter;

import static com.example.yamlvalidator.ValidatorUtils.needHelp;
import static java.lang.System.exit;

@SpringBootApplication
public class YamlValidatorApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(YamlValidatorApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = parser.parse(options(), args);

		if (needHelp(cmdLine)) {
			printHelp(options());
		} else {
			new YamlService().execute(cmdLine.getOptionValue("d"), cmdLine.getOptionValue("r"));
		}
		exit(0);
	}

	@Bean
	public Options options() {
		Options options = new Options();
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
		return options;
	}

	public void printHelp(Options options) {
		String commandLineSyntax = "execute ./target/*.jar";
		PrintWriter writer = new PrintWriter(System.out);
		HelpFormatter helpFormatter = new HelpFormatter();
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
