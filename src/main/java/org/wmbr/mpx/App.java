package org.wmbr.mpx;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        Options options = new Options();

        Option pollInterval = new Option("t", true, "polling interval in milliseconds");
        pollInterval.setRequired(true);
        pollInterval.setType(Number.class);
        options.addOption(pollInterval);

        Option url = new Option("e", true, "endpoint to poll for mpx data");
        url.setRequired(true);
        options.addOption(url);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            URI uri = new URI(cmd.getOptionValue(url));
            HttpGetPoller mPoller = new HttpGetPoller(
                    uri,
                    HttpClient.newHttpClient(),
                    new MPXTConsoleLogger(),
                    3);
            mPoller.startPolling(((Number) cmd.getParsedOptionValue(pollInterval)).longValue(), TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            formatter.printHelp("mpx-monitor", options);

            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "polling failed: " + e.toString(), e);

            System.exit(1);
        }
    }
}
