package setpreviewscene;

import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import mother.motherconnection.MotherConnection;

public class SetPreviewScene extends NormalAction
{
    public SetPreviewScene()
    {
        setName("Set Preview Scene");
        setCategory("OBS");
        setVisibilityInServerSettingsPane(false);
        setAuthor("rnayabed");
        setVersion(MotherConnection.VERSION);
    }

    @Override
    public void initProperties() throws MinorException
    {
        Property previewSceneProperty = new Property("preview_scene", Type.STRING);
        previewSceneProperty.setDisplayName("Preview Scene");

        Property autoConnectProperty = new Property("auto_connect", Type.BOOLEAN);
        autoConnectProperty.setDefaultValueBoolean(true);
        autoConnectProperty.setDisplayName("Auto Connect if not connected");

        addClientProperties(previewSceneProperty, autoConnectProperty);
    }

    @Override
    public void onActionClicked() throws MinorException
    {
        String previewScene = getClientProperties().getSingleProperty("preview_scene").getStringValue();

        if(previewScene.isBlank())
        {
            throw new MinorException("Blank Preview Scene Name","No Preview Scene Name specified");
        }

        if (MotherConnection.getRemoteController() == null)
        {
            boolean autoConnect = getClientProperties().getSingleProperty(
                    "auto_connect"
            ).getBoolValue();

            if(autoConnect)
            {
                MotherConnection.connect(()->setPreviewScene(previewScene));
            }
            else
            {
                MotherConnection.showOBSNotRunningError();
            }
        }
        else
        {
            setPreviewScene(previewScene);
        }
    }

    public void setPreviewScene(String previewScene)
    {
        MotherConnection.getRemoteController().setCurrentPreviewScene(previewScene, setPreviewSceneResponse -> {
            if(!setPreviewSceneResponse.isSuccessful())
            {
                String content;

                if(setPreviewSceneResponse.getMessageData().getRequestStatus().getCode().equals(600))
                {
                    content = "Preview Scene "+previewScene+" does not exist.";
                }
                else
                {
                    content = setPreviewSceneResponse.getMessageData().getRequestStatus().toString();
                }

                new StreamPiAlert("OBS",content, StreamPiAlertType.ERROR).show();
            }
        });
    }
}
