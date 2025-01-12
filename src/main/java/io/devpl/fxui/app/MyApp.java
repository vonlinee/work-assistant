package io.devpl.fxui.app;

import org.apache.commons.cli.*;

public class MyApp {
    public static void main(String[] args) {
        // 定义命令行参数
        Options options = new Options();
        Option input = new Option("i", "input", true, "输入文件路径");
        input.setRequired(true); // 设置为必需参数
        options.addOption(input);
        // 解析命令行参数
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue("input");
            System.out.println("输入文件为: " + inputFile);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MyApp", options); // 打印帮助信息

            System.exit(1);
        }
    }
}
