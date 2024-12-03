package togglemute;

import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import mother.motherconnection.MotherConnection;

public class ToggleMute extends ToggleAction
{
    public ToggleMute()
    {
        setName("Toggle Mute");
        setCategory("OBS");
        setVisibilityInServerSettingsPane(false);
        setAuthor("rnayabed");
        setVersion(MotherConnection.VERSION);
    }

    @Override
    public void onToggleOn() throws MinorException
    {
        onClicked(true);
    }

    @Override
    public void onToggleOff() throws MinorException
    {
        onClicked(false);
    }

    @Override
    public void initProperties() throws MinorException
    {
        Property sourceProperty = new Property("source", Type.STRING);
        sourceProperty.setDisplayName("Source");

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDefaultValueBoolean(true);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");

        addClientProperties(sourceProperty, autoConnectProperty);
    }

    public void onClicked(boolean mute) throws MinorException
    {
        String source = getClientProperties().getSingleProperty("source").getStringValue();

        if(source.isBlank())
        {
            throw new MinorException("Blank Source Name","No Source specified");
        }

        if (MotherConnection.getRemoteController() == null)
        {
            boolean autoConnect = getClientProperties().getSingleProperty(
                    "auto_connect"
            ).getBoolValue();

            if(autoConnect)
            {
                MotherConnection.connect(()->setMute(source, mute));
            }
            else
            {
                MotherConnection.showOBSNotRunningError();
            }
        }
        else
        {
            setMute(source, mute);
        }
    }

    public void setMute(String scene, boolean mute)
    {
        MotherConnection.getRemoteController().setInputMute(scene, mute, setMuteResponse -> {
            if(!setMuteResponse.isSuccessful())
            {
                String content;

                if(setMuteResponse.getMessageData().getRequestStatus().getCode().equals(600))
                {
                    content = "Source "+scene+" does not exist.";
                }
                else
                {
                    content = setMuteResponse.getMessageData().getRequestStatus().toString();
                }

                new StreamPiAlert("OBS",content, StreamPiAlertType.ERROR).show();
            }
        });

    }
}
