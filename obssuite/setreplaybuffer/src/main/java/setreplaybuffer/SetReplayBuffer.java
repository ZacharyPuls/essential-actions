package setreplaybuffer;

import java.util.ArrayList;
import java.util.Arrays;

import com.stream_pi.action_api.actionproperty.property.ListValue;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.version.Version;

import mother.motherconnection.MotherConnection;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.response.RequestResponse;

public class SetReplayBuffer extends NormalAction
{

    public SetReplayBuffer()
    {
        setName("Set Replay Buffer");
        setCategory("OBS");
        setVisibilityInServerSettingsPane(false);
        setAuthor("rnayabed");
        setVersion(MotherConnection.VERSION);

        states = new ArrayList<>();
        states.addAll(Arrays.asList(
                new ListValue("Start"),
                new ListValue("Stop"),
                new ListValue("Save")
        ));
    }

    private final ArrayList<ListValue> states;

    @Override
    public void initProperties() throws MinorException
    {

        Property replayStatusActionProperty = new Property("replay_status", Type.LIST);
        replayStatusActionProperty.setListValue(states);
        replayStatusActionProperty.setDisplayName("Replay Buffer State");

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDefaultValueBoolean(true);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");

        addClientProperties(replayStatusActionProperty, autoConnectProperty);
    }

    @Override
    public void onActionClicked() throws MinorException
    {
        String state = states.get(getClientProperties().getSingleProperty("replay_status").getSelectedIndex()).getName().toString();

        if (MotherConnection.getRemoteController() == null)
        {
            boolean autoConnect = getClientProperties().getSingleProperty(
                    "auto_connect"
            ).getBoolValue();

            if(autoConnect)
            {
                MotherConnection.connect(()->setReplayBuffer(state));
            }
            else
            {
                MotherConnection.showOBSNotRunningError();
            }
        }
        else
        {
            setReplayBuffer(state);
        }
    }

    private void errorThrow(RequestResponse<Void> requestResponse)
    {
        if(!requestResponse.isSuccessful())
        {
            new StreamPiAlert("OBS", requestResponse.getMessageData().toString(), StreamPiAlertType.ERROR).show();
        }
    }

    private void setReplayBuffer(String state)
    {
        OBSRemoteController controller = MotherConnection.getRemoteController();

        switch (state)
        {
            case "Start":
                controller.startReplayBuffer(startReplayBufferResponse -> errorThrow(startReplayBufferResponse));
                break;
            case "Stop":
                controller.stopReplayBuffer(stopReplayBufferResponse -> errorThrow(stopReplayBufferResponse));
                break;
            case "Save":
                controller.saveReplayBuffer(saveReplayBufferResponse -> errorThrow(saveReplayBufferResponse));
                break;
        }
    }
}
