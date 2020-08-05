package net.minecraftcapes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MinecraftCapesTester {

    private static JFrame frame = new JFrame("MinecraftCapes Tester");
    private JPanel panelMain;
    private JScrollPane logAreaScroll;
    private JTextArea logArea;
    private JButton saveLogButton;
    private JButton retryConnectionButton;
    private JButton clearLogButton;
    private JButton closeButton;

    /**
     * Constructor
     */
    public MinecraftCapesTester() {
        //Create Frame
        frame.setContentPane(panelMain);
        frame.setIconImage(new ImageIcon(getClass().getResource("/favicon.png")).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        saveLogButton.addActionListener(event -> {
            String fileName = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(new Date()) + ".log";

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setSelectedFile(new File(System.getProperty("user.home"), "Desktop/" + fileName));
            fileChooser.setFileFilter(new FileNameExtensionFilter("log file","log"));
            fileChooser.showSaveDialog(frame);

            try(FileWriter fw = new FileWriter(fileChooser.getSelectedFile())) {
                fw.write(logArea.getText());
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        });

        clearLogButton.addActionListener(event -> {
            logArea.setText(null);
        });

        retryConnectionButton.addActionListener(event -> {
            runJobs();
        });

        closeButton.addActionListener(event -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        //Run the jobs
        runJobs();
    }

    /**
     * The main which runs ever3ything
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new MinecraftCapesTester();
    }

    /**
     * Logs a string
     * @param log string to log
     */
    private void log(String log) {
        logArea.append(log + "\n");
        logArea.setCaretPosition(logArea.getText().length());
    }

    /**
     * Logs an exception
     * @param e exception to log
     */
    private void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(sw.toString());
    }

    /**
     * Runs the jobs
     */
    private void runJobs() {
        new Thread(() -> {
            enableButtons(false);
            testPingConnect();
            testHttpConnect();
            enableButtons(true);
            log("******** COMPLETED ********");
        }).start();
    }

    /**
     * Sends a test ping connection
     */
    private void testPingConnect() {
        log("*** Starting PING at " + new Date().toString() + " ***\n");
        try {
            InetAddress ping = InetAddress.getByName("minecraftcapes.net");
            for (int i = 0; i < 4; i++) {
                long timeStarted = System.currentTimeMillis();
                if(ping.isReachable(1000)) {
                    long timeEnded = System.currentTimeMillis() - timeStarted;
                    log("Reply from " + ping.getHostAddress() + ": time=" + timeEnded + "ms");
                    Thread.sleep(1000);
                } else {
                    log("Request timed out");
                }
            }
        } catch(Exception e) {
            log("Ping Failed - " + e.getMessage());
            log(e);
        }
    }

    /**
     * Sends a test HTTPS connection
     */
    private void testHttpConnect() {
        log("\n*** Starting HTTPS Connection at " + new Date().toString() + " ***\n");
        try {
            log("Opening Connection...");
            URL url = new URL("https://minecraftcapes.net/profile/ba4161c03a42496c8ae07d13372f3371");
            HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();
            httpurlconnection.connect();
            log("Response Code: " + httpurlconnection.getResponseCode() + "\n");

            log("Response Headers:");
            httpurlconnection.getHeaderFields().forEach((header, value) -> {
                log(header + ": " + value);
            });
            log("");

            InputStream inputStream;
            if (httpurlconnection.getResponseCode() / 100 == 2) {
                inputStream = httpurlconnection.getInputStream();
            } else {
                inputStream = httpurlconnection.getErrorStream();
            }

            log("Response Body:");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String currentLine;
            while ((currentLine = in.readLine()) != null)
                log(currentLine);
            log("");

            in.close();
            log("Response Closed\n");
        } catch (Exception e) {
            log("Connection Failed - " + e.getMessage());
            log(e);
        }
    }

    /**
     * Toggles the buttons
     * @param value Should they be enabled/disabled
     */
    private void enableButtons(boolean value) {
        this.saveLogButton.setEnabled(value);
        this.retryConnectionButton.setEnabled(value);
        this.clearLogButton.setEnabled(value);
    }
}
