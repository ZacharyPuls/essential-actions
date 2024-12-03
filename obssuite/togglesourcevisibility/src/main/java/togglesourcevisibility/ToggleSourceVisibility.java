package togglesourcevisibility;

import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;

import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemIdResponse;
import io.obswebsocket.community.client.message.response.sceneitems.SetSceneItemEnabledResponse;
import mother.motherconnection.MotherConnection;

public class ToggleSourceVisibility extends ToggleAction {
    public ToggleSourceVisibility() {
        setName("Toggle Source Visibility");
        setCategory("OBS");
        setVisibilityInServerSettingsPane(false);
        setAuthor("rnayabed");
        setVersion(MotherConnection.VERSION);
    }

    @Override
    public void onToggleOn() throws MinorException {
        onClicked(true);
    }

    @Override
    public void onToggleOff() throws MinorException {
        onClicked(false);
    }

    @Override
    public void initProperties() throws MinorException {
        Property sceneProperty = new Property("scene", Type.STRING);
        sceneProperty.setDisplayName("Scene");

        Property sourceProperty = new Property("source", Type.STRING);
        sourceProperty.setDisplayName("Source");

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDefaultValueBoolean(true);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");

        addClientProperties(sceneProperty, sourceProperty, autoConnectProperty);
    }

    public void onClicked(boolean visible) throws MinorException {
        getLogger().info("[ToggleSourceVisibility]::onClicked(" + visible + ")");
        String source = getClientProperties().getSingleProperty("source").getStringValue();
        String scene = getClientProperties().getSingleProperty("scene").getStringValue();

        if (source.isBlank()) {
            getLogger().info("[ToggleSourceVisibility]::onClicked: no source");
            throw new MinorException("Blank Source Name", "No Source specified");
        }

        if (scene.isBlank()) {
            getLogger().info("[ToggleSourceVisibility]::onClicked: no scene");
            throw new MinorException("Blank Scene Name", "No Source specified");
        }

        if (MotherConnection.getRemoteController() == null) {
            getLogger().info("[ToggleSourceVisibility]::onClicked: remote controller is null");
            boolean autoConnect = getClientProperties().getSingleProperty(
                    "auto_connect").getBoolValue();

            if (autoConnect) {
                MotherConnection.connect(false, () -> getLogger().info("Failed to connect to OBS."), () -> setVisibility(scene, source, visible), () -> getLogger().info("Disconnecting from OBS."));
            } else {
                MotherConnection.showOBSNotRunningError();
            }
        } else {
            getLogger().info("[ToggleSourceVisibility]::onClicked: executing setVisibility");
            setVisibility(scene, source, visible);
        }
    }

    public void setVisibility(String scene, String source, boolean visible) {
        getLogger().info("[ToggleSourceVisibility]::setVisibility(" + scene + "," + source + "," + visible + ")");
        GetSceneItemIdResponse getSceneItemIdResponse = MotherConnection.getRemoteController().getSceneItemId(scene, source, 0, 1000);
        getLogger().info("[ToggleSourceVisibility] getSceneItemIdReponse: " + getSceneItemIdResponse);
        if (!getSceneItemIdResponse.isSuccessful()) {
            new StreamPiAlert("OBS", "[ToggleSourceVisibility] Could not find Scene item ID for source " + source,
                    StreamPiAlertType.ERROR).show();
        } else {
            SetSceneItemEnabledResponse setSceneItemEnabledResponse = MotherConnection.getRemoteController().setSceneItemEnabled(scene, getSceneItemIdResponse.getSceneItemId(), visible, 1000);
            getLogger().info("[ToggleSourceVisibility] setSceneItemEnabledResponse: " + setSceneItemEnabledResponse);
            if (!setSceneItemEnabledResponse.isSuccessful()) {
                new StreamPiAlert("OBS", setSceneItemEnabledResponse.getMessageData().toString(), StreamPiAlertType.ERROR).show();
            }
        }
    }
}
