package mother;

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import mother.motherconnection.MotherConnection;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSCommunicator;

public class OBSActionConnectionTask extends Task<Void> {
    private Runnable onFailToConnectRunnable = null;
    private Runnable onConnectRunnable = null;
    private Runnable onDisconnectRunnable = null;
    private OBSRemoteController obsRemoteController = null;

    public OBSActionConnectionTask(boolean runAsync, Runnable onFailToConnectRunnable,
            Runnable onConnectRunnable, Runnable onDisconnectRunnable) {
        this.onFailToConnectRunnable = onFailToConnectRunnable;
        this.onConnectRunnable = onConnectRunnable;
        this.onDisconnectRunnable = onDisconnectRunnable;

        if (runAsync) {
            new Thread(this).start();
        } else {
            call();
        }
    }

    private void onReady() {
        OBSActionConnectionTask.setConnectDisconnectButtonText("Disconnect");
        MotherConnection.setRemoteController(obsRemoteController);

        if (onConnectRunnable != null) {
            onConnectRunnable.run();
            onConnectRunnable = null;
        }
    }

    @Override
    protected Void call() {
        try {
            String url = MotherConnection.getUrl();
            String pass = MotherConnection.getPass();

            setConnectDisconnectButtonDisable(true);

            int lastColonIndex = url.lastIndexOf(":");

            if (!url.startsWith("ws://") || lastColonIndex == -1 || lastColonIndex == 2) {
                new StreamPiAlert("Invalid URL", "Please fix URL and try again", StreamPiAlertType.ERROR).show();
                return null;
            }

            if (pass.isEmpty() || pass.isBlank())
                pass = null;

            String host = url.substring(5, lastColonIndex);
            int port = Integer.parseInt(url.substring(lastColonIndex + 1));

            this.obsRemoteController = OBSRemoteController.builder().host(host).port(port)
                    .password(pass).lifecycle().onReady(this::onReady).and().build();

            obsRemoteController.connect();

        } catch (Exception e) {
            MotherConnection.showOBSNotRunningError();
            MotherConnection.setRemoteController(null);
            e.printStackTrace();
        } finally {
            setConnectDisconnectButtonDisable(false);
        }

        return null;
    }

    private static void setConnectDisconnectButtonText(String text) {
        if (MotherConnection.getConnectDisconnectButton() == null)
            return;

        Platform.runLater(() -> MotherConnection.getConnectDisconnectButton().setText(text));
    }

    private void setConnectDisconnectButtonDisable(boolean disable) {
        if (MotherConnection.getConnectDisconnectButton() == null)
            return;

        Platform.runLater(() -> MotherConnection.getConnectDisconnectButton().setDisable(disable));
    }

}