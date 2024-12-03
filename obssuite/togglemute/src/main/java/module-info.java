import togglemute.ToggleMute;

module com.stream_pi.obssuite.togglemuteaction
{
    requires com.stream_pi.action_api;
    requires com.stream_pi.util;

    requires client;
    requires com.stream_pi.obssuite.motheraction;

    provides com.stream_pi.action_api.externalplugin.ExternalPlugin with ToggleMute;
}
