package xyz.nayskutzu.mythicalclient;

public class MessageBox {
    public static void error(String title, String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    public static void info(String title, String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warning(String title, String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Warning", javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(String title, String message) {
        int response = javax.swing.JOptionPane.showConfirmDialog(null, message, title, javax.swing.JOptionPane.YES_NO_OPTION);
        return response == javax.swing.JOptionPane.YES_OPTION;
    }

    public static String input(String title, String message) {
        return javax.swing.JOptionPane.showInputDialog(null, message, title);
    }

    public static void main(String[] args) {
        error("Error", "This is an error message.");
        info("Information", "This is an information message.");
        warning("Warning", "This is a warning message.");
        boolean confirmed = confirm("Confirmation", "Are you sure you want to proceed?");
        System.out.println("User confirmed: " + confirmed);
        String input = input("Input", "Please enter your name:");
        System.out.println("User input: " + input);
    }
    
}
