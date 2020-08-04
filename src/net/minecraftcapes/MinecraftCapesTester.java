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
    private JTextArea logArea;
    private JButton saveLogButton;
    private JButton retryConnectionButton;
    private JButton clearLogButton;
    private JButton closeButton;

    public MinecraftCapesTester() {
        testPingConnect();
        testHttpConnect();

        closeButton.addActionListener(event -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });

        clearLogButton.addActionListener(event -> {
            logArea.setText(null);
        });

        retryConnectionButton.addActionListener(event -> {
            testPingConnect();
            testHttpConnect();
        });

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

        frame.setContentPane(new MinecraftCapesTester().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * Logs a string
     * @param log string to log
     */
    private void log(String log) {
        logArea.append(log + "\n");
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
                    Thread.sleep(500);
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
}
