package com.x.hhqs.joa.alarm.server.handler;


import com.x.hhqs.joa.alarm.model.OmcAlarmVOWrapper;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OmcAlarmChannelIndexFilter extends MessageToMessageEncoder<OmcAlarmVOWrapper> {

	public static final String NAME = "omcAlarmChannelIndexFilter";

	private final int channelIndex;

	public OmcAlarmChannelIndexFilter(int channelIndex) {
		this.channelIndex = channelIndex;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, OmcAlarmVOWrapper wrapper, List<Object> out) throws Exception {
		int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        log.info("OmcAlarmChannelIndexFilter{} channelIndex: {}", port, channelIndex);
		if (wrapper.isAlarmList() || wrapper.isSyncAlarms()) {
			List<OmcAlarmVO> alarms = wrapper.getOmcAlarmVOs();

			List<OmcAlarmVO> sends = alarms.stream()
					.filter(this::isTargetChannel)
					.collect(Collectors.toList());

            log.info("{} after filter: {}", port, sends.size());
			out.add(wrapper.resetAlarms(sends));
		} else {
			OmcAlarmVO alarm = wrapper.getOmcAlarmVO();
			if (isTargetChannel(alarm)) {
				out.add(wrapper);
			}
		}
	}

	public boolean isTargetChannel(OmcAlarmVO alarm) {
		return alarm.getChannelIndex() == channelIndex;
	}

}
