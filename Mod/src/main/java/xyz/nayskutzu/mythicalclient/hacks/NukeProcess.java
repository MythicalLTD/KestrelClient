package xyz.nayskutzu.mythicalclient.hacks;

public class NukeProcess {
    public static void main() {
        int response = javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to proceed?", "Confirmation", javax.swing.JOptionPane.YES_NO_OPTION);
        if (response == javax.swing.JOptionPane.YES_OPTION) {
            System.out.println("Nuke process started...");
            // Nuke process code here
            
            if (PlayerESP.enabled) {
                PlayerESP.main();
            }
            if (BridgeHack.enabled) {
                BridgeHack.main();
            }
            System.out.println("Nuke process completed.");
        } else {
            System.out.println("Process aborted.");
        }
    }
}
