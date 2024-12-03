module com.stream_pi.obssuite.setpreviewsceneaction
{
    requires com.stream_pi.action_api;
    requires com.stream_pi.util;

    requires client;
    requires com.stream_pi.obssuite.motheraction;

    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with setpreviewscene.SetPreviewScene;
}
