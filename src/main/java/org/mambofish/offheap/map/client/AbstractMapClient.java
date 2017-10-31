package org.mambofish.offheap.map.client;

import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author vince
 */
public abstract class AbstractMapClient {

    protected Map map;

    private StringTokenizer stringTokenizer;

    public abstract Object key(String k);

    public abstract Object value(Object v);

    public abstract Object get(Object v);

    private void expect(String expected) throws Exception {
        String actual = nextToken();
        if (!expected.equals(actual)) {
            throw new Exception("Expected '" + expected + "' but got '" + actual + "'");
        }
    }

    private String nextToken() {
        if (stringTokenizer.hasMoreTokens()) {
            return stringTokenizer.nextToken().trim();
        } else {
            return "error: premature end of input";
        }
    }

    private Object doGet(String cmd) {
        stringTokenizer = new StringTokenizer(cmd, "()", true);
        try {
            expect("get");
            expect("(");
            String k = nextToken();
            expect(")");
            return( get(map.get(k)) );
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    private void doPut(String cmd) {

        stringTokenizer = new StringTokenizer(cmd, "(,)", true);

        try {

            expect("put");
            expect("(");
            String k = nextToken();
            expect(",");
            String v = nextToken();
            expect(")");

            map.put(key(k), value(v));

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    private void doDelete(String cmd) {
        throw new RuntimeException("Not implemented");
    }

    private void doKeys() {
        throw new RuntimeException("Not implemented");
    }

    private void doValues() {
        throw new RuntimeException("Not implemented");
    }

    private void doHelp() {

        System.out.println("Available commands:");
        System.out.println("\tput(key, value) : add an entry to the map");
        System.out.println("\tget(key)        : get an entry from the map");
        System.out.println("\trm(key)         : remove an entry to the map");
        System.out.println("\tkeys()          : list the keys in the map");
        System.out.println("\tvalues()        : list the values in the map");
        System.out.println("\thelp            : get help (this message)");
        System.out.println("\tquit/exit       : close the client");

    }

    public void listen() {
        
        CLI cli = new CLI();

        for (; ; ) {

            String cmd = cli.read().toLowerCase();

            if (cmd.startsWith("put(")) {
                doPut(cmd);
            }

            if (cmd.startsWith("get(")) {
                cli.write(String.valueOf(doGet(cmd)));
            }

            if (cmd.startsWith("delete(")) {
                doDelete(cmd);
            }

            if (cmd.equals("keys()")) {
                doKeys();
            }

            if (cmd.equals("values()")) {
                doValues();
            }

            if (cmd.equals("help")) {
                doHelp();
            }

            if (cmd.equals("quit") || cmd.equals("exit")) {
                break;
            }

        }

    }
    /**
     * Provides console input/output capability. Useful for applications that require
     * dynamic interaction with an input device.
     */
    static final class CLI {
        private String prompt = "> ";
        private PrintStream output = System.out;
        private BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        /**
         * Default constructor.<p>Calling the default constructor will initialise the class
         * and send the default announcement to the output device
         */
        public CLI() {
            hello();
        }

        /**
         * Message constructor.<p>Calling this constructor will initialise the class and send the supplied announcement
         * to the output device. Clients who wish to customise the start up messages may use
         * this constructor.
         *
         * @param announcement - the message to be displayed when the cli instance starts
         */
        public CLI(String announcement) {
            output.println(announcement + "\n");
        }

        private void hello() {
            output.println("\nShared Map Command Line Interface\ninitialising, please wait...\n");
        }

        /**
         * Writes the current prompt string to the output device
         */
        public void doPrompt() {
            try {
                output.print(prompt);
                input.skip(prompt.length());
            } catch (Exception ignored) {
            }
        }

        /**
         * Sets the prompt string
         *
         * @param prompt - the prompt to display on the current output device
         */
        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        /**
         * Writes a message to the output device
         *
         * @param msg - the message to display on the output device.
         */
        public void write(String msg) {
            output.println(msg);
            output.flush();
        }

        /**
         * Reads characters from the input device.<p>The read terminates if the character read is
         * a newline character (<code>'\n'</code>).
         *
         * @return a String containing characters read from the input device
         *         (excluding the terminating newline character).
         */
        public String read() {
            try {
                // Loop forever, reading the user's input
                while (true) {
                    output.print(prompt);    // prompt the user
                    output.flush();          // make the prompt appear immediately
                    String inpbuf = input.readLine();  // get a line of input from the user

                    if (inpbuf.length() == 0) continue;

                    return inpbuf;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }

        /**
         * Sets the input device to read from a specified file
         *
         * @param fileName name of the file that is to be used for reading from
         *                 by the {@link #read} method.
         */
        public void setInput(String fileName) {
            try {
                input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            } catch (Exception e) {
                output.println("Cannot set input stream: " + e);
            }
        }

        /**
         * Sets the output device to write to the specified file
         *
         * @param fileName name of the file that is to be used for writing to
         *                 by the {@link #write} method.
         */
        public void setOutput(String fileName) {
            try {
                output = new java.io.PrintStream(new FileOutputStream(fileName));
            } catch (Exception e) {
                output.println("Cannot set output stream: " + e);
            }
        }
    }



}
